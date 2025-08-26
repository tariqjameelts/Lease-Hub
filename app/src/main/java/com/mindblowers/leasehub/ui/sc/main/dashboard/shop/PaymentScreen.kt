package com.mindblowers.leasehub.ui.sc.main.dashboard.shop

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.PaymentMethod
import com.mindblowers.leasehub.data.entities.PaymentStatus
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.repository.RentStatus
import com.mindblowers.leasehub.data.repository.RentSummary
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    agreement: LeaseAgreement,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val paymentDate by remember { mutableStateOf(Date()) }
    val cal = remember(paymentDate) { Calendar.getInstance().apply { time = paymentDate } }
    val payMonth = cal.get(Calendar.MONTH) + 1
    val payYear = cal.get(Calendar.YEAR)
    val isLate = cal.get(Calendar.DAY_OF_MONTH) > agreement.rentDueDay

    // Summary state from VM
    val summary by dashboardViewModel.rentSummary.collectAsState()

    // fetch summary when sheet opens
    LaunchedEffect(agreement.id) {
        dashboardViewModel.loadRentSummary(agreement.id)
    }

    // UI state
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }
    var referenceNumber by remember { mutableStateOf("") }
    var showDetails by remember { mutableStateOf(false) }

    // Prefill once we have summary
    LaunchedEffect(summary) {
        summary?.let {
            amount = it.currentRemaining.coerceAtLeast(0.0).toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Add Rent Payment", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            // Summary section
            if (summary == null) {
                CircularProgressIndicator()
            } else {
                val s = summary!!

                // Professional summary panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        SummaryRow("Current Month Due", "Rs. ${s.currentRemaining.toInt()}")
                        SummaryRow(
                            "Previous Pending",
                            "Rs. ${s.previousPendingTotal.toInt()}",
                            isAlert = s.previousPendingTotal > 0.0
                        )
                        Divider(Modifier.padding(vertical = 8.dp))
                        SummaryRow("Total Remaining", "Rs. ${s.totalRemaining.toInt()}", bold = true)
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { showDetails = true }) {
                            Text("View Details")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val parsed = input.toDoubleOrNull() ?: 0.0
                        val maxPay = s.currentRemaining.coerceAtLeast(0.0)
                        if (parsed <= maxPay) {
                            amount = input
                        } else {
                            Toast.makeText(
                                context,
                                "Cannot pay more than current month due (Rs. ${maxPay.toInt()}).",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Payment method
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = method.name,
                        onValueChange = {},
                        label = { Text("Payment Method") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        PaymentMethod.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    method = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("Reference Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                val status = when {
                    parsedAmount <= 0.0 -> PaymentStatus.PENDING
                    parsedAmount < (s.currentRemaining.coerceAtLeast(0.0)) -> PaymentStatus.PARTIAL
                    else -> PaymentStatus.PAID
                }

                Button(
                    onClick = {
                        // Final validation against current summary
                        val maxPay = s.currentRemaining.coerceAtLeast(0.0)
                        if (parsedAmount <= 0.0) {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (parsedAmount > maxPay) {
                            Toast.makeText(context, "Cannot pay more than Rs. ${maxPay.toInt()}", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val payment = RentPayment(
                            agreementId = agreement.id,
                            amount = parsedAmount,
                            paymentDate = paymentDate,
                            month = payMonth,
                            year = payYear,
                            paymentMethod = method,
                            referenceNumber = referenceNumber.ifBlank { null },
                            notes = notes.ifBlank { null },
                            isLate = isLate,
                            lateFee = 0.0,
                            status = status
                        )

                        dashboardViewModel.addRentPayment(
                            payment,
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                            onSuccess = {
                                Toast.makeText(context, "Payment Added", Toast.LENGTH_SHORT).show()
                                // refresh the summary to reflect new remaining immediately
                                dashboardViewModel.loadRentSummary(agreement.id)
                                onDismiss()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Payment")
                }
            }
        }
    }

    if (showDetails && summary != null) {
        RentDetailsDialog(
            summary = summary!!,
            onDismiss = { showDetails = false }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isAlert: Boolean = false, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun RentDetailsDialog(
    summary: RentSummary,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Rent Details") },
        text = {
            Column {
                // Header
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Month", fontWeight = FontWeight.Bold)
                    Text("Rent", fontWeight = FontWeight.Bold)
                    Text("Paid", fontWeight = FontWeight.Bold)
                    Text("Remain", fontWeight = FontWeight.Bold)
                    Text("Status", fontWeight = FontWeight.Bold)
                }
                Divider(Modifier.padding(vertical = 8.dp))

                // Rows
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    items(summary.records.size) { idx ->
                        val r = summary.records[idx]
                        val statusColor =
                            when (r.status) {
                                RentStatus.UNPAID, RentStatus.PARTIAL -> MaterialTheme.colorScheme.error
                                RentStatus.PAID -> MaterialTheme.colorScheme.primary
                            }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${monthName(r.month)} ${r.year}")
                            Text("Rs. ${r.rent.toInt()}")
                            Text("Rs. ${r.paid.toInt()}")
                            Text("Rs. ${r.remaining.toInt()}")
                            Text(r.status.name, color = statusColor)
                        }
                        Divider(Modifier.padding(vertical = 6.dp))
                    }
                }
            }
        }
    )
}

private fun monthName(m: Int): String {
    return java.text.DateFormatSymbols().months[m - 1]
}
