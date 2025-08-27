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

    @Query("""
    SELECT * FROM activity_log
    WHERE userId = :userId
    ORDER BY timestamp DESC
    LIMIT 50
""")
    fun getRecentActivities(userId: Long): Flow<List<ActivityLog>>

    @Query("""
    SELECT * FROM activity_log 
    WHERE userId = :userId 
      AND timestamp BETWEEN :startDate AND :endDate 
    ORDER BY timestamp DESC
""")
    fun getActivitiesBetween(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<ActivityLog>>

}
