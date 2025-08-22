package com.mindblowers.leasehub.ui.sc.main.dashboard.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.DashboardStats
import com.mindblowers.leasehub.utils.BackupManager
import com.mindblowers.leasehub.utils.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val backupManager: BackupManager,
    private val securityUtils: SecurityUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadDashboardStats()
        loadBackups()
    }

    fun changePassword() {
        // For demo: generate secure password and update current user
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val newPass = securityUtils.generateSecurePassword()
            val hashed = securityUtils.hashPassword(newPass)
            repository.createUser(user.copy(passwordHash = hashed)) // simplistic; should update
            // Ideally emit an event to show new password or send via secure channel
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            val file = backupManager.createBackup()
            if (file != null) loadBackups()
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val backups = backupManager.getAvailableBackups()
            if (backups.isNotEmpty()) {
                val file = backups.first()
                val uri = android.net.Uri.fromFile(file)
                backupManager.restoreBackup(uri)
                loadBackups()
            }
        }
    }

    fun deleteBackup(file: File) {
        backupManager.deleteBackup(file)
        loadBackups()
    }

    fun getReadableSize(file: File): String = backupManager.getReadableFileSize(file.length())

    private fun loadDashboardStats() {
        viewModelScope.launch {
            val stats = repository.getDashboardStats()
            _uiState.value = _uiState.value.copy(dashboardStats = stats)
        }
    }

    private fun loadBackups() {
        val backups = backupManager.getAvailableBackups()
        _uiState.value = _uiState.value.copy(backups = backups)
    }

    // --- Theme Management ---
    fun setTheme(option: ThemeOption) {
        _uiState.value = _uiState.value.copy(themeOption = option)
        // TODO: Persist theme option (e.g., DataStore)
    }

    fun setDynamicColor(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(dynamicColor = enabled)
        // TODO: Persist dynamic color preference (e.g., DataStore)
    }
}

data class SettingsUiState(
    val user: User? = null,
    val backups: List<File> = emptyList(),
    val dashboardStats: DashboardStats? = null,
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val dynamicColor: Boolean = true
)

enum class ThemeOption { SYSTEM, LIGHT, DARK }
