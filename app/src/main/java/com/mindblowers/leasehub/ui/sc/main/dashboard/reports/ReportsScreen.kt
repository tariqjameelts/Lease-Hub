package com.mindblowers.leasehub.ui.sc.main.dashboard.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavHostController) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Select a Report Category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            val reportCategories = listOf(
                Triple("Financial Reports", "View rent, income vs expense", ReportsNavRoutes.Financial.route),
                Triple("Lease & Occupancy", "Shop occupancy & lease expiry", ReportsNavRoutes.Lease.route),
                Triple("Activity Reports", "User & system activity logs", ReportsNavRoutes.Activity.route)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reportCategories) { (title, desc, route) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(route) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(title, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

}

sealed class ReportsNavRoutes(val route: String) {
    object Dashboard : ReportsNavRoutes("reports_dashboard")
    object Financial : ReportsNavRoutes("financial_reports")
    object Lease : ReportsNavRoutes("lease_reports")
    object Activity : ReportsNavRoutes("activity_reports")
}
