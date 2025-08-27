package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.mindblowers.leasehub.data.converters.DateConverter

@Entity(
    tableName = "lease_agreements",
    foreignKeys = [
        ForeignKey(
            entity = Shop::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),

    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["shopId"]),
        Index(value = ["tenantId"]),
        Index(value = ["agreementNumber"], unique = true)
    ]
)
data class LeaseAgreement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long ,
    val agreementNumber: String, // Unique identifier
    val shopId: Long,
    val tenantId: Long,
    @TypeConverters(DateConverter::class)
    val startDate: Date,
    @TypeConverters(DateConverter::class)
    val endDate: Date,
    val monthlyRent: Double,
    val securityDeposit: Double,
    val rentDueDay: Int, // Day of month when rent is due
    val paymentTerms: String? = null,
    val maintenanceCharges: Double = 0.0,
    val utilitiesIncluded: Boolean = false,
    val noticePeriodDays: Int = 30,
    val agreementDocumentPath: String? = null, // Path to stored document
    val status: AgreementStatus = AgreementStatus.ACTIVE,
    @TypeConverters(DateConverter::class)
    val createdAt: Date = Date(),
    val notes: String? = null
)

enum class AgreementStatus {
    ACTIVE, EXPIRED, TERMINATED, RENEWED
}