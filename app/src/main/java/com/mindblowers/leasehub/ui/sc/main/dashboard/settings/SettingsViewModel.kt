package com.mindblowers.leasehub.ui.sc.main.dashboard.settings

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.prefs.ThemeOption
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.DashboardStats
import com.mindblowers.leasehub.data.repository.SettingsRepo
import com.mindblowers.leasehub.utils.BackupManager
import com.mindblowers.leasehub.utils.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val backupManager: BackupManager,
    private val securityUtils: SecurityUtils,
    private val appPrefs: AppPrefs,
    private val settingsRepo: SettingsRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _operationProgress = MutableStateFlow<String?>(null)
    val operationProgress: StateFlow<String?> = _operationProgress.asStateFlow()

    private val currentUserId get() = appPrefs.getUserId() ?: -1L

    init {
        loadDashboardStats()
        loadActiveUser()
        observeThemePrefs()
    }

    private fun observeThemePrefs() {
        viewModelScope.launch {
            combine(
                settingsRepo.themeOption,
                settingsRepo.dynamicColor
            ) { theme, dynamic ->
                _uiState.value.copy(
                    themeOption = theme,
                    dynamicColor = dynamic
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    // ----- Theme Management -----
    fun setTheme(option: ThemeOption) {
        settingsRepo.setTheme(option)
    }

    fun setDynamicColor(enabled: Boolean) {
        settingsRepo.setDynamicColor(enabled)
    }

    // ----- Backup Operations -----
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOperationInProgress = true)
            val success = backupManager.exportBackupToUri(uri) { progress ->
                _operationProgress.value = progress
            }
            _uiState.value = _uiState.value.copy(isOperationInProgress = false)

            if (success) {
                _operationProgress.value = "Backup exported successfully!"
            } else {
                _operationProgress.value = "Backup export failed!"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOperationInProgress = true)
            val success = backupManager.importAndMergeFromUri(uri, currentUserId) { progress ->
                _operationProgress.value = progress
            }
            _uiState.value = _uiState.value.copy(isOperationInProgress = false)

            if (success) {
                _operationProgress.value = "Backup imported successfully!"
                // Refresh data after import
                loadDashboardStats()
                loadActiveUser()
            } else {
                _operationProgress.value = "Backup import failed!"
            }
        }
    }

    fun clearProgress() {
        _operationProgress.value = null
    }

    // ----- User Management -----
    private fun loadDashboardStats() {
        viewModelScope.launch {
            val stats = repository.getDashboardStats(currentUserId)
            _uiState.value = _uiState.value.copy(dashboardStats = stats)
        }
    }

    private fun loadActiveUser() {
        viewModelScope.launch {
            val user = if (currentUserId != -1L) repository.getUserById(currentUserId) else null
            _uiState.value = _uiState.value.copy(user = user)
        }
    }

    fun updateFullName(newName: String) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            if (newName.isNotBlank()) {
                val updated = currentUser.copy(fullName = newName)
                repository.updateUser(updated)
                _uiState.value = _uiState.value.copy(user = updated)
            }
        }
    }

    fun updateUserName(newUsername: String) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            if (newUsername.isNotBlank() && newUsername != currentUser.username) {
                val updatedUser = currentUser.copy(username = newUsername)
                repository.updateUser(updatedUser)
                _uiState.value = _uiState.value.copy(user = updatedUser)
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearCurrentUser()
            appPrefs.clearSession()
            _uiState.value = _uiState.value.copy(user = null)
            onComplete()
        }
    }
}

data class SettingsUiState(
    val user: User? = null,
    val dashboardStats: DashboardStats? = null,
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val dynamicColor: Boolean = true,
    val isOperationInProgress: Boolean = false
)