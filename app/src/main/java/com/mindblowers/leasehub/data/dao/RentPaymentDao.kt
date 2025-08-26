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
    @Query("""
    SELECT SUM(amount) 
    FROM rent_payments 
    WHERE agreementId = :agreementId AND month = :month AND year = :year
""")
    suspend fun getTotalPaidForPeriod(
        agreementId: Long,
        month: Int,
        year: Int
    ): Double


    @Transaction
    @Query("SELECT * FROM rent_payments WHERE id = :paymentId")
    suspend fun getPaymentWithAgreement(paymentId: Long): RentPaymentWithDetails  // Renamed

    @Query("SELECT * FROM rent_payments WHERE agreementId = :agreementId AND month = :month AND year = :year")
    suspend fun getPaymentForPeriod(agreementId: Long, month: Int, year: Int): RentPayment?

    @Transaction
    @Query("SELECT * FROM rent_payments WHERE agreementId = :agreementId ORDER BY year DESC, month DESC")
    fun getPaymentsWithDetailsForAgreement(agreementId: Long): Flow<List<RentPaymentWithDetails>>

    @Query("SELECT SUM(amount) FROM rent_payments WHERE agreementId = :agreementId AND year = :year")
    suspend fun getYearlyTotal(agreementId: Long, year: Int): Double

    @Query("SELECT * FROM rent_payments WHERE paymentDate BETWEEN :startDate AND :endDate")
    suspend fun getPaymentsBetweenDates(startDate: Date, endDate: Date): List<RentPayment>

    @Query("SELECT SUM(amount) FROM rent_payments WHERE year = :year AND month = :month")
    suspend fun getMonthlyRevenue(year: Int, month: Int): Double
}