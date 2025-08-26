package com.mindblowers.leasehub.ui.sc.main.dashboard.tenant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.data.entities.Tenant
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel

@Composable
fun TenantsScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val tenants by viewModel.tenants.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadTenants() }

    Scaffold(
        topBar = { /* same top bar */ }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tenants) { tenant ->
                TenantCard(tenant) {
                    navController.navigate("tenant_detail/${tenant.id}")
                }
            }
        }
    }
}

@Composable
fun TenantCard(tenant: Tenant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    tenant.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Optional: show company name if exists
                tenant.companyName?.let { company ->
                    Text(
                        company,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text("📞 ${tenant.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("🏠 ${tenant.address}", style = MaterialTheme.typography.bodySmall)
            tenant.email?.let { email ->
                Text("✉️ $email", style = MaterialTheme.typography.bodySmall)
            }
            tenant.emergencyPhone?.let { emergency ->
                Text("📱 Emergency: $emergency", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

