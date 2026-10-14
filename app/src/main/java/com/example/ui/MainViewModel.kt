package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val estimateDao = db.estimateDao()
    private val orderDao = db.confirmedOrderDao()
    private val visitorDao = db.visitorDao()
    private val carpenterDao = db.carpenterDao()
    private val carpenterAssignmentDao = db.carpenterAssignmentDao()
    private val attendanceDao = db.attendanceDao()
    private val firestoreRepo by lazy { com.example.data.repository.FirestoreRepository(application) }

    val estimates: StateFlow<List<Estimate>> = estimateDao.getAllEstimates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<ConfirmedOrder>> = orderDao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visitors: StateFlow<List<Visitor>> = visitorDao.getAllVisitors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carpenters: StateFlow<List<Carpenter>> = carpenterDao.getAllCarpenters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carpenterAssignments: StateFlow<List<CarpenterAssignment>> = carpenterAssignmentDao.getAllAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<AttendanceRecord?> = attendanceDao.getLatestAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    private val _selectedMonth = MutableStateFlow("October")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow("2026")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if DB is empty on first launch and seed if needed
            val orderList = orderDao.getAllOrders().first()
            if (orderList.isEmpty()) {
                AppDatabase.seedInitialData(db)
            }
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        _currentSubScreen.value = SubScreen.NONE
    }

    fun setSubScreen(subScreen: SubScreen) {
        _currentSubScreen.value = subScreen
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setSelectedYear(year: String) {
        _selectedYear.value = year
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun toggleCheckIn() {
        viewModelScope.launch {
            val current = attendance.value
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            val currentDate = dateFormat.format(Date())

            if (current == null || !current.isCheckedIn) {
                attendanceDao.insertAttendance(
                    AttendanceRecord(
                        date = currentDate,
                        checkInTime = currentTime,
                        checkOutTime = "",
                        isCheckedIn = true
                    )
                )
                _userMessage.value = "Shop Checked In successfully at $currentTime"
            } else {
                attendanceDao.insertAttendance(
                    AttendanceRecord(
                        date = current.date,
                        checkInTime = current.checkInTime,
                        checkOutTime = currentTime,
                        isCheckedIn = false
                    )
                )
                _userMessage.value = "Shop Checked Out at $currentTime"
            }
        }
    }

    fun addEstimate(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        productType: String,
        dimensions: String,
        areaSqFt: Double,
        ratePerSqFt: Double,
        hardwareCost: Double,
        discountAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val baseCost = areaSqFt * ratePerSqFt
            val finalTotal = (baseCost + hardwareCost - discountAmount).coerceAtLeast(0.0)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val estimate = Estimate(
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                productType = productType,
                dimensions = dimensions,
                areaSqFt = areaSqFt,
                ratePerSqFt = ratePerSqFt,
                hardwareCost = hardwareCost,
                discountAmount = discountAmount,
                totalAmount = finalTotal,
                createdDate = dateFormat.format(Date()),
                status = "Sent",
                notes = notes
            )
            estimateDao.insertEstimate(estimate)
            _userMessage.value = "Estimate of ₹${String.format(Locale.getDefault(), "%,.0f", finalTotal)} created for $customerName"
            _currentSubScreen.value = SubScreen.ESTIMATE_LIST
        }
    }

    fun convertEstimateToOrder(estimate: Estimate, advancePaid: Double) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val orderDate = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 14) // promised delivery in 2 weeks
            val deliveryDate = dateFormat.format(cal.time)

            val orderNo = "ORD-ARV-${System.currentTimeMillis() % 100000}"
            val balance = (estimate.totalAmount - advancePaid).coerceAtLeast(0.0)

            val order = ConfirmedOrder(
                orderNumber = orderNo,
                customerName = estimate.customerName,
                customerPhone = estimate.customerPhone,
                deliveryAddress = estimate.customerAddress,
                productSummary = "${estimate.productType} (${estimate.dimensions})",
                totalAmount = estimate.totalAmount,
                advancePaid = advancePaid,
                balanceDue = balance,
                orderDate = orderDate,
                deliveryDate = deliveryDate,
                stage = "Order Confirmed",
                paymentMode = "UPI / GPay"
            )

            orderDao.insertOrder(order)
            estimateDao.updateEstimate(estimate.copy(status = "Converted to Order"))
            _userMessage.value = "Converted to Confirmed Order $orderNo!"
            _currentSubScreen.value = SubScreen.CONFIRM_ORDER_LIST
        }
    }

    fun addConfirmedOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        productSummary: String,
        totalAmount: Double,
        advancePaid: Double,
        deliveryDate: String,
        paymentMode: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val orderNo = "ORD-ARV-${System.currentTimeMillis() % 100000}"
            val balance = (totalAmount - advancePaid).coerceAtLeast(0.0)

            val order = ConfirmedOrder(
                orderNumber = orderNo,
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = deliveryAddress,
                productSummary = productSummary,
                totalAmount = totalAmount,
                advancePaid = advancePaid,
                balanceDue = balance,
                orderDate = dateFormat.format(Date()),
                deliveryDate = deliveryDate,
                stage = "Order Confirmed",
                paymentMode = paymentMode
            )
            orderDao.insertOrder(order)
            _userMessage.value = "Confirmed Order $orderNo created successfully!"
            _currentSubScreen.value = SubScreen.CONFIRM_ORDER_LIST
        }
    }

    fun updateOrderStage(order: ConfirmedOrder, newStage: String) {
        viewModelScope.launch {
            orderDao.updateOrder(order.copy(stage = newStage))
            _userMessage.value = "Order ${order.orderNumber} status updated to: $newStage"
        }
    }

    fun addOrderPayment(order: ConfirmedOrder, paymentAmount: Double) {
        viewModelScope.launch {
            val newAdvance = order.advancePaid + paymentAmount
            val newBalance = (order.totalAmount - newAdvance).coerceAtLeast(0.0)
            orderDao.updateOrder(order.copy(advancePaid = newAdvance, balanceDue = newBalance))
            _userMessage.value = "Recorded payment of ₹$paymentAmount for ${order.orderNumber}"
        }
    }

    fun addVisitor(
        name: String,
        phone: String,
        address: String,
        interestedProduct: String,
        budget: String,
        priority: String,
        followUpDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val visitor = Visitor(
                name = name,
                phone = phone,
                address = address,
                visitDate = dateFormat.format(Date()),
                interestedProduct = interestedProduct,
                budget = budget,
                priority = priority,
                followUpDate = followUpDate,
                notes = notes,
                status = "Active"
            )
            visitorDao.insertVisitor(visitor)
            _userMessage.value = "Visitor entry logged for $name"
            _currentSubScreen.value = SubScreen.VISITOR_LIST
        }
    }

    fun addCarpenter(
        name: String,
        phone: String,
        location: String,
        specialty: String = "PVC Modular Kitchens",
        experienceYears: Int = 5,
        tier: String = "Gold Mitra"
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val carpenter = Carpenter(
                name = name,
                phone = phone,
                location = location,
                specialty = specialty,
                experienceYears = experienceYears,
                tier = tier,
                joinDate = dateFormat.format(Date())
            )
            val insertedId = carpenterDao.insertCarpenter(carpenter)
            val savedCarpenter = carpenter.copy(id = insertedId)

            if (firestoreRepo.currentUser != null) {
                val syncResult = firestoreRepo.saveCarpenterToFirestore(savedCarpenter)
                if (syncResult.isSuccess) {
                    _userMessage.value = "Registered $name & synced to Firestore Cloud!"
                } else {
                    _userMessage.value = "Registered $name locally. Cloud sync pending."
                }
            } else {
                _userMessage.value = "Registered new Carpenter Partner: $name"
            }
            _currentSubScreen.value = SubScreen.CARPENTER_LIST
        }
    }

    fun addAssignment(
        carpenterId: Long,
        clientName: String,
        clientPhone: String,
        siteAddress: String,
        workType: String,
        laborCharge: Double,
        advancePaid: Double,
        targetCompletionDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val assignment = CarpenterAssignment(
                carpenterId = carpenterId,
                clientName = clientName,
                clientPhone = clientPhone,
                siteAddress = siteAddress,
                workType = workType,
                laborCharge = laborCharge,
                advancePaid = advancePaid,
                assignedDate = dateFormat.format(Date()),
                targetCompletionDate = targetCompletionDate,
                status = "In Progress",
                notes = notes
            )
            val assignId = carpenterAssignmentDao.insertAssignment(assignment)
            val savedAssignment = assignment.copy(id = assignId)

            if (firestoreRepo.currentUser != null) {
                val result = firestoreRepo.saveAssignmentToFirestore(carpenterId, savedAssignment)
                if (result.isSuccess) {
                    _userMessage.value = "Assigned project for $clientName & synced to Firestore!"
                } else {
                    _userMessage.value = "Project assigned locally. Cloud sync: ${result.exceptionOrNull()?.message}"
                }
            } else {
                _userMessage.value = "Project assigned for $clientName successfully!"
            }
        }
    }

    fun updateAssignmentStatus(
        assignment: CarpenterAssignment,
        newStatus: String,
        additionalPayment: Double = 0.0
    ) {
        viewModelScope.launch {
            val newAdvance = assignment.advancePaid + additionalPayment
            val updated = assignment.copy(status = newStatus, advancePaid = newAdvance)
            carpenterAssignmentDao.updateAssignment(updated)

            if (firestoreRepo.currentUser != null) {
                firestoreRepo.updateAssignmentStatusInFirestore(
                    carpenterId = assignment.carpenterId,
                    assignmentId = assignment.id,
                    newStatus = newStatus,
                    advancePaid = newAdvance
                )
            }
            _userMessage.value = "Project updated to '$newStatus'!"
        }
    }

    fun deleteAssignment(assignment: CarpenterAssignment) {
        viewModelScope.launch {
            carpenterAssignmentDao.deleteAssignment(assignment)
            _userMessage.value = "Project assignment removed."
        }
    }

    fun syncCarpenterToFirestore(carpenter: Carpenter) {
        viewModelScope.launch {
            if (firestoreRepo.currentUser == null) {
                _userMessage.value = "Please sign in with Google to sync to Firestore Cloud."
                return@launch
            }
            _isCloudSyncing.value = true
            try {
                val assignmentsForCarp = carpenterAssignments.value.filter { it.carpenterId == carpenter.id }
                val result = firestoreRepo.saveCarpenterToFirestore(carpenter, assignmentsForCarp)
                if (result.isSuccess) {
                    _userMessage.value = "Carpenter ${carpenter.name} & ${assignmentsForCarp.size} projects synced to Firestore!"
                } else {
                    _userMessage.value = "Sync failed: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun syncAllCarpentersToFirestore() {
        viewModelScope.launch {
            if (firestoreRepo.currentUser == null) {
                _userMessage.value = "Please sign in with Google first to sync with Firestore Cloud."
                return@launch
            }
            _isCloudSyncing.value = true
            try {
                val currentCarps = carpenters.value
                val currentAssigns = carpenterAssignments.value
                val result = firestoreRepo.syncAllCarpentersAndAssignments(currentCarps, currentAssigns)
                if (result.isSuccess) {
                    _userMessage.value = "Successfully synced ${result.getOrNull()} carpenters & ${currentAssigns.size} projects to Firestore!"
                } else {
                    _userMessage.value = "Cloud sync error: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun recordCarpenterReferral(carpenter: Carpenter, commissionAmt: Double, points: Int) {
        viewModelScope.launch {
            val updated = carpenter.copy(
                totalReferrals = carpenter.totalReferrals + 1,
                pointsEarned = carpenter.pointsEarned + points,
                totalCommissionEarned = carpenter.totalCommissionEarned + commissionAmt,
                pendingPayout = carpenter.pendingPayout + commissionAmt
            )
            carpenterDao.updateCarpenter(updated)

            if (firestoreRepo.currentUser != null) {
                val assignmentsForCarp = carpenterAssignments.value.filter { it.carpenterId == carpenter.id }
                firestoreRepo.saveCarpenterToFirestore(updated, assignmentsForCarp)
            }
            _userMessage.value = "Added referral commission ₹$commissionAmt to ${carpenter.name}"
        }
    }

    fun payoutCarpenter(carpenter: Carpenter, amount: Double) {
        viewModelScope.launch {
            val remaining = (carpenter.pendingPayout - amount).coerceAtLeast(0.0)
            val updated = carpenter.copy(pendingPayout = remaining)
            carpenterDao.updateCarpenter(updated)

            if (firestoreRepo.currentUser != null) {
                val assignmentsForCarp = carpenterAssignments.value.filter { it.carpenterId == carpenter.id }
                firestoreRepo.saveCarpenterToFirestore(updated, assignmentsForCarp)
            }
            _userMessage.value = "Paid ₹$amount commission to ${carpenter.name}"
        }
    }
}
