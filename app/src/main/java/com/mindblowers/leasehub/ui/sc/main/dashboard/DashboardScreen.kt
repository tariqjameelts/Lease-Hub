package com.mindblowers.leasehub.ui.sc.main.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.ui.sc.main.dashboard.home.HomeScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.settings.SettingsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.EditShopBottomSheet
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopListScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel : DashboardViewModel = hiltViewModel(),
    list: List<String> = listOf("Lease Hub", "Shops", "Tenants", "Reports", "Settings")
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddShopSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(list[selectedTab]) },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddShopSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shop", tint = Color.White)
                }
            }
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
                    icon = { Icon(Icons.Default.Create, contentDescription = "Shops") },
                    label = { Text("Shops") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Tenants") },
                    label = { Text("Tenants") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Reports") },
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
                0 -> HomeScreen(dashboardViewModel)
                1 -> ShopListScreen(navController)
                2 -> TenantsScreen(navController)
                3 -> ReportsScreen(navController)
                4 -> SettingsScreen {
                    navController.navigate("signup") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            }
        }
        if (showAddShopSheet) {
            EditShopBottomSheet(
                onDismiss = {
                    showAddShopSheet = false
                 //   viewModel.loadShops() // reload after adding
                },
                viewModel = dashboardViewModel,

            )
        }
    }
}
