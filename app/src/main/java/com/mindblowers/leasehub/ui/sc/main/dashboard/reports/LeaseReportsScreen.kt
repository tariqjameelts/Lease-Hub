package com.mindblowers.leasehub.ui.sc.main.dashboard.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindblowers.leasehub.ui.sc.components.ReportDetailCard
import com.mindblowers.leasehub.ui.sc.components.ReportSummaryCard
import com.mindblowers.leasehub.ui.sc.components.ReportsFilterBar

@Composable
fun LeaseReportsScreen() {
    var dateRange by remember { mutableStateOf("This Month") }

    Column(
        modifier = Modifier.statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp)
    ) {

        ReportsFilterBar(
            "Lease & Occupancy Reports",
            dateRange = dateRange,
            onDateClick = { /* Show date picker */ }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportSummaryCard("Total Shops", "12", Modifier.weight(1f))
            ReportSummaryCard("Occupied", "9", Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Text("Lease Details", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(5) { index ->
                ReportDetailCard(
                    title = "Shop ${index + 1}",
                    subtitle = "Tenant: Tenant ${index + 1}",
                    details = listOf(
                        "Status: Active",
                        "Monthly Rent: ₹${1000 + index * 100}"
                    )
                )
            }
        }
    }
}


