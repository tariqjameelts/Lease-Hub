package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "",
    val passwordHash: String= "", // Store hashed password, never plain text
    val email: String? = null,
    val fullName: String ="",
    val phoneNumber: String? = null,
    val isActive: Boolean = false,
    val createdAt: Date = Date(),
    val lastLogin: Date? = null,
    val securityQuestion: String? = null,
    val securityAnswerHash: String? = null
)