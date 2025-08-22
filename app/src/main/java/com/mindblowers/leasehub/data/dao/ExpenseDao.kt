package com.mindblowers.leasehub.data.dao

import androidx.room.*
import com.mindblowers.leasehub.data.entities.Expense
import com.mindblowers.leasehub.data.entities.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE shopId = :shopId ORDER BY expenseDate DESC")
    fun getExpensesForShop(shopId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate")
    suspend fun getExpensesBetweenDates(startDate: Date, endDate: Date): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesBetweenDates(startDate: Date, endDate: Date): Double

    // FIXED: Use a data class for the result instead of Map
    data class ExpenseByCategory(
        val category: ExpenseCategory,
        val total: Double
    )

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate GROUP BY category")
    suspend fun getExpensesByCategory(startDate: Date, endDate: Date): List<ExpenseByCategory>
}