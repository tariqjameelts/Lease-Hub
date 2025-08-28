package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.entities.RentPaymentWithDetails
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RentPaymentDao {

    @Insert
    suspend fun insert(payment: RentPayment): Long

    // ✅ Total paid in a specific period for a given agreement + user
    @Query("""
        SELECT SUM(amount) 
        FROM rent_payments 
        WHERE agreementId = :agreementId 
          AND month = :month 
          AND year = :year 
          AND userId = :userId
    """)
    suspend fun getTotalPaidForPeriod(
        agreementId: Long,
        month: Int,
        year: Int,
        userId: Long
    ): Double



    // Add this method to RentPaymentDao
    @Query("SELECT * FROM rent_payments WHERE userId = :userId")
    suspend fun getAllPaymentsForUser(userId: Long): List<RentPayment>

    @Query("SELECT * FROM rent_payments")
    suspend fun getAllForBackup(): List<RentPayment>

    @Query("SELECT * FROM rent_payments WHERE id = :id AND userId = :userId")
    suspend fun getPaymentByIdSync(id: Long, userId: Long): RentPayment?

    // ✅ Payment with agreement details (by userId too)
    @Transaction
    @Query("""
        SELECT * 
        FROM rent_payments 
        WHERE id = :paymentId 
          AND userId = :userId
    """)
    suspend fun getPaymentWithAgreement(paymentId: Long, userId: Long): RentPaymentWithDetails

    // ✅ Single payment for given period + user
    @Query("""
        SELECT * 
        FROM rent_payments 
        WHERE agreementId = :agreementId 
          AND month = :month 
          AND year = :year 
          AND userId = :userId
        LIMIT 1
    """)
    suspend fun getPaymentForPeriod(
        agreementId: Long,
        month: Int,
        year: Int,
        userId: Long
    ): RentPayment?

    // ✅ All payments for an agreement + user
    @Transaction
    @Query("""
        SELECT * 
        FROM rent_payments 
        WHERE agreementId = :agreementId 
          AND userId = :userId
        ORDER BY year DESC, month DESC
    """)
    fun getPaymentsWithDetailsForAgreement(
        agreementId: Long,
        userId: Long
    ): Flow<List<RentPaymentWithDetails>>

    // ✅ Yearly total for agreement + user
    @Query("""
        SELECT SUM(amount) 
        FROM rent_payments 
        WHERE agreementId = :agreementId 
          AND year = :year 
          AND userId = :userId
    """)
    suspend fun getYearlyTotal(
        agreementId: Long,
        year: Int,
        userId: Long
    ): Double

    // ✅ Payments between dates by user
    // In RentPaymentDao.kt - Update the existing method
    @Query("""
    SELECT * 
    FROM rent_payments 
    WHERE paymentDate BETWEEN :startDate AND :endDate
      AND userId = :userId
""")
    suspend fun getPaymentsBetweenDates(
        startDate: Date,
        endDate: Date,
        userId: Long
    ): List<RentPayment>

    // ✅ Monthly revenue by user
    @Query("""
        SELECT SUM(amount) 
        FROM rent_payments 
        WHERE year = :year 
          AND month = :month
          AND userId = :userId
    """)
    suspend fun getMonthlyRevenue(
        year: Int,
        month: Int,
        userId: Long
    ): Double
}
