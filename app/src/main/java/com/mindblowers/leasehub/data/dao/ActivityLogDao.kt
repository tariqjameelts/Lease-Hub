package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mindblowers.leasehub.data.entities.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert
    suspend fun insert(log: ActivityLog)

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT 50")
    fun getRecentActivities(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_log WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getActivitiesBetween(startDate: Long, endDate: Long): Flow<List<ActivityLog>>

}
