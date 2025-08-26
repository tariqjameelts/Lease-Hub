package com.mindblowers.leasehub.ui.sc.main.dashboard.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.data.entities.ActivityLog
import com.mindblowers.leasehub.ui.sc.components.ReportsFilterBar
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import com.mindblowers.leasehub.ui.sc.main.dashboard.DateRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityReportsScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var showPickers by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    val dateRange by viewModel.dateRange.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()

    Column(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding()
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ReportsFilterBar(
            title = "Activity Logs",
            dateRange = dateRange.label,
            onDateClick = { showPickers = true }
        )

        Spacer(Modifier.height(16.dp))

        if (showPickers) {
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
                        showPickers = false
                    }) { Text("Cancel") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        if (startDate != null && endDate != null) {
                            val s = minOf(startDate!!, endDate!!)
                            val e = maxOf(startDate!!, endDate!!)
                            viewModel.updateDateRange(DateRange.Custom(s, e))
                        }
                        showPickers = false
                    }) { Text("Apply") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activities) { activity ->
                ActivityCard(activity)
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityLog) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val timestamp = dateFormat.format(activity.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activity.message,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(6.dp))
            Divider()
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Timestamp: $timestamp",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(
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
                    Icon(Icons.Filled.DateRange, contentDescription = "Select date")
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
                            TextButton(onClick = { showPopup.value = false }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let(onDateSelected)
                                showPopup.value = false
                            }) { Text("OK") }
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
