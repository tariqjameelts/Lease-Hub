package com.mindblowers.leasehub.ui.sc.main.dashboard.tenant

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mindblowers.leasehub.data.entities.Tenant
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantBottomSheet(
    tenant: Tenant? = null,
    onDismiss: () -> Unit,
    onSave: (Tenant, Date?, Date?) -> Unit
) {
    var fullName by remember { mutableStateOf(tenant?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(tenant?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(tenant?.address ?: "") }
    var companyName by remember { mutableStateOf(tenant?.companyName ?: "") }
    var email by remember { mutableStateOf(tenant?.email ?: "") }
    var emergencyPhone by remember { mutableStateOf(tenant?.emergencyPhone ?: "") }
    var startDate by remember { mutableStateOf<Date?>(Date()) }
    var endDate by remember { mutableStateOf<Date?>(Date()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val startDateState = rememberDatePickerState(
        initialSelectedDateMillis = startDate?.time ?: System.currentTimeMillis()
    )
    val endDateState = rememberDatePickerState(
        initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (tenant != null) "Update Tenant" else "Add Tenant",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = { Text("Emergency Phone (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            if (tenant == null) {
                Spacer(Modifier.height(16.dp))
                Text("Agreement Information", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = startDate?.let { dateFormatter.format(it) } ?: "",
                        onValueChange = {},
                        label = { Text("Start Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick start date")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = endDate?.let { dateFormatter.format(it) } ?: "",
                        onValueChange = {},
                        label = { Text("End Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick end date")
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedTenant = tenant?.copy(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        address = address,
                        companyName = companyName.ifBlank { null },
                        email = email.ifBlank { null },
                        emergencyPhone = emergencyPhone.ifBlank { null }
                    ) ?: Tenant(
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        address = address,
                        companyName = companyName.ifBlank { null },
                        email = email.ifBlank { null },
                        emergencyPhone = emergencyPhone.ifBlank { null }
                    )

                    if (tenant == null) {
                        onSave(updatedTenant, startDate, endDate)
                    } else {
                        onSave(updatedTenant, null, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fullName.isNotBlank() && phoneNumber.isNotBlank() && address.isNotBlank()
            ) {
                Text(if (tenant != null) "Update Tenant" else "Add Tenant")
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateState.selectedDateMillis?.let { startDate = Date(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDateState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateState.selectedDateMillis?.let { endDate = Date(it) }
                    Log.d("endDate", endDate.toString())
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDateState) }
    }
}
