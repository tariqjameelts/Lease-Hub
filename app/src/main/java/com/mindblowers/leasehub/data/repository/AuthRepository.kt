package com.mindblowers.leasehub.data.repository

import com.mindblowers.leasehub.data.dao.UserDao
import com.mindblowers.leasehub.data.entities.User
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)
    }


    suspend fun deactivateAllUsers() {
        userDao.deactivateAllUsers()
    }


}
