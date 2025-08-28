package com.mindblowers.leasehub.ui.sc.main.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.R
import com.mindblowers.leasehub.data.entities.User
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
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    list: List<String> = listOf("Lease Hub", "Shops", "Tenants", "Reports", "Settings"),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddShopSheet by remember { mutableStateOf(false) }

    val currentUser by dashboardViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                currentUser = currentUser,
                selectedTab = selectedTab,
                tabTitles = list,
                onProfileClick = { navController.navigate("profile") },
                onNotificationsClick = { navController.navigate("notifications") },
                onSearchClick = { navController.navigate("search") }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
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
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.shop_ic),
                            contentDescription = "Shops",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Shops") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.tenant_ic),
                            contentDescription = "Shops",
                            modifier = Modifier.size(24.dp)
                        )
                    }, label = { Text("Tenants") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.report_ic),
                            contentDescription = "Shops",
                            modifier = Modifier.size(24.dp)
                        )
                    }, label = { Text("Reports") }
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
        Box(modifier = Modifier.padding(padding)){

            when (selectedTab) {
                0 -> HomeScreen(dashboardViewModel)
                1 -> ShopListScreen(navController)
                2 -> TenantsScreen(navController)
                3 -> ReportsScreen(navController)
                4 -> SettingsScreen {
                    navController.navigate("signin") {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    currentUser: User?,
    selectedTab: Int,
    tabTitles: List<String>,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    // Create a scroll behavior for the app bar :cite[2]:cite[3]
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // Use MediumTopAppBar for a modern look with better spacing :cite[3]
    MediumTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // User avatar and greeting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    /*// User avatar with professional styling
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "User profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )*/

                    Column {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "User",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                    }
                }

                // Current tab title with badge indicator
                TabIndicator(title = tabTitles.getOrNull(selectedTab) ?: "Dashboard")
            }
        },
        navigationIcon = {
            // App logo or navigation icon
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){

                IconButton(onClick = { /* Handle navigation menu */ }) {
                    Image(
                        painter = painterResource(R.drawable.applogo),
                        contentDescription = "LeaseHub",
                        //   tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

              //  Spacer(modifier = Modifier.width(50.dp))
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                //    modifier = Modifier.fillMaxWidth(),
                //    textAlign = TextAlign.Center
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .shadow(
                elevation = if (scrollBehavior.state.collapsedFraction > 0.1f) 4.dp else 0.dp,
                shape = MaterialTheme.shapes.medium
            )
    )
}

@Composable
fun TabIndicator(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCenterAlignedTopAppBar(
    currentUser: User?,
    selectedTab: Int,
    tabTitles: List<String>,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Hi ${currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "User"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
                Text(
                    text = tabTitles.getOrNull(selectedTab) ?: "Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(R.drawable.applogo),
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                // Badge would be added here similar to previous example
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium)
    )
}*/
