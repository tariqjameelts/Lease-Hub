package com.mindblowers.leasehub.ui.sc.main.dashboard.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindblowers.leasehub.data.repository.RentDueReminder
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardActivity
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.dashboardStats.collectAsState()
    val reminders by viewModel.rentDueReminders.collectAsState()
    val activities by viewModel.recentActivity.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary section
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { -50 })
            ) {
                Column {
                    Text("Summary", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard("Total Shops", stats?.totalShops?.toString() ?: "-", Icons.Default.Create)
                        SummaryCard("Vacant Shops", stats?.vacantShops?.toString() ?: "-", Icons.Default.Create)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard("Active Tenants", stats?.activeTenants?.toString() ?: "-", Icons.Default.Create)
                        SummaryCard("Net Profit", stats?.netProfit?.toString() ?: "-", Icons.Default.Create)
                    }
                }
            }
        }

        // Rent Reminders
        item { Text("Rent Due Reminders", style = MaterialTheme.typography.titleMedium) }
        if (reminders.isEmpty()) {
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically()) {
                    Text("✅ No pending rents", modifier = Modifier.padding(8.dp))
                }
            }
        } else {
            itemsIndexed(reminders) { index, reminder ->
                RentReminderCard(reminder, index, visible)
            }
        }

        // Recent Activity
        item { Text("Recent Activity", style = MaterialTheme.typography.titleMedium) }
        if (activities.isEmpty()) {
            item {
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically()) {
                    Text("No recent activity yet.", modifier = Modifier.padding(8.dp))
                }
            }
        } else {
            itemsIndexed(activities) { index, activity ->
                RecentActivityCard(activity, index, visible)
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun RentReminderCard(reminder: RentDueReminder, index: Int, visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = index * 100)) +
                slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = index * 100))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Shop: ${reminder.shop.shopNumber}", style = MaterialTheme.typography.bodyLarge)
                Text("Tenant: ${reminder.tenant.fullName}")
                Text("Due: ${reminder.dueDate}")
                Text("Overdue: ${reminder.daysOverdue} days")
            }
        }
    }
}

@Composable
fun RecentActivityCard(activity: DashboardActivity, index: Int, visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = index * 100)) +
                slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = index * 100))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(activity.message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

