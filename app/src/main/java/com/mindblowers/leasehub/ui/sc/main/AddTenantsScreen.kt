//package com.mindblowers.leasehub.ui.sc.main
//
//
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddTenantsScreen(navController: NavHostController) {
//    var shopName by remember { mutableStateOf("") }
//    var rentAmount by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = { CenterAlignedTopAppBar(title = { Text("Add Shop") }) }
//    ) { padding ->
//        Column(Modifier.padding(padding).padding(16.dp)) {
//            OutlinedTextField(
//                value = shopName, onValueChange = { shopName = it },
//                label = { Text("Tenant Name") }, modifier = Modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = rentAmount, onValueChange = { rentAmount = it },
//                label = { Text("Phone") }, modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(16.dp))
//            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
//                Text("Save")
//            }
//        }
//    }
//}
