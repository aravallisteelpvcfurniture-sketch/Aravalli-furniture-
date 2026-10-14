package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estimates")
data class Estimate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String = "",
    val productType: String,
    val dimensions: String = "",
    val areaSqFt: Double = 0.0,
    val ratePerSqFt: Double = 0.0,
    val hardwareCost: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val createdDate: String,
    val status: String = "Sent", // "Draft", "Sent", "Converted to Order"
    val notes: String = ""
)

@Entity(tableName = "confirmed_orders")
data class ConfirmedOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String = "",
    val productSummary: String,
    val totalAmount: Double,
    val advancePaid: Double,
    val balanceDue: Double,
    val orderDate: String,
    val deliveryDate: String,
    val stage: String = "Order Confirmed", // "Order Confirmed", "Material Cutting", "Assembling", "Quality Check", "Ready for Dispatch", "Delivered"
    val paymentMode: String = "UPI / GPay"
)

@Entity(tableName = "visitors")
data class Visitor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val visitDate: String,
    val interestedProduct: String,
    val budget: String = "Standard",
    val priority: String = "Hot", // "Hot", "Warm", "Cold"
    val followUpDate: String = "",
    val notes: String = "",
    val status: String = "Active" // "Active", "Converted", "Closed"
)

@Entity(tableName = "carpenters")
data class Carpenter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val location: String,
    val specialty: String = "PVC Modular Kitchens",
    val experienceYears: Int = 8,
    val tier: String = "Gold Mitra", // Silver, Gold, Platinum
    val totalReferrals: Int = 0,
    val pointsEarned: Int = 0,
    val totalCommissionEarned: Double = 0.0,
    val pendingPayout: Double = 0.0,
    val joinDate: String = "2026-01-10"
)

@Entity(tableName = "carpenter_assignments")
data class CarpenterAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carpenterId: Long,
    val clientName: String,
    val clientPhone: String = "",
    val siteAddress: String = "",
    val workType: String, // e.g. "PVC Kitchen Installation", "Steel Wardrobe Fitting", "PVC Bathroom Door"
    val laborCharge: Double = 0.0,
    val advancePaid: Double = 0.0,
    val assignedDate: String,
    val targetCompletionDate: String,
    val status: String = "In Progress", // "Assigned", "In Progress", "Completed"
    val notes: String = ""
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String = "",
    val isCheckedIn: Boolean = true
)
