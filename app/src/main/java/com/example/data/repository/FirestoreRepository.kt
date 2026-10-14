package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class FurnitureEstimateItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "PVC Modular Kitchen",
    val dimensions: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0
)

class FirestoreRepository(private val context: Context) {
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    private val auth by lazy { Firebase.auth }

    val currentUser get() = auth.currentUser

    fun requireUserId(): String {
        return auth.currentUser?.uid ?: "unauthenticated"
    }

    suspend fun saveEstimateToFirestore(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        items: List<FurnitureEstimateItem>,
        hardwareCost: Double,
        discountAmount: Double,
        totalAmount: Double,
        notes: String
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google to save directly to Firestore."))

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val estimateDocId = "EST-${System.currentTimeMillis()}"

        val itemsData = items.map { item ->
            mapOf(
                "id" to item.id,
                "name" to item.name,
                "category" to item.category,
                "dimensions" to item.dimensions,
                "quantity" to item.quantity,
                "unitPrice" to item.unitPrice,
                "subtotal" to item.subtotal
            )
        }

        val primaryCategory = items.firstOrNull()?.category ?: "Custom PVC/Steel Furniture"

        val map = hashMapOf(
            "id" to estimateDocId,
            "userId" to uid,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "customerAddress" to customerAddress,
            "productType" to primaryCategory,
            "dimensions" to items.joinToString(", ") { "${it.name} (${it.dimensions})" },
            "areaSqFt" to 0.0,
            "ratePerSqFt" to 0.0,
            "hardwareCost" to hardwareCost,
            "discountAmount" to discountAmount,
            "totalAmount" to totalAmount,
            "createdDate" to dateFormat.format(Date()),
            "status" to "Sent",
            "items" to itemsData,
            "notes" to notes
        )

        return try {
            db.collection("users").document(uid).collection("estimates")
                .document(estimateDocId)
                .set(map)
                .await()
            Result.success(estimateDocId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCarpenterToFirestore(
        carpenter: Carpenter,
        assignments: List<CarpenterAssignment> = emptyList()
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google to save to Firestore."))

        val docId = carpenter.id.toString()
        val carpenterMap = hashMapOf(
            "id" to docId,
            "userId" to uid,
            "name" to carpenter.name,
            "phone" to carpenter.phone,
            "location" to carpenter.location,
            "specialty" to carpenter.specialty,
            "experienceYears" to carpenter.experienceYears,
            "tier" to carpenter.tier,
            "totalReferrals" to carpenter.totalReferrals,
            "pointsEarned" to carpenter.pointsEarned,
            "totalCommissionEarned" to carpenter.totalCommissionEarned,
            "pendingPayout" to carpenter.pendingPayout,
            "joinDate" to carpenter.joinDate,
            "activeProjectsCount" to assignments.count { it.status != "Completed" }
        )

        return try {
            val carpRef = db.collection("users").document(uid).collection("carpenters").document(docId)
            carpRef.set(carpenterMap).await()

            for (assignment in assignments) {
                val assignId = assignment.id.toString()
                val assignMap = hashMapOf(
                    "id" to assignId,
                    "carpenterId" to docId,
                    "clientName" to assignment.clientName,
                    "clientPhone" to assignment.clientPhone,
                    "siteAddress" to assignment.siteAddress,
                    "workType" to assignment.workType,
                    "laborCharge" to assignment.laborCharge,
                    "advancePaid" to assignment.advancePaid,
                    "assignedDate" to assignment.assignedDate,
                    "targetCompletionDate" to assignment.targetCompletionDate,
                    "status" to assignment.status,
                    "notes" to assignment.notes
                )
                carpRef.collection("projects").document(assignId).set(assignMap).await()
            }
            Result.success(docId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveAssignmentToFirestore(
        carpenterId: Long,
        assignment: CarpenterAssignment
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in to save assignment."))

        val assignId = assignment.id.toString()
        val assignMap = hashMapOf(
            "id" to assignId,
            "carpenterId" to carpenterId.toString(),
            "clientName" to assignment.clientName,
            "clientPhone" to assignment.clientPhone,
            "siteAddress" to assignment.siteAddress,
            "workType" to assignment.workType,
            "laborCharge" to assignment.laborCharge,
            "advancePaid" to assignment.advancePaid,
            "assignedDate" to assignment.assignedDate,
            "targetCompletionDate" to assignment.targetCompletionDate,
            "status" to assignment.status,
            "notes" to assignment.notes
        )

        return try {
            db.collection("users").document(uid)
                .collection("carpenters").document(carpenterId.toString())
                .collection("projects").document(assignId)
                .set(assignMap)
                .await()
            Result.success(assignId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAssignmentStatusInFirestore(
        carpenterId: Long,
        assignmentId: Long,
        newStatus: String,
        advancePaid: Double? = null
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not signed in."))

        return try {
            val updateMap = hashMapOf<String, Any>("status" to newStatus)
            if (advancePaid != null) {
                updateMap["advancePaid"] = advancePaid
            }
            db.collection("users").document(uid)
                .collection("carpenters").document(carpenterId.toString())
                .collection("projects").document(assignmentId.toString())
                .update(updateMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllCarpentersAndAssignments(
        carpenters: List<Carpenter>,
        assignments: List<CarpenterAssignment>
    ): Result<Int> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google to sync to Cloud."))

        return try {
            var syncedCount = 0
            for (carp in carpenters) {
                val carpAssignments = assignments.filter { it.carpenterId == carp.id }
                saveCarpenterToFirestore(carp, carpAssignments)
                syncedCount++
            }
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncEstimate(estimate: Estimate) {
        val uid = auth.currentUser?.uid ?: return
        val map = hashMapOf(
            "userId" to uid,
            "customerName" to estimate.customerName,
            "customerPhone" to estimate.customerPhone,
            "customerAddress" to estimate.customerAddress,
            "productType" to estimate.productType,
            "dimensions" to estimate.dimensions,
            "areaSqFt" to estimate.areaSqFt,
            "ratePerSqFt" to estimate.ratePerSqFt,
            "hardwareCost" to estimate.hardwareCost,
            "discountAmount" to estimate.discountAmount,
            "totalAmount" to estimate.totalAmount,
            "createdDate" to estimate.createdDate,
            "status" to estimate.status,
            "notes" to estimate.notes
        )
        db.collection("users").document(uid).collection("estimates")
            .document(estimate.id.toString())
            .set(map)
            .await()
    }

    suspend fun syncOrder(order: ConfirmedOrder) {
        val uid = auth.currentUser?.uid ?: return
        val map = hashMapOf(
            "userId" to uid,
            "orderNumber" to order.orderNumber,
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "deliveryAddress" to order.deliveryAddress,
            "productSummary" to order.productSummary,
            "totalAmount" to order.totalAmount,
            "advancePaid" to order.advancePaid,
            "balanceDue" to order.balanceDue,
            "orderDate" to order.orderDate,
            "deliveryDate" to order.deliveryDate,
            "stage" to order.stage,
            "paymentMode" to order.paymentMode
        )
        db.collection("users").document(uid).collection("orders")
            .document(order.id.toString())
            .set(map)
            .await()
    }

    suspend fun syncVisitor(visitor: Visitor) {
        val uid = auth.currentUser?.uid ?: return
        val map = hashMapOf(
            "userId" to uid,
            "name" to visitor.name,
            "phone" to visitor.phone,
            "address" to visitor.address,
            "visitDate" to visitor.visitDate,
            "interestedProduct" to visitor.interestedProduct,
            "budget" to visitor.budget,
            "priority" to visitor.priority,
            "followUpDate" to visitor.followUpDate,
            "notes" to visitor.notes,
            "status" to visitor.status
        )
        db.collection("users").document(uid).collection("visitors")
            .document(visitor.id.toString())
            .set(map)
            .await()
    }

    suspend fun syncCarpenter(carpenter: Carpenter) {
        val uid = auth.currentUser?.uid ?: return
        val map = hashMapOf(
            "userId" to uid,
            "name" to carpenter.name,
            "phone" to carpenter.phone,
            "location" to carpenter.location,
            "specialty" to carpenter.specialty,
            "experienceYears" to carpenter.experienceYears,
            "tier" to carpenter.tier,
            "totalReferrals" to carpenter.totalReferrals,
            "pointsEarned" to carpenter.pointsEarned,
            "totalCommissionEarned" to carpenter.totalCommissionEarned,
            "pendingPayout" to carpenter.pendingPayout,
            "joinDate" to carpenter.joinDate
        )
        db.collection("users").document(uid).collection("carpenters")
            .document(carpenter.id.toString())
            .set(map)
            .await()
    }
}
