package com.mindblowers.leasehub.ui.sc.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    list :List<String> = listOf("Lease Hub","Shops","Tenants","Reports","settings")
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(list[selectedTab]) }, modifier = Modifier.background(
                Color.Red))
        },
        floatingActionButton = {
            if (selectedTab == 1) { // Example: only show FAB on Shops
                FloatingActionButton(onClick = { navController.navigate("add_shop") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shop")
                }
            }
//            if (selectedTab == 2) { // Example: only show FAB on Shops
//                FloatingActionButton(onClick = { navController.navigate("add_tenant") }) {
//                    Icon(Icons.Default.Add, contentDescription = "Add Shop")
//                }
//            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Shops") },
                    label = { Text("Shops") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Tenants") },
                    label = { Text("Tenants") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> DashboardContent()
                1 -> ShopListScreen(navController)
                2 -> TenantsScreen(navController)
                3 -> ReportsScreen(navController)
                4 -> SettingsScreen(navController)
            }
        }
    }
}

@Composable
fun DashboardContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Summary", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryCard("Total Shops", "10", Icons.Default.Face)
            SummaryCard("Pending Rents", "3", Icons.Default.Warning)
        }
        Spacer(Modifier.height(16.dp))
        Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(listOf("Shop #1 rent paid", "Shop #4 rent pending", "Shop #2 new tenant added")) { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = activity,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}
