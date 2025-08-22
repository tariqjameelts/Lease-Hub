package com.mindblowers.leasehub.ui.sc.main.dashboard.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    navController: NavController,
    shopId: String,
    shopName: String = "Jalil Mobile Shop",
    tenantName: String = "Ali Raza",
    leaseAmount: Double = 25000.0,
    dueDate: String = "15 Aug 2025",
    remainingRent: Double = 5000.0,
    advancePaid: Double = 5000.0,
    status: String = "Rented" // or "Open"
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
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
                    DetailRow("Shop Name", shopName)
                    DetailRow("Tenant", tenantName)
                    DetailRow("Lease Amount", "Rs. ${leaseAmount.toInt()}")
                    DetailRow("Remaining Rent", "Rs. ${remainingRent.toInt()}")
                    DetailRow("Advance Paid By Tenant", "Rs. ${remainingRent.toInt()}")
                    DetailRow("Due Date", dueDate)
                    DetailRow("Status", status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.navigate("add_tenant/$shopId") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Tenant")
                }
                Button(
                    onClick = { navController.navigate("payment/$shopId") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Payment")
                }
            }
        }
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
