package com.mindblowers.leasehub.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "activity_log",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val message: String,
    val timestamp: Date = Date()
)
