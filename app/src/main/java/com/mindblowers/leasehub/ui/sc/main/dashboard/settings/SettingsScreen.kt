package com.mindblowers.leasehub.ui.sc.main.dashboard.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.R
import com.mindblowers.leasehub.data.prefs.ThemeOption
import com.mindblowers.leasehub.utils.showToast

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val operationProgress by viewModel.operationProgress.collectAsState()
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }

    // SAF Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportBackup(uri)
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importBackup(uri)
            }
        }
    )

    // Dialogs
    if (showEditNameDialog && uiState.user != null) {
        EditNameDialog(
            currentName = uiState.user!!.fullName,
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                viewModel.updateFullName(newName)
                showEditNameDialog = false
            }
        )
    }

    if (showEditUsernameDialog && uiState.user != null) {
        EditUsernameDialog(
            currentUsername = uiState.user!!.username,
            onDismiss = { showEditUsernameDialog = false },
            onSave = { newUsername ->
                viewModel.updateUserName(newUsername)
                showEditUsernameDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                onLogout()
            }
        )
    }

    // Show progress during operations
    if (uiState.isOperationInProgress) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Processing...", color = Color.White)
                operationProgress?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }

    // Show success/error messages
    LaunchedEffect(operationProgress) {
        operationProgress?.let { message ->
            if (!uiState.isOperationInProgress) {
                showToast(context, message)
                viewModel.clearProgress()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- User Profile ---
        item {
            SettingsCard(title = "User Profile", icon = R.drawable.profile_icon) {
                if (uiState.user != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Full Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Full Name", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    uiState.user!!.fullName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { showEditNameDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name"
                                )
                            }
                        }

                        // Username
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Username", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    uiState.user!!.username,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { showEditUsernameDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Username"
                                )
                            }
                        }
                    }
                } else {
                    Text("No active user", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // --- Appearance ---
        item {
            SettingsCard(title = "Appearance", icon = R.drawable.theme_icon) {
                // Theme options
                ThemeOption.entries.forEach { option ->
                    RadioSettingRow(
                        title = when (option) {
                            ThemeOption.SYSTEM -> "Use device theme"
                            ThemeOption.LIGHT -> "Light"
                            ThemeOption.DARK -> "Dark"
                        },
                        selected = uiState.themeOption == option,
                        onClick = { viewModel.setTheme(option) }
                    )
                }

                // Dynamic color toggle (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SwitchSettingRow(
                        title = "Dynamic color (Material You)",
                        checked = uiState.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        subtitle = "Match app colors to device wallpaper"
                    )
                }
            }
        }

        // --- Backup & Restore Section ---
        item {
            SettingsCard(title = "Backup & Restore", icon = R.drawable.backup_icon) {
                // Disable buttons during operations
                val enabled = !uiState.isOperationInProgress

                // Export Backup
                SettingActionRow(
                    title = "Export Backup",
                    onClick = {
                        val fileName = "leasehub_backup_${System.currentTimeMillis()}.json"
                        exportLauncher.launch(fileName)
                    },
                    enabled = enabled
                )

                // Import Backup
                SettingActionRow(
                    title = "Import Backup",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    },
                    enabled = enabled
                )

                // Information text
                Text(
                    "Exports all your data to a JSON file. Importing will merge data without losing existing information.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // --- Account & Security ---
        item {
            SettingsCard(title = "Account & Security", icon = R.drawable.account_security_icon) {
                SettingActionRow(
                    title = "Logout",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }
        }

        // --- About ---
        item {
            SettingsCard(title = "About", icon = R.drawable.about_icon) {
                KeyValueRow("App Version", "LeaseHub v1.0.0")
                KeyValueRow("©", "2025 Mindblowers")
            }
        }
    }
}

// --- UI Components ---

@Composable
fun SettingsCard(
    title: String,
    icon: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingActionRow(
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            else if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RadioSettingRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (!subtitle.isNullOrEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Full Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    val isValid = remember(username) {
        username.matches(Regex("^[a-z_]+$")) && username.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Username") },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                supportingText = {
                    Text("Only lowercase letters and underscores allowed")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(username) },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Logout") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}