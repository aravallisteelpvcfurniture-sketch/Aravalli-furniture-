package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Estimate::class,
        ConfirmedOrder::class,
        Visitor::class,
        Carpenter::class,
        CarpenterAssignment::class,
        AttendanceRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun estimateDao(): EstimateDao
    abstract fun confirmedOrderDao(): ConfirmedOrderDao
    abstract fun visitorDao(): VisitorDao
    abstract fun carpenterDao(): CarpenterDao
    abstract fun carpenterAssignmentDao(): CarpenterAssignmentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aravalli_furniture_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            // Seed sample Confirmed Orders totaling ~₹662,092 matching the screenshot
            val initialOrders = listOf(
                ConfirmedOrder(
                    orderNumber = "ORD-ARV-2026-101",
                    customerName = "Prakash Mewara",
                    customerPhone = "+91 98290 12345",
                    deliveryAddress = "Plot 42, Shreenath Nagar, Rajsamand",
                    productSummary = "PVC Modular L-Shape Kitchen (12x9 ft) with Acrylic Finish",
                    totalAmount = 145000.0,
                    advancePaid = 80000.0,
                    balanceDue = 65000.0,
                    orderDate = "2026-10-02",
                    deliveryDate = "2026-10-18",
                    stage = "Assembling",
                    paymentMode = "UPI / GPay"
                ),
                ConfirmedOrder(
                    orderNumber = "ORD-ARV-2026-102",
                    customerName = "Vikram Rathore",
                    customerPhone = "+91 94140 88219",
                    deliveryAddress = "Sector 14, Hiran Magri, Udaipur",
                    productSummary = "Heavy Gauge 3-Door Steel Almirah + Digital Locker (Matte Grey)",
                    totalAmount = 88500.0,
                    advancePaid = 50000.0,
                    balanceDue = 38500.0,
                    orderDate = "2026-10-05",
                    deliveryDate = "2026-10-14",
                    stage = "Ready for Dispatch",
                    paymentMode = "Cash"
                ),
                ConfirmedOrder(
                    orderNumber = "ORD-ARV-2026-103",
                    customerName = "Kailash Choudhary",
                    customerPhone = "+91 97845 33412",
                    deliveryAddress = "Near Bus Stand, Nathdwara",
                    productSummary = "PVC Waterproof Bathroom & Balcony Doors (6 Units) + Frames",
                    totalAmount = 64200.0,
                    advancePaid = 40000.0,
                    balanceDue = 24200.0,
                    orderDate = "2026-10-07",
                    deliveryDate = "2026-10-20",
                    stage = "Material Cutting",
                    paymentMode = "UPI / GPay"
                ),
                ConfirmedOrder(
                    orderNumber = "ORD-ARV-2026-104",
                    customerName = "Anil Somani",
                    customerPhone = "+91 98282 55901",
                    deliveryAddress = "Subhash Nagar, Bhilwara",
                    productSummary = "Complete PVC Bedroom Wardrobe (9x7 ft) with Loft Cabinets",
                    totalAmount = 175000.0,
                    advancePaid = 100000.0,
                    balanceDue = 75000.0,
                    orderDate = "2026-10-09",
                    deliveryDate = "2026-10-24",
                    stage = "Material Cutting",
                    paymentMode = "Cheque"
                ),
                ConfirmedOrder(
                    orderNumber = "ORD-ARV-2026-105",
                    customerName = "Mahaveer Jain",
                    customerPhone = "+91 94600 77123",
                    deliveryAddress = "Goverdhan Vilas, Udaipur",
                    productSummary = "Commercial Steel Storage Racks & File Cabinets (10 Units)",
                    totalAmount = 189392.0,
                    advancePaid = 120000.0,
                    balanceDue = 69392.0,
                    orderDate = "2026-10-11",
                    deliveryDate = "2026-10-28",
                    stage = "Order Confirmed",
                    paymentMode = "UPI / GPay"
                )
            )
            database.confirmedOrderDao().insertAll(initialOrders)

            // Seed sample Estimates
            val initialEstimates = listOf(
                Estimate(
                    customerName = "Prakash Mewara",
                    customerPhone = "+91 98290 12345",
                    customerAddress = "Rajsamand",
                    productType = "PVC Modular Kitchen",
                    dimensions = "12 x 9 ft",
                    areaSqFt = 108.0,
                    ratePerSqFt = 1350.0,
                    hardwareCost = 15000.0,
                    discountAmount = 15800.0,
                    totalAmount = 145000.0,
                    createdDate = "2026-10-01",
                    status = "Converted to Order",
                    notes = "Acrylic high gloss sheets requested"
                ),
                Estimate(
                    customerName = "Dr. Sanjay Bhatt",
                    customerPhone = "+91 94141 99011",
                    customerAddress = "Chetak Circle, Udaipur",
                    productType = "PVC False Ceiling & Wall Panels",
                    dimensions = "16 x 14 ft",
                    areaSqFt = 224.0,
                    ratePerSqFt = 110.0,
                    hardwareCost = 8000.0,
                    discountAmount = 2640.0,
                    totalAmount = 30000.0,
                    createdDate = "2026-10-04",
                    status = "Sent",
                    notes = "Waiting for site measurement approval"
                ),
                Estimate(
                    customerName = "Vikram Rathore",
                    customerPhone = "+91 94140 88219",
                    customerAddress = "Udaipur",
                    productType = "Steel Almirah (Wardrobe)",
                    dimensions = "6.5 x 4 ft",
                    areaSqFt = 26.0,
                    ratePerSqFt = 3400.0,
                    hardwareCost = 0.0,
                    discountAmount = 0.0,
                    totalAmount = 88500.0,
                    createdDate = "2026-10-04",
                    status = "Converted to Order",
                    notes = "18 gauge heavy steel with locker"
                ),
                Estimate(
                    customerName = "Smt. Leela Dave",
                    customerPhone = "+91 97833 44556",
                    customerAddress = "Kankroli, Rajsamand",
                    productType = "PVC Modular Kitchen",
                    dimensions = "10 x 8 ft",
                    areaSqFt = 80.0,
                    ratePerSqFt = 1400.0,
                    hardwareCost = 18000.0,
                    discountAmount = 10000.0,
                    totalAmount = 120000.0,
                    createdDate = "2026-10-06",
                    status = "Sent",
                    notes = "Client requested soft-close telescopic drawers"
                ),
                Estimate(
                    customerName = "Anil Somani",
                    customerPhone = "+91 98282 55901",
                    customerAddress = "Bhilwara",
                    productType = "PVC Bedroom Wardrobe",
                    dimensions = "9 x 7 ft",
                    areaSqFt = 63.0,
                    ratePerSqFt = 2500.0,
                    hardwareCost = 25000.0,
                    discountAmount = 7500.0,
                    totalAmount = 175000.0,
                    createdDate = "2026-10-08",
                    status = "Converted to Order",
                    notes = "Woodgrain textured PVC finish"
                ),
                Estimate(
                    customerName = "Gopal Krishna Gehlot",
                    customerPhone = "+91 94133 22114",
                    customerAddress = "Bedla, Udaipur",
                    productType = "PVC Solid Doors (10 Qty)",
                    dimensions = "7 x 3 ft",
                    areaSqFt = 210.0,
                    ratePerSqFt = 380.0,
                    hardwareCost = 12000.0,
                    discountAmount = 6000.0,
                    totalAmount = 85800.0,
                    createdDate = "2026-10-10",
                    status = "Sent",
                    notes = "For hotel resort project"
                )
            )
            database.estimateDao().insertAll(initialEstimates)

            // Seed sample Visitors
            val initialVisitors = listOf(
                Visitor(
                    name = "Manoj Purohit",
                    phone = "+91 98291 44521",
                    address = "Fatehpura, Udaipur",
                    visitDate = "2026-10-12",
                    interestedProduct = "PVC Modular Kitchen & Pantry",
                    budget = "₹1.5 Lakh - ₹2 Lakh",
                    priority = "Hot",
                    followUpDate = "2026-10-14",
                    notes = "Visited showroom with spouse. Liked grey marble textured PVC finish.",
                    status = "Active"
                ),
                Visitor(
                    name = "Rakesh Sharma",
                    phone = "+91 94142 88710",
                    address = "Kankroli",
                    visitDate = "2026-10-12",
                    interestedProduct = "Steel Almirah with Mirror & Locker",
                    budget = "₹35,000 - ₹50,000",
                    priority = "Hot",
                    followUpDate = "2026-10-13",
                    notes = "Looking for heavy 18-gauge steel wardrobe for wedding gift.",
                    status = "Active"
                ),
                Visitor(
                    name = "Bhavna Patel",
                    phone = "+91 97840 11984",
                    address = "Sector 4, Hiran Magri",
                    visitDate = "2026-10-11",
                    interestedProduct = "PVC Bathroom & Terrace Doors",
                    budget = "₹25,000",
                    priority = "Warm",
                    followUpDate = "2026-10-15",
                    notes = "Water leakage in old wooden door, wants 100% waterproof PVC.",
                    status = "Active"
                ),
                Visitor(
                    name = "Jitendra Vaishnav",
                    phone = "+91 94611 33452",
                    address = "Nathdwara",
                    visitDate = "2026-10-10",
                    interestedProduct = "PVC Fluted Wall Paneling for Drawing Room",
                    budget = "₹40,000",
                    priority = "Warm",
                    followUpDate = "2026-10-16",
                    notes = "Requested catalog PDF on WhatsApp.",
                    status = "Active"
                )
            )
            database.visitorDao().insertAll(initialVisitors)

            // Seed sample Carpenters
            val initialCarpenters = listOf(
                Carpenter(
                    id = 1L,
                    name = "Ramesh Suthar (Master)",
                    phone = "+91 98299 11223",
                    location = "Kankroli & Rajsamand",
                    specialty = "PVC Modular Kitchen Specialist",
                    experienceYears = 12,
                    tier = "Platinum Mitra",
                    totalReferrals = 14,
                    pointsEarned = 1420,
                    totalCommissionEarned = 35500.0,
                    pendingPayout = 4500.0,
                    joinDate = "2025-06-15"
                ),
                Carpenter(
                    id = 2L,
                    name = "Dinesh Mistri",
                    phone = "+91 94145 66778",
                    location = "Udaipur North",
                    specialty = "Steel Wardrobe & Almirah Fitter",
                    experienceYears = 9,
                    tier = "Gold Mitra",
                    totalReferrals = 9,
                    pointsEarned = 950,
                    totalCommissionEarned = 23750.0,
                    pendingPayout = 2500.0,
                    joinDate = "2025-08-20"
                ),
                Carpenter(
                    id = 3L,
                    name = "Shyamlal Panchal",
                    phone = "+91 97821 44556",
                    location = "Nathdwara",
                    specialty = "PVC Waterproof Door Master",
                    experienceYears = 7,
                    tier = "Gold Mitra",
                    totalReferrals = 7,
                    pointsEarned = 720,
                    totalCommissionEarned = 18000.0,
                    pendingPayout = 1200.0,
                    joinDate = "2025-11-04"
                ),
                Carpenter(
                    id = 4L,
                    name = "Kishore Kumar Jangid",
                    phone = "+91 94602 88990",
                    location = "Bhilwara Bypass",
                    specialty = "PVC Fluted Ceiling & Paneling",
                    experienceYears = 5,
                    tier = "Silver Mitra",
                    totalReferrals = 5,
                    pointsEarned = 510,
                    totalCommissionEarned = 12750.0,
                    pendingPayout = 0.0,
                    joinDate = "2026-02-18"
                )
            )
            database.carpenterDao().insertAll(initialCarpenters)

            // Seed active project assignments
            val initialAssignments = listOf(
                CarpenterAssignment(
                    carpenterId = 1L,
                    clientName = "Prakash Mewara",
                    clientPhone = "+91 98290 12345",
                    siteAddress = "Plot 42, Shreenath Nagar, Rajsamand",
                    workType = "L-Shape PVC Kitchen Installation (12x9 ft)",
                    laborCharge = 18000.0,
                    advancePaid = 8000.0,
                    assignedDate = "2026-10-06",
                    targetCompletionDate = "2026-10-18",
                    status = "In Progress",
                    notes = "Carcass assembled. Acrylic shutter alignment in progress."
                ),
                CarpenterAssignment(
                    carpenterId = 2L,
                    clientName = "Vikram Rathore",
                    clientPhone = "+91 94140 88219",
                    siteAddress = "Sector 14, Hiran Magri, Udaipur",
                    workType = "Steel Almirah Locker & Mirror Fitting",
                    laborCharge = 7500.0,
                    advancePaid = 4000.0,
                    assignedDate = "2026-10-08",
                    targetCompletionDate = "2026-10-14",
                    status = "Inspection Pending",
                    notes = "Digital lock installation tested with client."
                ),
                CarpenterAssignment(
                    carpenterId = 3L,
                    clientName = "Kailash Choudhary",
                    clientPhone = "+91 97845 33412",
                    siteAddress = "Near Bus Stand, Nathdwara",
                    workType = "6 Waterproof PVC Bathroom Doors with Frames",
                    laborCharge = 12000.0,
                    advancePaid = 5000.0,
                    assignedDate = "2026-10-10",
                    targetCompletionDate = "2026-10-20",
                    status = "Assigned",
                    notes = "Materials dispatched to site. Hinges and handles ready."
                ),
                CarpenterAssignment(
                    carpenterId = 1L,
                    clientName = "Anil Somani",
                    clientPhone = "+91 98282 55901",
                    siteAddress = "Subhash Nagar, Bhilwara",
                    workType = "PVC Bedroom Wardrobe with Loft",
                    laborCharge = 22000.0,
                    advancePaid = 10000.0,
                    assignedDate = "2026-10-11",
                    targetCompletionDate = "2026-10-24",
                    status = "Assigned",
                    notes = "Awaiting final wall plaster before installation."
                )
            )
            database.carpenterAssignmentDao().insertAll(initialAssignments)

            // Initial attendance
            database.attendanceDao().insertAttendance(
                AttendanceRecord(
                    date = "2026-10-14",
                    checkInTime = "09:00 AM",
                    checkOutTime = "",
                    isCheckedIn = true
                )
            )
        }
    }
}
