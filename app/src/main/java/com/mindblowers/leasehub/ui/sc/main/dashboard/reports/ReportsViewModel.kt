package com.mindblowers.leasehub.ui.sc.main.dashboard.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FinancialReportState(
    val dateRange: String = "Jan 2025",
    val shops: List<String> = listOf("All", "Shop A", "Shop B"),
    val selectedShop: String = "All",
    val totalRent: Double = 50000.0,
    val outstanding: Double = 8000.0,
    val rentRows: List<List<String>> = listOf(
        listOf("John Doe", "Shop A", "Jan", "₹20000", "Paid"),
        listOf("Jane Smith", "Shop B", "Jan", "₹15000", "Pending")
    )
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
//    private val repository: ReportsRepository
) : ViewModel() {
    private val _financialReportState = MutableStateFlow(FinancialReportState())
    val financialReportState = _financialReportState.asStateFlow()

    fun onShopSelected(shop: String) {
        _financialReportState.update { it.copy(selectedShop = shop) }
    }
}
