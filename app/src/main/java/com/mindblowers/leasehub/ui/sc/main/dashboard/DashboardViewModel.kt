package com.mindblowers.leasehub.ui.sc.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.DashboardStats
import com.mindblowers.leasehub.data.repository.RentDueReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardActivity(
    val message: String,
    val timestamp: Date
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats

    private val _rentDueReminders = MutableStateFlow<List<RentDueReminder>>(emptyList())
    val rentDueReminders: StateFlow<List<RentDueReminder>> = _rentDueReminders

    private val _recentActivity = MutableStateFlow<List<DashboardActivity>>(emptyList())
    val recentActivity: StateFlow<List<DashboardActivity>> = _recentActivity

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    val vacantShops: StateFlow<List<Shop>> = shops.map { list ->
        list.filter { it.status == ShopStatus.VACANT }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val occupiedShops: StateFlow<List<Shop>> = shops.map { list ->
        list.filter { it.status == ShopStatus.OCCUPIED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val maintenanceShops: StateFlow<List<Shop>> = shops.map { list ->
        list.filter { it.status == ShopStatus.UNDER_MAINTENANCE }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reservedShops: StateFlow<List<Shop>> = shops.map { list ->
        list.filter { it.status == ShopStatus.RESERVED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadDashboardData()
        loadShops()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _dashboardStats.value = repository.getDashboardStats()
                _rentDueReminders.value = repository.getRentDueReminders()

                // Temporary mock activity log (replace with DB-driven later)
                val now = Date()
                val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                _recentActivity.value = listOf(
                    DashboardActivity("Shop #1 rent paid", now),
                    DashboardActivity("Shop #4 rent pending", Date(now.time - 86400000L)),
                    DashboardActivity("New tenant added in Shop #2", Date(now.time - 2 * 86400000L))
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadShops() {
        viewModelScope.launch {
            repository.getAllShops()
                .collect { shopList ->
                    _shops.value = shopList
                }
        }
    }

    fun addShop(shop: Shop) {
        viewModelScope.launch {
            repository.insertShop(shop)
            loadShops()
            addActivity("New shop '${shop.shopNumber}' added")
        }
    }


    fun updateShopStatus(shopId: Long, status: ShopStatus) {
        viewModelScope.launch {
            repository.updateShopStatus(shopId, status)
            loadShops()
            addActivity("Shop #$shopId status updated to $status")
        }
    }

    private fun addActivity(message: String) {
        _recentActivity.update { old ->
            listOf(DashboardActivity(message, Date())) + old.take(19) // keep last 20
        }
    }
}
