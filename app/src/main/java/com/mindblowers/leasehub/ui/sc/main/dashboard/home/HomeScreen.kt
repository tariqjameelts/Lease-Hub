package com.mindblowers.leasehub.ui.sc.main.dashboard.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindblowers.leasehub.R
import com.mindblowers.leasehub.data.entities.ActivityLog
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.data.repository.RentDueReminder
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun HomeScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.dashboardStats.collectAsState()
    val reminders by viewModel.rentDueReminders.collectAsState()
    val activities by viewModel.recentActivity.collectAsState()
    val shops by viewModel.shops.collectAsState()

    var visible by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogType?>(null) }

    val (overdueReminders, upcomingReminders) = remember(reminders) {
        reminders.partition { it.daysOverdue > 0 }
    }

    LaunchedEffect(Unit) {
        visible = true
        viewModel.loadDashboardData()
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 📊 Summary section
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
                        SummaryCard(
                            "Total Shops",
                            stats?.totalShops?.toString() ?: "-",
                            painterResource(R.drawable.shop_ic)
                        ) {
                            dialogState = DialogType.TotalShops(shops)
                        }
                        SummaryCard(
                            "Vacant Shops",
                            stats?.vacantShops?.toString() ?: "-",
                            painterResource(R.drawable.lease_shop)
                        ) {
                            dialogState =
                                DialogType.VacantShops(shops.filter { it.status == ShopStatus.VACANT })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryCard(
                            "Active Tenants",
                            stats?.activeTenants?.toString() ?: "-",
                            painterResource(R.drawable.tenant_ic)
                        ) {
                            dialogState = DialogType.ActiveTenants(stats?.activeTenants ?: 0)
                        }
                        SummaryCard(
                            "Net Profit",
                            stats?.netProfit?.toString() ?: "-",
                            painterResource(R.drawable.net_profit)
                        ) {
                            dialogState = DialogType.NetProfit(stats?.netProfit ?: 0.0)
                        }
                    }
                }
            }
        }

        // 📌 Rent Reminders
