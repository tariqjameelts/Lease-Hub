package com.mindblowers.leasehub.ui.sc.auth.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val appRepo: AppRepository,
    val appPrefs: AppPrefs
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    fun signUp(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // ✅ First deactivate any existing active users (only one active user at a time)
                repository.deactivateAllUsers()

                // ✅ Insert new user with active = true
                val activeUser = user.copy(isActive = true)
                val userId = repository.insertUser(activeUser)
                if (userId > 0) {
                    val savedUser = repository.getUserById(userId)
                    _userState.value = savedUser

                    // ✅ Save session for persistence
                    appPrefs.saveUserSession(userId, isNewUser = false)
                    setCurrentUser(userId)
                    onResult(true)
                } else {
                    _errorMessage.value = "Failed to create user"
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Expose repository method for external checks (like AppNavHost)
    suspend fun getUserById(id: Long): User? {
        return repository.getUserById(id)
    }

    // ✅ Clear current user session everywhere
    fun clearCurrentUser() {
        viewModelScope.launch {
            try {
                appPrefs.clearSession()
                appRepo.clearCurrentUser()
                _userState.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun setCurrentUser(userId: Long){
        viewModelScope.launch {
            try {
                appRepo.setCurrentUser(userId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

}
