package com.mindblowers.leasehub.ui.sc.main.dashboard.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    val context  = LocalContext.current

    // Restore confirmations
    var pendingInternalRestore by remember { mutableStateOf<File?>(null) }
    var showRestoreWarning by remember { mutableStateOf(false) }

    // ----- SAF Launchers -----

    // Export: CreateDocument
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportLatestBackupToUri(uri) { ok ->
                    // You can show a snackbar/toast via your mechanism
                    // e.g., if (!ok) show error
                }
            }
        }
    )

    // Import: OpenDocument
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val ctx = context
                val name = runCatching {
                    ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                        val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (c.moveToFirst() && nameIdx != -1) c.getString(nameIdx) else null
                    }
                }.getOrNull()

                viewModel.importAndRestoreFromUri(uri, name) { ok ->
                    // snackbar/toast if needed
                }
            }
        }
    )


    // ----- Dialogs -----
    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false; pendingInternalRestore = null },
            title = { Text("Restore backup?") },
            text = { Text("Restoring a backup will REPLACE all current app data with the backup. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val file = pendingInternalRestore
                    showRestoreWarning = false
                    if (file != null) {
                        viewModel.restoreInternalBackup(file) { ok ->
                            // snackbar/toast if needed
                        }
                    }
                    pendingInternalRestore = null
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarning = false; pendingInternalRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Edit dialog for full name ---
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

    // --- Edit dialog for username ---
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- User Profile ---
        item {
            SettingsCard(title = "User Profile", icon = Icons.Default.Info) {
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
            SettingsCard(title = "Appearance", icon = Icons.Default.Info) {
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


        // --- Database & Backup ---
        item {
            SettingsCard(title = "Database & Backup", icon = Icons.Default.Info) {
                // Internal quick backup
                SettingActionRow(
                    title = "Create Internal Backup",
                    onClick = { viewModel.createInternalBackup() }
                )

                // Export (SAF)
                SettingActionRow(
                    title = "Export Backup (Choose location)",
                    onClick = {
                        // Suggest file name
                        val fname = viewModel.suggestedBackupFileName()
                        exportLauncher.launch(fname)
                    }
                )

                // Import (SAF) -> Restore
                SettingActionRow(
                    title = "Import & Restore Backup",
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    }
                )

                if (uiState.backups.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Internal Backups", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))

                    uiState.backups.forEach { file ->
                        InternalBackupRow(
                            file = file,
                            readableSize = viewModel.getReadableSize(file),
                            onRestore = {
                                pendingInternalRestore = file
                                showRestoreWarning = true
                            },
                            onDelete = { viewModel.deleteBackup(file) }
                        )
                    }
                }
            }
        }

        // --- Account & Security ---
        item {
            SettingsCard(title = "Account & Security", icon = Icons.Default.Info) {
                /*SettingActionRow(
                    title = "Change Password",
                    onClick = { viewModel.changePassword() }
                )*/
                SettingActionRow(
                    title = "Logout",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
            }
        }

        // --- About ---
        item {
            SettingsCard(title = "About", icon = Icons.Default.Info) {
                KeyValueRow("App Version", "LeaseHub v1.0.0")
                KeyValueRow("©", "2025 Mindblowers")
            }
        }
    }
}

// --- Edit Username Dialog ---
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


// --- UI building blocks ---

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingActionRow(title: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
fun BackupRow(
    file: File,
    onDelete: () -> Unit,
    getReadableSize: () -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                getReadableSize(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Backup",
                tint = MaterialTheme.colorScheme.error
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



@Composable
private fun InternalBackupRow(
    file: File,
    readableSize: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(readableSize, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Row {
            TextButton(onClick = onRestore) { Text("Restore") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Backup", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}