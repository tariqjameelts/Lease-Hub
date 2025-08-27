package com.mindblowers.leasehub.data.repository


import android.util.Log
import com.mindblowers.leasehub.data.dao.UserDao
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
import java.util.*
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val appPrefs: AppPrefs
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

    /**
     * Activate user by username
     */
    suspend fun activateUserByUsername(username: String, onResult: (Boolean) -> Unit): User? {
        val user = userDao.getUserByUsername(username)
        Log.d("usernameis", user.toString())
        if (user != null) {
            // deactivate all users first
            userDao.deactivateAllUsers()

            // update user to active and set lastLogin
            val updatedUser = user.copy(
                isActive = true,
                lastLogin = Date()
            )
            userDao.update(updatedUser)

            // Save session in prefs
            appPrefs.saveUserSession(updatedUser.id, isNewUser = false)

            onResult(true)
            return updatedUser
        }
        onResult(false)
        return null
    }
}

