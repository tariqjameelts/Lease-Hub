package com.mindblowers.leasehub.ui.sc.main.dashboard.shop

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.AddTenantBottomSheet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    navController: NavController,
    shopId: Long,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val shop by viewModel.shop.collectAsState()
    val activeAgreement by viewModel.activeAgreement.collectAsState()
    val tenantName by viewModel.activeTenantName.collectAsState() // <- NEW STATEFLOW for tenant name
    var showTenantSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showAgreementDialog by remember { mutableStateOf(false) }
    var showDeleteShopDialog by remember { mutableStateOf(false) }
    var showEditShopSheet by remember { mutableStateOf(false) }
    var showRemoveAgreementConfirm by remember { mutableStateOf(false) }
    var remainingRent by remember { mutableDoubleStateOf(0.0) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // fetch shop + agreement
    LaunchedEffect(shopId) {
        viewModel.getShopById(shopId)
        viewModel.refreshActiveAgreement(shopId)
        viewModel.refreshActiveTenantName(shopId) // <-- ADD THIS
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    shop?.let {
                        IconButton(onClick = { showDeleteShopDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Shop")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shop Info Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shop?.let {
                        DetailRow("Shop Number", it.shopNumber)
                        DetailRow("Floor", it.floor.toString())
                        DetailRow("Building", it.buildingName)
                        DetailRow("Address", it.address)
                        DetailRow("Area", "${it.area} sqft")
                        DetailRow("Monthly Rent", "Rs. ${it.monthlyRent.toInt()}")
                        if (it.status == ShopStatus.OCCUPIED) {
                            DetailRow("Security Deposit", "Rs. ${it.securityDeposit.toInt()}")
                        }
                        DetailRow("Status", it.status.name)
                        it.amenities?.let { amenity -> DetailRow("Amenities", amenity) }
                        it.notes?.let { note -> DetailRow("Notes", note) }
                        TextButton(
                            onClick = { showEditShopSheet = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Edit Shop Details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Blue
                            )
                        }

                    }

                    if (activeAgreement != null) {
                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(12.dp))
                        Text("Active Agreement", fontWeight = FontWeight.Bold)
                        DetailRow("Agreement No.", activeAgreement!!.agreementNumber)
                        DetailRow("Tenant", tenantName ?: "Unknown") // SHOW NAME, not ID
                        DetailRow("Monthly Rent", "Rs. ${activeAgreement!!.monthlyRent.toInt()}")
                        activeAgreement!!.startDate.let {
                            DetailRow("Start Date", dateFormatter.format(it))
                        }
                        activeAgreement!!.endDate.let {
                            DetailRow("End Date", dateFormatter.format(it))
                        }
                        DetailRow("Status", activeAgreement!!.status.name)

                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showAgreementDialog = true }) {
                            Text("View Agreement Details")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { showTenantSheet = true },
                    modifier = Modifier.weight(1f),
                    enabled = shop?.status != ShopStatus.OCCUPIED,
                    colors = ButtonDefaults.buttonColors(
                        if (shop?.status != ShopStatus.OCCUPIED) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("Assign Tenant")
                }
                Button(
                    onClick = { showPaymentSheet = true },
                    modifier = Modifier.weight(1f),
                    enabled = activeAgreement != null,
                    colors = ButtonDefaults.buttonColors(
                        if (activeAgreement != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("Add Payment")
                }
            }

            // Add Tenant BottomSheet
            if (showTenantSheet) {
                AddTenantBottomSheet(
              //      tenant = , // pass tenant if editing, null if adding
                    onDismiss = { showTenantSheet = false },
                    onSave = { tenant, startDate, endDate ->
                        val s = shop
                        if (s != null) {
                            if (startDate != null && endDate != null) {
                                // Adding new tenant + agreement
                                viewModel.assignTenantToShop(s.id, tenant, startDate, endDate)
                            } else {
                                // Updating existing tenant
                                viewModel.addOrUpdateTenant(tenant, isUpdate = true)
                            }
                            showTenantSheet = false
                        }
                    }
                )
            }


            // Payment BottomSheet
            if (showPaymentSheet && activeAgreement != null) {
                PaymentBottomSheet(
                    agreement = activeAgreement!!,
                    dashboardViewModel = viewModel,
                    onDismiss = {
                        showPaymentSheet = false
                        viewModel.refreshActiveAgreement(shopId)
                    }
                )
            }
        }
    }

    // Agreement Details Dialog
    if (showAgreementDialog && activeAgreement != null) {
        AlertDialog(
            onDismissRequest = { showAgreementDialog = false },
            title = { Text("Agreement Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Agreement No.", activeAgreement!!.agreementNumber)
                    DetailRow("Tenant", tenantName ?: "Unknown")
                    DetailRow("Monthly Rent", "Rs. ${activeAgreement!!.monthlyRent.toInt()}")
                    activeAgreement!!.startDate?.let {
                        DetailRow("Start Date", dateFormatter.format(it))
                    }
                    activeAgreement!!.endDate?.let {
                        DetailRow("End Date", dateFormatter.format(it))
                    }
                    DetailRow("Status", activeAgreement!!.status.name)
                }
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        showAgreementDialog = false
                        // Open Date Picker initialized with current endDate
                        val calendar = Calendar.getInstance()
                        activeAgreement!!.endDate?.let { calendar.time = it }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val newDate = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }.time
                                viewModel.renewAgreement(activeAgreement!!.id, newDate)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Text("Renew Agreement")
                    }
                    TextButton(
                        onClick = {
                            showAgreementDialog = false
                            viewModel.viewModelScope.launch {
                                val remaining = viewModel.getRemainingRent(
                                    activeAgreement!!.id,
                                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                                    Calendar.getInstance().get(Calendar.YEAR)
                                )
                                remainingRent = remaining
                                showRemoveAgreementConfirm = true
                            }
                        }
                    ) {
                        Text("Remove Agreement", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAgreementDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Confirm Remove Agreement Dialog
    if (showRemoveAgreementConfirm && activeAgreement != null) {
        AlertDialog(
            onDismissRequest = { showRemoveAgreementConfirm = false },
            title = { Text("Confirm Removal") },
            text = {
                Text("Removing this agreement will free the shop.\nRemaining rent: Rs. $remainingRent.\nAre you sure?")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeAgreement(activeAgreement!!.id)
                    showRemoveAgreementConfirm = false
                    viewModel.getShopById(shopId)
                    viewModel.refreshActiveAgreement(shopId)
                }) {
                    Text("Proceed", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAgreementConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditShopSheet && shop != null) {
        EditShopBottomSheet(
            shop = shop!!,
            onDismiss = { showEditShopSheet = false }
        )
    }

    // Confirm Delete Shop Dialog
    if (showDeleteShopDialog && shop != null) {
        AlertDialog(
            onDismissRequest = { showDeleteShopDialog = false },
            title = { Text("Delete Shop") },
            text = { Text("Are you sure you want to delete this shop? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteShop(shop!!)
                    showDeleteShopDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteShopDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.DarkGray)
    }
}
