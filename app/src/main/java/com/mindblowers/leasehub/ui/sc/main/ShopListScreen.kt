package com.mindblowers.leasehub.ui.sc.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class Shop(
    val id: String,
    val name: String,
    val tenant: String,
    val leaseAmount: Double,
    val remainingRent: Double,
    val status: String // "Rented" or "Open"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopListScreen(navController: NavHostController) {
    val shops = listOf(
        Shop("1", "Shop 1", "Ali Raza", 25000.0, 5000.0, "Rented"),
        Shop("2", "Shop 2", "No Tenant", 20000.0, 0.0, "Open"),
        Shop("3", "Shop 3", "Bilal Khan", 30000.0, 10000.0, "Rented")
    )
        LazyColumn(
            modifier = Modifier
                .background(Color(0xFFF4F4F4))
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(shops) { shop ->
                ShopCard(shop = shop) {
                    navController.navigate("shop_detail/${shop.id}")
                }
            }
        }

}

@Composable
fun ShopCard(shop: Shop, onClick: () -> Unit) {
    val cardColor = if (shop.status == "Rented") {
        Color(0xFFE6F7F1) // Soft green
    } else {
        Color(0xFFFFF2F2) // Soft red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop details
            Column(modifier = Modifier.weight(1f)) {
                Text(shop.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Tenant: ${shop.tenant}", fontSize = 11.sp)
                Text("Lease: Rs. ${shop.leaseAmount.toInt()}", fontSize = 11.sp)
                Text(
                    "Status: ${shop.status}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (shop.status == "Rented") Color(0xFF1B5E20) else Color(0xFFB71C1C)
                )
            }

            // Remaining Rent on the right
            Text(
                "Rs. ${shop.remainingRent.toInt()}",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
