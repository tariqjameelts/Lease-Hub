package com.mindblowers.leasehub.ui.sc.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(

    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Settings Placeholder")
            Button(onClick = { navController.navigate("login") }) {
                Text("Logout")
            }
        }
    }
}
