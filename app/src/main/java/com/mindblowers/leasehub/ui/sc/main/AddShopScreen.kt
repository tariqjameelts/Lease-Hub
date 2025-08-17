package com.mindblowers.leasehub.ui.sc.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.ui.sc.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopScreen(navController: NavHostController) {
    var shopName by remember { mutableStateOf("") }
    var tenantName by remember { mutableStateOf("") }
    var leaseAmount by remember { mutableStateOf("") }
    var advancePaid by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Open") } // Default status

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Add Shop") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shop Name
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Tenant Name
            OutlinedTextField(
                value = tenantName,
                onValueChange = { tenantName = it },
                label = { Text("Tenant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Lease Amount
            OutlinedTextField(
                value = leaseAmount,
                onValueChange = { leaseAmount = it },
                label = { Text("Lease Amount (Rs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Paid Advance Amount
            OutlinedTextField(
                value = advancePaid,
                onValueChange = { advancePaid = it },
                label = { Text("Advance Paid By tenant (Rs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            // Status Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Open", "Rented").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }

            // Confirmation Dialog
            if (showDialog) {
                ConfirmationDialog(
                    title = "Confirm Save",
                    message = "Do you want to save this shop?",
                    onConfirm = {
                        showDialog = false
                        // TODO: Save to database (Supabase or local)
                        navController.popBackStack()
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}