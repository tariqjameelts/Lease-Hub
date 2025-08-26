package com.mindblowers.leasehub.ui.sc.main.dashboard.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.ui.sc.components.ConfirmationDialog
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopBottomSheet(
    shop: Shop = Shop(), // default empty means "Add"
    onDismiss: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // Check if this is a new shop or existing one
    val isNewShop = shop.id == 0L

    // Form state initialized with shop’s values
    var shopNumber by remember { mutableStateOf(shop.shopNumber) }
    var floor by remember { mutableStateOf(shop.floor.toString()) }
    var buildingName by remember { mutableStateOf(shop.buildingName) }
    var address by remember { mutableStateOf(shop.address) }
    var area by remember { mutableStateOf(shop.area.toString()) }
    var monthlyRent by remember { mutableStateOf(shop.monthlyRent.toString()) }
    var securityDeposit by remember { mutableStateOf(shop.securityDeposit.toString()) }
    var amenities by remember { mutableStateOf(shop.amenities ?: "") }
    var notes by remember { mutableStateOf(shop.notes ?: "") }
    var showDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetState = bottomSheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ✅ Dynamic title
            item {
                Text(
                    if (isNewShop) "Add Shop" else "Edit Shop",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // --- Text fields remain same as before ---
            item {
                OutlinedTextField(
                    value = shopNumber,
                    onValueChange = { shopNumber = it },
                    label = { Text("Shop Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = buildingName,
                    onValueChange = { buildingName = it },
                    label = { Text("Building Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area (sq. ft)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = monthlyRent,
                    onValueChange = { monthlyRent = it },
                    label = { Text("Monthly Rent (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = securityDeposit,
                    onValueChange = { securityDeposit = it },
                    label = { Text("Security Deposit (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = amenities,
                    onValueChange = { amenities = it },
                    label = { Text("Amenities (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ✅ Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (shopNumber.isNotBlank() && floor.isNotBlank()) {
                                showDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (isNewShop) "Add" else "Update",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ✅ Dynamic dialog
        if (showDialog) {
            ConfirmationDialog(
                title = if (isNewShop) "Confirm Add" else "Confirm Update",
                message = if (isNewShop)
                    "Do you want to add this shop?"
                else
                    "Do you want to update this shop?",
                onConfirm = {
                    showDialog = false
                    val updatedShop = shop.copy(
                        shopNumber = shopNumber.trim(),
                        floor = floor.toIntOrNull() ?: 0,
                        buildingName = buildingName.trim(),
                        address = address.trim(),
                        area = area.toDoubleOrNull() ?: 0.0,
                        monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
                        securityDeposit = securityDeposit.toDoubleOrNull() ?: 0.0,
                        amenities = amenities.takeIf { it.isNotBlank() },
                        notes = notes.takeIf { it.isNotBlank() },
                        createdAt = shop.createdAt // preserve original createdAt
                    )
                    if (isNewShop) {
                        viewModel.addShop(updatedShop)
                    } else {
                        viewModel.updateShop(updatedShop)
                    }
                    onDismiss()
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}
