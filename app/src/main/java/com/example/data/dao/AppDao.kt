package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EstimateDao {
    @Query("SELECT * FROM estimates ORDER BY id DESC")
    fun getAllEstimates(): Flow<List<Estimate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: Estimate): Long

    @Update
    suspend fun updateEstimate(estimate: Estimate)

    @Delete
    suspend fun deleteEstimate(estimate: Estimate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(estimates: List<Estimate>)
}

@Dao
interface ConfirmedOrderDao {
    @Query("SELECT * FROM confirmed_orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<ConfirmedOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: ConfirmedOrder): Long

    @Update
    suspend fun updateOrder(order: ConfirmedOrder)

    @Delete
    suspend fun deleteOrder(order: ConfirmedOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<ConfirmedOrder>)
}

@Dao
interface VisitorDao {
    @Query("SELECT * FROM visitors ORDER BY id DESC")
    fun getAllVisitors(): Flow<List<Visitor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitor(visitor: Visitor): Long

    @Update
    suspend fun updateVisitor(visitor: Visitor)

    @Delete
    suspend fun deleteVisitor(visitor: Visitor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visitors: List<Visitor>)
}

@Dao
interface CarpenterDao {
    @Query("SELECT * FROM carpenters ORDER BY totalReferrals DESC")
    fun getAllCarpenters(): Flow<List<Carpenter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarpenter(carpenter: Carpenter): Long

    @Update
    suspend fun updateCarpenter(carpenter: Carpenter)

    @Delete
    suspend fun deleteCarpenter(carpenter: Carpenter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(carpenters: List<Carpenter>)
}

@Dao
interface CarpenterAssignmentDao {
    @Query("SELECT * FROM carpenter_assignments ORDER BY id DESC")
    fun getAllAssignments(): Flow<List<CarpenterAssignment>>

    @Query("SELECT * FROM carpenter_assignments WHERE carpenterId = :carpenterId ORDER BY id DESC")
    fun getAssignmentsForCarpenter(carpenterId: Long): Flow<List<CarpenterAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: CarpenterAssignment): Long

    @Update
    suspend fun updateAssignment(assignment: CarpenterAssignment)

    @Delete
    suspend fun deleteAssignment(assignment: CarpenterAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<CarpenterAssignment>)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY id DESC LIMIT 1")
    fun getLatestAttendance(): Flow<AttendanceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)
}
