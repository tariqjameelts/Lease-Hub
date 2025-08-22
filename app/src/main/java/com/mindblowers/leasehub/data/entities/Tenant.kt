package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.mindblowers.leasehub.data.converters.DateConverter

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val email: String? = null,
    val idType: String? = null, // Passport, Driver's License, etc.
    val idNumber: String? = null,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val address: String,
    val companyName: String? = null,
    val businessType: String,
    @TypeConverters(DateConverter::class)
    val createdAt: Date = Date(),
    val notes: String? = null,
    val isActive: Boolean = true
)