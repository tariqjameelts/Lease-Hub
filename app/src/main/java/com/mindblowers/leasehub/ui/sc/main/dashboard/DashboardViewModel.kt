package com.mindblowers.leasehub.ui.sc.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindblowers.leasehub.data.dao.ExpenseDao
import com.mindblowers.leasehub.data.entities.ActivityLog
import com.mindblowers.leasehub.data.entities.Expense
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.data.entities.Tenant
import com.mindblowers.leasehub.data.entities.User
import com.mindblowers.leasehub.data.prefs.AppPrefs
import com.mindblowers.leasehub.data.repository.AppRepository
import com.mindblowers.leasehub.data.repository.DashboardStats
import com.mindblowers.leasehub.data.repository.RentDueReminder
import com.mindblowers.leasehub.data.repository.RentSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AppRepository,
    private val appPrefs: AppPrefs
) : ViewModel() {


    val userId = appPrefs.getUserId()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser


    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _categorySummary = MutableStateFlow<List<ExpenseDao.ExpenseByCategory>>(emptyList())
    val categorySummary: StateFlow<List<ExpenseDao.ExpenseByCategory>> = _categorySummary

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses

    // ---- StateFlows for UI ----
    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants

    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats

    private val _rentDueReminders = MutableStateFlow<List<RentDueReminder>>(emptyList())
    val rentDueReminders: StateFlow<List<RentDueReminder>> = _rentDueReminders

    private val _recentActivity = MutableStateFlow<List<ActivityLog>>(emptyList())
    val recentActivity: StateFlow<List<ActivityLog>> = _recentActivity

    private val _recentActivities = MutableStateFlow<List<ActivityLog>>(emptyList())
    val recentActivities: StateFlow<List<ActivityLog>> = _recentActivities

    // IMPORTANT: Explicit generic to prevent inference to DateRange.ThisMonth
    private val _dateRange = MutableStateFlow<DateRange>(DateRange.ThisMonth)
    val dateRange: StateFlow<DateRange> = _dateRange

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    private val _activeAgreement = MutableStateFlow<LeaseAgreement?>(null)
    val activeAgreement: StateFlow<LeaseAgreement?> = _activeAgreement

    private val _rentSummary = MutableStateFlow<RentSummary?>(null)
    val rentSummary: StateFlow<RentSummary?> = _rentSummary

    private val _tenantAdded = MutableSharedFlow<Long>()
    val tenantAdded: kotlinx.coroutines.flow.SharedFlow<Long> = _tenantAdded

    private val _activeTenantName = MutableStateFlow<String?>(null)
    val activeTenantName: StateFlow<String?> = _activeTenantName

    // ---- Derived flows for shop statuses ----
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
        loadRecentActivity() // load activity from repository
        getActivitiesBetween()
        loadActiveUser()
    }

    private fun loadActiveUser() {
        viewModelScope.launch {
            val userId = appPrefs.getUserId()
            val user = if (userId != null) repository.getUserById(userId) else null
            _currentUser.value = user
        }
    }

    // ---- Dashboard data ----
    fun loadDashboardData() = viewModelScope.launch {
        try {
            _dashboardStats.value = repository.getDashboardStats(userId!!)
            _rentDueReminders.value = repository.getRentDueReminders(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadExpenses() {
        val start = Date(_dateRange.value.startDate)
        val end = Date(_dateRange.value.endDate)

        viewModelScope.launch {
            repository.getExpensesBetweenDates(start, end).collect { _expenses.value = it }
        }

        viewModelScope.launch {
            _categorySummary.value= repository.getExpensesByCategory(start, end)
        }

        viewModelScope.launch {
            _totalExpenses.value = repository.getTotalExpensesBetweenDates(start, end)
        }
    }

    // ---- Shops ----
    fun loadShops() = viewModelScope.launch {
        repository.getAllShops(userId!!).collect { _shops.value = it }
    }

    fun getShopById(shopId: Long) = viewModelScope.launch {
        repository.getShopById(shopId).collect { _shop.value = it }
    }

    fun addShop(shop: Shop) = viewModelScope.launch {
        repository.insertShop(shop)
        loadShops()
        addActivity("New shop '${shop.shopNumber}' added")
    }

    fun updateShop(shop: Shop) = viewModelScope.launch {
        repository.updateShop(shop)
        getShopById(shop.id)
    }

    fun deleteShop(shop: Shop) = viewModelScope.launch {
        repository.deleteShop(shop)
        loadShops()
    }


    fun updateShopStatus(shopId: Long, status: ShopStatus) = viewModelScope.launch {
        repository.updateShopStatus(shopId, status)
        loadShops()
        addActivity("Shop #$shopId status updated to $status")
    }

    // ---- Tenants & Agreements ----
    fun addTenant(tenant: Tenant) = viewModelScope.launch {
        val tenantId = repository.insertTenant(tenant)
        _tenantAdded.emit(tenantId)
    }

    fun assignTenantToShop(shopId: Long, tenant: Tenant, startDate: Date, endDate: Date) = viewModelScope.launch {
        val tenantId = repository.insertTenant(tenant)
        val today = Date()
        val rentDueDay = Calendar.getInstance().apply {
            time = today; set(Calendar.DAY_OF_MONTH, 5)
        }.get(Calendar.DAY_OF_MONTH)

        val agreement = LeaseAgreement(
            agreementNumber = "AG-${System.currentTimeMillis()}",
            shopId = shopId,
            tenantId = tenantId,
            startDate = startDate,
            endDate = endDate,
            monthlyRent = shop.value?.monthlyRent ?: 0.0,
            securityDeposit = shop.value?.securityDeposit ?: 0.0,
            rentDueDay = rentDueDay,
            createdAt = today,
            userId = userId!!
        )
        repository.insertLeaseAgreement(agreement)
        repository.updateShopStatus(shopId, ShopStatus.OCCUPIED)
        refreshActiveAgreement(shopId)
        addActivity("Tenant '${tenant.fullName}' assigned to Shop #$shopId")
    }

    fun refreshActiveAgreement(shopId: Long) = viewModelScope.launch {
        _activeAgreement.value = repository.getActiveAgreementForShop(userId!!,shopId)
        refreshActiveTenantName(shopId)
    }

    fun getActiveAgreementForShop(shopId: Long) = refreshActiveAgreement(shopId)

    fun getActiveAgreementForShopAndTenant(shopId: Long, tenantId: Long) = viewModelScope.launch {
        _activeAgreement.value = repository.getActiveAgreementForShopAndTenant(userId!!, shopId, tenantId)
    }

    fun refreshActiveTenantName(shopId: Long) = viewModelScope.launch {
        _activeTenantName.value = repository.getActiveTenantName(userId!!, shopId)
    }



    // ---- Rent Payment ----
    fun insertRentPayment(payment: RentPayment) = viewModelScope.launch {
        repository.insertRentPayment(payment)
    }

    fun addRentPayment(payment: RentPayment, onError: (String) -> Unit, onSuccess: () -> Unit) = viewModelScope.launch {
        val remaining = repository.getRemainingRentForPeriod(userId!!, payment.agreementId, payment.month, payment.year)
        when {
            payment.amount <= 0 -> onError("Invalid payment amount")
            payment.amount > remaining -> onError("Cannot pay more than remaining ($remaining)")
            else -> {
                repository.insertRentPayment(payment)
                addActivity("Rent payment of ${payment.amount} added for agreement #${payment.agreementId}")
                onSuccess()
            }
        }
    }

    suspend fun getRemainingRent(agreementId: Long, month: Int, year: Int): Double {
        return repository.getRemainingRentForPeriod(userId!!, agreementId, month, year)
    }

    fun loadRentSummary(agreementId: Long) = viewModelScope.launch {
        _rentSummary.value = repository.buildRentSummary(userId!!, agreementId, Date())
    }

    // ---- Lease Agreement ----
    fun addLeaseAgreement(leaseAgreement: LeaseAgreement) = viewModelScope.launch {
        repository.insertLeaseAgreement(leaseAgreement)
    }

    fun renewAgreement(agreementId: Long, newEndDate: Date) = viewModelScope.launch {
        repository.updateAgreementEndDate(userId!!, agreementId, newEndDate)
        val shopId = repository.getAgreementById(userId, agreementId)?.shopId ?: return@launch
        refreshActiveAgreement(shopId)
        addActivity("Agreement #$agreementId renewed until $newEndDate")
    }

    fun removeAgreement(agreementId: Long) = viewModelScope.launch {
        val agreement = repository.getAgreementById(userId!!, agreementId) ?: return@launch

        val tenantId = agreement.tenantId
        repository.deleteAgreement(agreement)
        repository.updateShopStatus(agreement.shopId, ShopStatus.VACANT)

        val tenant = repository.getTenantById(userId,tenantId).firstOrNull()
        if (tenant != null) {
            repository.deleteTenant(tenant)
            addActivity("Tenant '${tenant.fullName}' deleted after removing agreement #$agreementId")
        }

        refreshActiveAgreement(agreement.shopId)
        addActivity("Agreement #$agreementId removed for Shop #${agreement.shopId}")
    }

    // ---- Activity log ----
    private fun addActivity(message: String) = viewModelScope.launch {
        repository.addActivity(userId!!, message)
        loadRecentActivity()
    }

    private fun loadRecentActivity() = viewModelScope.launch {
        repository.getRecentActivities(userId!!).collect { activities ->
            _recentActivity.value = activities
        }
    }

    fun updateDateRange(range: DateRange) {
        _dateRange.value = range
        getActivitiesBetween()
    }

    private fun getActivitiesBetween() = viewModelScope.launch {
        repository.getActivitiesBetween(
            userId = userId!!,
            startDate = _dateRange.value.startDate,
            endDate = _dateRange.value.endDate
        ).collect { activities ->
            _recentActivities.value = activities
        }
    }

    fun loadTenants() = viewModelScope.launch {
        repository.getAllTenants(userId!!).collect { _tenants.value = it }
    }

    fun addOrUpdateTenant(tenant: Tenant, isUpdate: Boolean = false) = viewModelScope.launch {
        if (isUpdate) {
            repository.updateTenant(tenant)
            repository.addActivity(userId!!,"Tenant '${tenant.fullName}' updated")
        } else {
            repository.insertTenant(tenant)
            repository.addActivity(userId!!,"Tenant '${tenant.fullName}' added")
        }
        loadTenants()
    }

    fun deleteTenant(tenant: Tenant, onDeleted: () -> Unit) = viewModelScope.launch {
        repository.deleteTenant(tenant)
        repository.addActivity(userId!!,"Tenant '${tenant.fullName}' deleted")
        loadTenants()
        onDeleted()
    }

    fun getTenantById(id: Long): Flow<Tenant?> = repository.getTenantById(userId!!,id)
}

// ---- DateRange + helpers ----
sealed class DateRange(val label: String, val startDate: Long, val endDate: Long) {
    object ThisMonth : DateRange(
        "This Month",
        startDate = getStartOfMonth(),
        endDate = getEndOfMonth()
    )

    object LastMonth : DateRange(
        "Last Month",
        startDate = getStartOfLastMonth(),
        endDate = getEndOfLastMonth()
    )

    data class Custom(val customStart: Long, val customEnd: Long) :
        DateRange("Custom", customStart, customEnd)
}

private fun normalizeToStartOfDay(cal: Calendar) {
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
}

private fun normalizeToEndOfDay(cal: Calendar) {
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
}

fun getStartOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    normalizeToStartOfDay(cal)
    return cal.timeInMillis
}

fun getEndOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    normalizeToEndOfDay(cal)
    return cal.timeInMillis
}

fun getStartOfLastMonth(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    normalizeToStartOfDay(cal)
    return cal.timeInMillis
}

fun getEndOfLastMonth(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    normalizeToEndOfDay(cal)
    return cal.timeInMillis
}
