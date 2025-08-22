package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.mindblowers.leasehub.data.converters.DateConverter

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopNumber: String,
    val floor: Int,
    val buildingName: String,
    val address: String,
    val area: Double, // in square meters/feet
    val monthlyRent: Double,
    val securityDeposit: Double,
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