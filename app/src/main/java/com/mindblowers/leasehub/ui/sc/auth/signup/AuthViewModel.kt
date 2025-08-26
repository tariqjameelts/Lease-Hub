package com.mindblowers.leasehub.ui.sc.auth.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
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
                val userId = repository.insertUser(user)
                if (userId > 0) {
                    val savedUser = repository.getUserById(userId)
                    _userState.value = savedUser
                    savedUser?.let { appPrefs.saveUserSession(it.id) }
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
}
