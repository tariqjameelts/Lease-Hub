package com.mindblowers.leasehub.ui.sc.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavHostController, shopId: String) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Payment for $shopId") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Amount") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = method, onValueChange = { method = it },
                label = { Text("Payment Method") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}


