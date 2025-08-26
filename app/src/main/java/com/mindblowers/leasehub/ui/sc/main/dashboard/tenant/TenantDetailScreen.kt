package com.mindblowers.leasehub.ui.sc.main.dashboard.tenant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import com.mindblowers.leasehub.data.entities.Tenant
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailScreen(
    navController: NavHostController,
    tenantId: Long,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
   // var tenant by remember { mutableStateOf<Tenant?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

    val tenant by dashboardViewModel.getTenantById(tenantId)
        .collectAsState(initial = null)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tenant Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (tenant != null) {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Tenant")
                        }
                        /*IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Tenant")
                        }*/
                    }
                }
            )
        }
    ) { padding ->
        tenant?.let { t ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem(label = "Full Name", value = t.fullName)
                DetailItem(label = "Phone Number", value = t.phoneNumber)
                DetailItem(label = "Address", value = t.address)
                DetailItem(label = "Company Name", value = t.companyName ?: "-")
                DetailItem(label = "Email", value = t.email ?: "-")
                DetailItem(label = "Emergency Phone", value = t.emergencyPhone ?: "-")
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Tenant") },
                text = { Text("Are you sure you want to delete this tenant? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        tenant?.let {
                            dashboardViewModel.deleteTenant(it) {
                                Toast.makeText(context, "Tenant deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditSheet && tenant != null) {
            AddTenantBottomSheet(
                tenant = tenant,  // <-- pass tenant here
                onDismiss = { showEditSheet = false },
                onSave = { updatedTenant, startDate, endDate ->
                    dashboardViewModel.addOrUpdateTenant(updatedTenant, isUpdate = true)
                    showEditSheet = false
                }
            )
        }

    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
