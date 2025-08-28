package com.mindblowers.leasehub.ui.sc.main.dashboard.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import com.mindblowers.leasehub.ui.sc.main.dashboard.DateRange
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val financialData by viewModel.financialReportsData.collectAsState()
    val dateRange by viewModel.reportsDateRange.collectAsState()

    var showDatePickers by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    // Load data when screen is shown or date range changes
    LaunchedEffect(dateRange) {
        viewModel.loadFinancialReportsData()
    }

    Scaffold(
        topBar = {
            // Filter bar with proper spacing from top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Financial Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Date filter button
                FilterChip(
                    selected = dateRange is DateRange.Custom,
                    onClick = { showDatePickers = true },
                    label = { Text(dateRange.label) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date range"
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Date pickers section
            if (showDatePickers) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text("Select Date Range", style = MaterialTheme.typography.titleMedium)

                    Spacer(Modifier.height(12.dp))

                    DatePickerDocked(
                        label = "Start Date",
                        selectedDate = startDate,
                        onDateSelected = { startDate = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    DatePickerDocked(
                        label = "End Date",
                        selectedDate = endDate,
                        onDateSelected = { endDate = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            startDate = null
                            endDate = null
                            showDatePickers = false
                        }) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (startDate != null && endDate != null) {
                                    val s = minOf(startDate!!, endDate!!)
                                    val e = maxOf(startDate!!, endDate!!)
                                    viewModel.updateReportsDateRange(DateRange.Custom(s, e))
                                }
                                showDatePickers = false
                            },
                            enabled = startDate != null && endDate != null
                        ) {
                            Text("Apply", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Summary Cards - Removed Expenses card as requested
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Rent Collected",
                    amount = "PKR ${formatAmount(financialData.totalCollected)}",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Pending Rent",
                    amount = "PKR ${formatAmount(financialData.pendingRent)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            if (financialData.recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions found for selected period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(financialData.recentTransactions) { transaction ->
                        TransactionCard(
                            tenantName = transaction.tenantName,
                            amount = transaction.amount,
                            status = transaction.status,
                            date = transaction.date,
                            paymentMethod = transaction.paymentMethod
                        )
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsDatePickerDocked(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit
) {
    val showPopup = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val formatted = selectedDate?.let { convertMillisToDate(it) } ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatted,
            onValueChange = { /* readOnly */ },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showPopup.value = !showPopup.value }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        if (showPopup.value) {
            Popup(
                onDismissRequest = { showPopup.value = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Column {
                        DatePicker(state = datePickerState, showModeToggle = false)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showPopup.value = false }) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let(onDateSelected)
                                    showPopup.value = false
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

// Simple Summary Card component
@Composable
fun SummaryCard(title: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Simple Transaction Card component
@Composable
fun TransactionCard(
    tenantName: String,
    amount: Double,
    status: String,
    date: Date,
    paymentMethod: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tenantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "PKR ${formatAmount(amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (status.equals("paid", true)) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: $status",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Method: $paymentMethod",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Utility functions
private fun formatAmount(amount: Double): String {
    return if (amount >= 100000) {
        String.format("%.1fL", amount / 100000)
    } else if (amount >= 1000) {
        String.format("%.1fK", amount / 1000)
    } else {
        String.format("%.0f", amount)
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}

// Data classes
data class FinancialReportsData(
    val totalCollected: Double = 0.0,
    val pendingRent: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<FinancialTransaction> = emptyList()
)

data class FinancialTransaction(
    val tenantName: String,
    val amount: Double,
    val status: String,
    val date: Date,
    val paymentMethod: String
)