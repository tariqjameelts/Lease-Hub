//package com.mindblowers.leasehub.ui.sc.main.dashboard.reports
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.mindblowers.leasehub.data.entities.Expense
//import com.mindblowers.leasehub.ui.sc.components.ReportDetailCard
//import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardViewModel
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun ExpenseReportsScreen(
//    dashboardViewModel: DashboardViewModel = hiltViewModel()
//) {
//    val expenses by dashboardViewModel.expenses.collectAsState()
//    val categorySummary by dashboardViewModel.categorySummary.collectAsState()
//    val totalExpenses by dashboardViewModel.totalExpenses.collectAsState()
//
//    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Header
//        Text(
//            text = "Expense Reports",
//            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
//        )
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // ---- TOTAL CARD ----
////        ReportDetailCard(
//            title = "Total Expenses",
//            subtitle = "For selected period",
//            details = listOf("Rs. %.2f".format(totalExpenses))
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // ---- CATEGORY SUMMARY ----
//        if (categorySummary.isNotEmpty()) {
//            ReportDetailCard(
//                title = "By Category",
//                subtitle = "Breakdown",
//                details = categorySummary.map {
//                    "${it.category}: Rs. %.2f".format(it.total)
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // ---- EXPENSE LIST ----
//        Text(
//            text = "Expense Details",
//            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        LazyColumn(modifier = Modifier.weight(1f)) {
//            items(expenses) { expense ->
//                ExpenseItem(expense, dateFormat)
//            }
//        }
//    }
//}
//
//@Composable
//private fun ExpenseItem(
//    expense: Expense,
//    dateFormat: SimpleDateFormat
//) {
//    ReportDetailCard(
//        title = expense.category.name,
//        subtitle = "Rs. %.2f".format(expense.amount),
//        details = listOf(
//            "Date: ${dateFormat.format(expense.expenseDate)}",
//            "Description: ${expense.description}",
//            expense.notes?.let { "Notes: $it" } ?: ""
//        ).filter { it.isNotBlank() }
//    )
//}