// In HomeScreen.kt - Update the Rent Reminders section
        item {
            Text("Rent Due Reminders", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Shows overdue rents and upcoming dues within 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (overdueReminders.isEmpty() && upcomingReminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "No reminders",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("No rent reminders - all payments are up to date!")
                    }
                }
            }
        } else {
            // Show overdue reminders first
            if (overdueReminders.isNotEmpty()) {
                item {
                    Text(
                        "Overdue Rentals",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(overdueReminders.take(3)) { index, reminder ->
                    RentReminderCard(reminder, index, visible) {
                        dialogState = DialogType.RentReminders(reminders)
                    }
                }
            }

            // Show upcoming reminders
            if (upcomingReminders.isNotEmpty()) {
                item {
                    Text(
                        "Upcoming Rentals",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(upcomingReminders.take(3)) { index, reminder ->
                    RentReminderCard(reminder, index, visible) {
                        dialogState = DialogType.RentReminders(reminders)
                    }
                }
            }

            if (reminders.size > 3) {
                item {
                    TextButton(onClick = { dialogState = DialogType.RentReminders(reminders) }) {
                        Text("View All Reminders (${reminders.size})")
                    }
                }
            }
        }

        // 🕒 Recent Activity
        item { Text("Recent Activity", style = MaterialTheme.typography.titleMedium) }
        if (activities.isEmpty()) {
            item { Text("No recent activity yet.", modifier = Modifier.padding(8.dp)) }
        } else {
            itemsIndexed(activities.take(10)) { index, activity ->
                RecentActivityCard(activity, index, visible) {
                    dialogState = DialogType.RecentActivities(activities)
                }
            }
        }
    }

    // ✅ Render dialogs
    when (val state = dialogState) {
        is DialogType.TotalShops -> ShopsDialog("All Shops", state.shops) { dialogState = null }
        is DialogType.VacantShops -> ShopsDialog("Vacant Shops", state.shops) { dialogState = null }
        is DialogType.ActiveTenants -> InfoDialog(
            "Active Tenants",
            "${state.count} tenants active"
        ) { dialogState = null }

        is DialogType.NetProfit -> InfoDialog(
            "Net Profit",
            "Total: ₹${state.amount}"
        ) { dialogState = null }

        is DialogType.RentReminders -> RemindersDialog(state.reminders) { dialogState = null }
        is DialogType.RecentActivities -> ActivitiesDialog(state.activities) { dialogState = null }
        null -> {}
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: Painter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun RentReminderCard(reminder: RentDueReminder, index: Int, visible: Boolean, onClick: () -> Unit) {
    val isOverdue = reminder.daysOverdue > 0
    val isUpcoming = reminder.daysOverdue < 0
    val daysText = if (isOverdue) {
        "${reminder.daysOverdue} days overdue"
    } else if (isUpcoming) {
        "Due in ${-reminder.daysOverdue} days"
    } else {
        "Due today"
    }

    val cardColor = if (isOverdue) {
        MaterialTheme.colorScheme.errorContainer
    } else if (isUpcoming) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = if (isOverdue) {
        MaterialTheme.colorScheme.error
    } else if (isUpcoming) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = index * 100)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(400, delayMillis = index * 100)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(
                    if (isOverdue) Icons.Default.Warning else Icons.Default.DateRange,
                    contentDescription = "Reminder",
                    tint = iconColor
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Shop ${reminder.shop.shopNumber} - ${reminder.tenant.fullName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Amount: PKR ${String.format("%.2f", reminder.amountDue)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Period: ${reminder.period}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        daysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = iconColor
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Due: ${
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                                reminder.dueDate
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivityCard(activity: ActivityLog, index: Int, visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = index * 100)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(400, delayMillis = index * 100)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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


sealed class DialogType {
    data class TotalShops(val shops: List<Shop>) : DialogType()
    data class VacantShops(val shops: List<Shop>) : DialogType()
    data class ActiveTenants(val count: Int) : DialogType()
    data class NetProfit(val amount: Double) : DialogType()
    data class RentReminders(val reminders: List<RentDueReminder>) : DialogType()
    data class RecentActivities(val activities: List<ActivityLog>) : DialogType()
}


@Composable
fun ShopsDialog(title: String, shops: List<Shop>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .height(350.dp)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shops) { shop ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Shop #${shop.shopNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = shop.status.name.replace("_", " ")
                                    .capitalize(Locale.getDefault()),
                                style = MaterialTheme.typography.labelMedium,
                                color = when (shop.status) {
                                    ShopStatus.VACANT -> Color(0xFF388E3C)
                                    ShopStatus.OCCUPIED -> Color(0xFF1976D2)
                                    ShopStatus.RESERVED -> Color(0xFFFBC02D)
                                    ShopStatus.UNDER_MAINTENANCE -> Color(0xFFD32F2F)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun InfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

@Composable
fun RemindersDialog(reminders: List<RentDueReminder>, onDismiss: () -> Unit) {
    val (overdue, upcoming) = remember(reminders) {
        reminders.partition { it.daysOverdue > 0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rent Reminders",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .height(400.dp)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (overdue.isNotEmpty()) {
                    item {
                        Text(
                            "Overdue Rentals (${overdue.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(overdue) { reminder ->
                        ReminderItem(reminder)
                    }
                }

                if (upcoming.isNotEmpty()) {
                    item {
                        Text(
                            "Upcoming Rentals (${upcoming.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(upcoming) { reminder ->
                        ReminderItem(reminder)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun ReminderItem(reminder: RentDueReminder) {
    val isOverdue = reminder.daysOverdue > 0
    val daysText = if (isOverdue) {
        "${reminder.daysOverdue} days overdue"
    } else if (reminder.daysOverdue < 0) {
        "Due in ${-reminder.daysOverdue} days"
    } else {
        "Due today"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shop ${reminder.shop.shopNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOverdue) Color.Red else Color(0xFF388E3C),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tenant: ${reminder.tenant.fullName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Period: ${reminder.period}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Amount: PKR ${String.format("%.2f", reminder.amountDue)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Due: ${
                    SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                    ).format(reminder.dueDate)
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivitiesDialog(activities: List<ActivityLog>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recent Activities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .height(350.dp)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { act ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = act.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = SimpleDateFormat(
                                    "MMM dd",
                                    Locale.getDefault()
                                ).format(act.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
