package com.mindblowers.leasehub.ui.sc.main.dashboard.reports
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindblowers.leasehub.ui.sc.components.ReportDetailCard
import com.mindblowers.leasehub.ui.sc.components.ReportsFilterBar
import com.mindblowers.leasehub.ui.sc.components.ReportsTable
import com.mindblowers.leasehub.ui.sc.components.ReportSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportsScreen() {
    val dateRange by remember { mutableStateOf("This Month") }

    Column(
        modifier = Modifier.statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp)
    ) {

        ReportsFilterBar(
            "Financial Summary",
            dateRange = dateRange,
            onDateClick = { /* Show date picker */ }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportSummaryCard("Rent Collected", "₹1,20,000", Modifier.weight(1f))
            ReportSummaryCard("Pending", "₹30,000", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        ReportSummaryCard("Net Profit", "₹85,000", Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        val transactions = listOf(
            Triple("Tenant A", "₹15,000", "Paid"),
            Triple("Tenant B", "₹12,000", "Pending"),
            Triple("Tenant C", "₹18,000", "Paid")
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(transactions) { (tenant, amount, status) ->
                ReportDetailCard(
                    title = tenant,
                    subtitle = "Amount: $amount",
                    details = listOf("Status: $status", "Date: 10-Aug-2025", "Method: Bank Transfer")
                )
            }
        }
    }
}

