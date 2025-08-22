package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.mindblowers.leasehub.data.converters.DateConverter

@Entity(
    tableName = "rent_payments",
    foreignKeys = [
        ForeignKey(
            entity = LeaseAgreement::class,
            parentColumns = ["id"],
            childColumns = ["agreementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agreementId"]),
        Index(value = ["paymentDate"]),
        Index(value = ["month", "year"])
    ]
)
data class RentPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val agreementId: Long,
    val amount: Double,
    @TypeConverters(DateConverter::class)
    val paymentDate: Date,
    val month: Int,
    val year: Int,
    val paymentMethod: PaymentMethod,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val isLate: Boolean = false,
    val lateFee: Double = 0.0,
    val status: PaymentStatus = PaymentStatus.PAID
)

enum class PaymentMethod {
    CASH, BANK_TRANSFER, CHEQUE, DIGITAL_WALLET
}

enum class PaymentStatus {
    PAID, PENDING, PARTIAL, OVERDUE
}