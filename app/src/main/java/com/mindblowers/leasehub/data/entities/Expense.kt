package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mindblowers.leasehub.data.converters.DateConverter
import java.util.Date

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Shop::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shopId"]),
        Index(value = ["expenseDate"]),
        Index(value = ["category"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: Long? = null, // null if general expense
    val category: ExpenseCategory,
    val amount: Double,
    val description: String,
    @TypeConverters(DateConverter::class)
    val expenseDate: Date,
    val receiptPath: String? = null,
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency? = null,
    val notes: String? = null
)

enum class ExpenseCategory {
    MAINTENANCE, UTILITIES, REPAIRS, TAXES, INSURANCE, CLEANING, SECURITY, OTHER
}

enum class RecurringFrequency {
    DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
}