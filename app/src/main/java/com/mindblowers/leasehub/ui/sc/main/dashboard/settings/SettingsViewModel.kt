package com.mindblowers.leasehub.ui.sc.main.dashboard.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.DashboardStats
import com.mindblowers.leasehub.data.repository.SettingsRepo
import com.mindblowers.leasehub.utils.BackupManager
import com.mindblowers.leasehub.utils.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val backupManager: BackupManager,
    private val securityUtils: SecurityUtils,
    private val settingsRepo: SettingsRepo,
    private val appPrefs: AppPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
        loadBackups()
        observeThemePrefs()
        loadActiveUser() // ✅ load user on init
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

    fun changePassword() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val newPass = securityUtils.generateSecurePassword()
            val hashed = securityUtils.hashPassword(newPass)

            // ✅ update user password in DB
            val updated = user.copy(passwordHash = hashed)
            repository.updateUser(updated)

            // update state
            _uiState.value = _uiState.value.copy(user = updated)
        }
    }
/*
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
    }*/

    private fun loadDashboardStats() {
        viewModelScope.launch {
            val stats = repository.getDashboardStats()
            _uiState.value = _uiState.value.copy(dashboardStats = stats)
        }
    }

    // --- Theme Management ---
    fun setTheme(option: ThemeOption) {
        settingsRepo.setTheme(option)
    }

    fun setDynamicColor(enabled: Boolean) {
        settingsRepo.setDynamicColor(enabled)
    }

    // ✅ Load currently active user via AppPrefs + Repository
    private fun loadActiveUser() {
        viewModelScope.launch {
            val userId = appPrefs.getUserId()
            val user = if (userId != null) repository.getUserById(userId) else null
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

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch

            if (newName.isNotBlank() && newName != currentUser.username) {
                // create updated user copy
                val updatedUser = currentUser.copy(username = newName)

                // persist in database
                repository.updateUser(updatedUser)

                // update UI state
                _uiState.value = _uiState.value.copy(user = updatedUser)

                Log.d("SettingsViewModel", "User name updated to: $newName")
            } else {
                Log.w("SettingsViewModel", "Invalid or unchanged name, update skipped.")
            }
        }
    }


    // ✅ Handle Logout (clear DB + prefs + state)
    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val id = _uiState.value.user?.id
            repository.clearCurrentUser()
            repository.updateUser(_uiState.value.user?.copy(isActive = false))
            appPrefs.clearSession()
            _uiState.value = _uiState.value.copy(user = null)
            onComplete()
            Log.d("SettingsViewModel", "Logged out successfully")
        }
        Log.d("SettingsViewModel", "Logout failed")
    }



    // ---- Backups (Internal) ----
    fun createInternalBackup() {
        viewModelScope.launch {
            val f = backupManager.createInternalBackup()
            if (f != null) loadBackups()
        }
    }

    /** For restoring an internal backup picked from the list */
    fun restoreInternalBackup(file: File, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = backupManager.restoreFromInternalFile(file)
            loadBackups()
            onResult(ok)
        }
    }

    // ---- SAF Export / Import ----

    /**
     * Suggest a filename for CreateDocument (SAF). Call this before launching.
     */
    fun suggestedBackupFileName(): String = backupManager.suggestedBackupFileName()

    /**
     * Export the newest internal backup to a user-chosen SAF Uri.
     * If no internal backup exists, we create one first.
     */
    fun exportLatestBackupToUri(destUri: android.net.Uri, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            var latest = _uiState.value.backups.firstOrNull()
            if (latest == null) {
                latest = backupManager.createInternalBackup()
                if (latest != null) loadBackups()
            }
            val ok = if (latest != null) {
                backupManager.exportBackupToUri(latest, destUri)
            } else false
            onResult(ok)
        }
    }

    /**
     * Import + restore from a SAF Uri chosen by the user.
     * Pass the display name if you retrieved it (for validation).
     */
    fun importAndRestoreFromUri(
        srcUri: android.net.Uri,
        displayName: String? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ok = backupManager.restoreFromUri(srcUri, displayName)
            loadBackups()
            onResult(ok)
        }
    }

    // ---- Existing helpers unchanged ----
    fun deleteBackup(file: File) {
        backupManager.deleteInternalBackup(file)
        loadBackups()
    }

    fun getReadableSize(file: File): String =
        backupManager.getReadableFileSize(file.length())

    private fun loadBackups() {
        val backups = backupManager.getInternalBackups()
        _uiState.value = _uiState.value.copy(backups = backups)
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
