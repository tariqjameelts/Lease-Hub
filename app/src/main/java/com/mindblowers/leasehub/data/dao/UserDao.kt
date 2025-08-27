package com.mindblowers.leasehub.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mindblowers.leasehub.data.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Query("UPDATE users SET isActive = 0")
    suspend fun deactivateAllUsers()


    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET lastLogin = :loginTime WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, loginTime: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

}