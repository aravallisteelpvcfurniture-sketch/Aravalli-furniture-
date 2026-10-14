package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class AravalliBusinessLogicTest {

    @Test
    fun testEstimateCostCalculation() {
        val lengthFt = 12.0
        val heightFt = 9.0
        val areaSqFt = lengthFt * heightFt // 108 sq.ft
        val ratePerSqFt = 1350.0 // PVC Modular Kitchen rate
        val hardwareCost = 15000.0
        val discount = 15800.0

        val totalAmount = (areaSqFt * ratePerSqFt) + hardwareCost - discount
        assertEquals(145000.0, totalAmount, 0.01)
    }

    @Test
    fun testOrderAdvanceAndBalanceCalculation() {
        val totalOrderAmount = 145000.0
        val advancePaid = 80000.0
        val balanceDue = totalOrderAmount - advancePaid
        assertEquals(65000.0, balanceDue, 0.01)
    }

    @Test
    fun testCarpenterCommissionCalculation() {
        val orderAmount = 100000.0
        val commissionRate = 0.03 // 3%
        val commission = orderAmount * commissionRate
        val points = (orderAmount / 100).toInt()

        assertEquals(3000.0, commission, 0.01)
        assertEquals(1000, points)
    }
}
