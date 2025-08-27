package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.mindblowers.leasehub.data.converters.DateConverter

@Entity(
    tableName = "shops",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Shop(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val shopNumber: String = "",
    val floor: Int = 0,
    val buildingName: String = "",
    val address: String = "",
    val area: Double = 0.0, // in square meters/feet
    val monthlyRent: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val amenities: String? = null, // Comma separated or JSON
    val status: ShopStatus = ShopStatus.VACANT,
    val isActive: Boolean = true,

    @TypeConverters(DateConverter::class)
    val createdAt: Date = Date(),

    val notes: String? = null
)

enum class ShopStatus {
    VACANT, OCCUPIED, UNDER_MAINTENANCE, RESERVED
}
