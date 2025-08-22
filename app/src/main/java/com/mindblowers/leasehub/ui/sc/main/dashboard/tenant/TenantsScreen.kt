package com.mindblowers.leasehub.ui.sc.main.dashboard.tenant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

data class Tenant(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val purpose: String,
    val gender: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(
    navController: NavHostController,
    tenants: List<Tenant> = listOf(
        Tenant("1", "Ali Raza", "+92 300 1234567", "Main Market, Lahore", "Mobile Shop", "Male"),
        Tenant("2", "Sara Khan", "+92 301 7654321", "Bahria Town, Lahore", "Boutique", "Female"),
        Tenant("3", "Bilal Ahmed", "+92 302 9876543", "Faisal Town, Lahore", "Electronics", "Male")
    )
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tenants") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
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
                Text(tenant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    tenant.gender,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text("📞 ${tenant.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("🏠 ${tenant.address}", style = MaterialTheme.typography.bodySmall)
            Text("💼 ${tenant.purpose}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
