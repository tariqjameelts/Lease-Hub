package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mindblowers.leasehub.data.converters.DateConverter
import java.util.Date

@Entity(
    tableName = "tenants",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)

data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val fullName: String,
    val phoneNumber: String,
    val email: String? = null,
    val idType: String? = null, // Passport, Driver's License, etc.
    val idNumber: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val address: String,
    val companyName: String? = null,
    val businessType: String? = null,
    @TypeConverters(DateConverter::class)
    val createdAt: Date = Date(),
    val notes: String? = null,
    val isActive: Boolean = true
)