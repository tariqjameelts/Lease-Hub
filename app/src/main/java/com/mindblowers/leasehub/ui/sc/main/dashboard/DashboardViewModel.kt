package com.mindblowers.leasehub.ui.sc.main.dashboard

import android.util.Log
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
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.LeaseDetail
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.LeaseReportsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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


    // Add these to your DashboardViewModel class
    private val _financialReportsData = MutableStateFlow(FinancialReportsData())
    val financialReportsData: StateFlow<FinancialReportsData> = _financialReportsData

    private val _reportsDateRange = MutableStateFlow<DateRange>(DateRange.ThisMonth)
    val reportsDateRange: StateFlow<DateRange> = _reportsDateRange

    // Add to DashboardViewModel
    private val _leaseReportsData = MutableStateFlow(LeaseReportsData())
    val leaseReportsData: StateFlow<LeaseReportsData> = _leaseReportsData

    fun loadLeaseReportsData() {
        viewModelScope.launch {
            try {
                val range = _reportsDateRange.value
                val startDate = Date(range.startDate)
                val endDate = Date(range.endDate)

                // Get shop statistics
                val totalShops = repository.getAllShops(userId!!).first().size
                val occupiedShops =
                    repository.getShopsByStatus(userId, ShopStatus.OCCUPIED).first().size
                val vacantShops =
                    repository.getShopsByStatus(userId, ShopStatus.VACANT).first().size
                val occupancyRate = if (totalShops > 0) (occupiedShops * 100 / totalShops) else 0

                // Get lease details
                val activeAgreements = repository.getActiveAgreementsWithDetails(userId).first()
                val leaseDetails = activeAgreements.map { agreement ->
                    LeaseDetail(
                        shopNumber = agreement.shop.shopNumber,
                        tenantName = agreement.tenant.fullName,
                        status = agreement.agreement.status.name,
                        monthlyRent = agreement.agreement.monthlyRent,
                        startDate = agreement.agreement.startDate,
                        endDate = agreement.agreement.endDate
                    )
                }

                _leaseReportsData.value = LeaseReportsData(
                    totalShops = totalShops,
                    occupiedShops = occupiedShops,
                    vacantShops = vacantShops,
                    occupancyRate = occupancyRate,
                    leaseDetails = leaseDetails
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateReportsDateRange(range: DateRange) {
        _reportsDateRange.value = range
        loadFinancialReportsData()
    }

    fun loadFinancialReportsData() {
        viewModelScope.launch {
            try {
                val range = _reportsDateRange.value
                val startDate = Date(range.startDate)
                val endDate = Date(range.endDate)

                Log.d("DashboardViewModel","DEBUG: Loading financial data for range: $startDate to $endDate")


                // Get rent payments for the period
                val rentPayments = repository.getPaymentsBetweenDates(startDate, endDate, userId!!)
                Log.d("DashboardViewModel","DEBUG: Found ${rentPayments.size} payments in date range")

                val totalCollected = rentPayments.sumOf { it.amount }
                Log.d("DashboardViewModel","DEBUG: Total collected: $totalCollected")

                // Get pending rent (agreements without payments in this period)
                val pendingRent = calculatePendingRent(startDate, endDate)
                Log.d("DashboardViewModel","DEBUG: Pending rent: $pendingRent")

                // Get expenses for the period
                val totalExpenses = repository.getTotalExpensesBetweenDates(startDate, endDate)

                // Get recent transactions - FIXED: Use firstOrNull() to get Tenant from Flow
// In DashboardViewModel.kt - Fix the recent transactions mapping
                val recentTransactions = rentPayments.take(10).mapNotNull { payment ->
                    try {
                        val agreement = repository.getAgreementById(userId, payment.agreementId)
                        if (agreement != null) {
                            val tenant = repository.getTenantByIdDirect(userId, agreement.tenantId)
                            FinancialTransaction(
                                tenantName = tenant?.fullName ?: "Unknown Tenant",
                                amount = payment.amount,
                                status = if (payment.amount > 0) "Paid" else "Pending",
                                date = payment.paymentDate,
                                paymentMethod = payment.paymentMethod.name
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("DashboardViewModel","ERROR: Failed to load financial data: ${e.message}")
                        null
                    }
                }
                _financialReportsData.value = FinancialReportsData(
                    totalCollected = totalCollected,
                    pendingRent = pendingRent,
                    totalExpenses = totalExpenses,
                    recentTransactions = recentTransactions
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun getTenantIdFromAgreement(agreementId: Long): Long {
        val agreement = repository.getAgreementById(userId!!, agreementId)
        return agreement?.tenantId ?: -1L
    }


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

    // In DashboardViewModel.kt - Fix the calculatePendingRent method
    private suspend fun calculatePendingRent(startDate: Date, endDate: Date): Double {
        val activeAgreements = repository.getActiveAgreementsWithDetails(userId!!).first()
        var pendingAmount = 0.0

        activeAgreements.forEach { agreement ->
            // Check if agreement was active during the selected period
            val agreementStart = agreement.agreement.startDate
            val agreementEnd = agreement.agreement.endDate

            // Skip if agreement wasn't active during the selected period
            if (agreementEnd.before(startDate) || agreementStart.after(endDate)) {
                return@forEach
            }

            val payments = repository.getPaymentsBetweenDatesForAgreement(
                agreement.agreement.id, startDate, endDate, userId!!
            )

            // Calculate expected rent for the period
            val expectedRent = calculateExpectedRentForPeriod(
                agreement.agreement.monthlyRent,
                agreementStart,
                agreementEnd,
                startDate,
                endDate
            )

            val totalPaid = payments.sumOf { it.amount }
            if (totalPaid < expectedRent) {
                pendingAmount += (expectedRent - totalPaid)
            }
        }

        return pendingAmount
    }

    // Helper function to calculate expected rent for a period
    private fun calculateExpectedRentForPeriod(
        monthlyRent: Double,
        agreementStart: Date,
        agreementEnd: Date,
        periodStart: Date,
        periodEnd: Date
    ): Double {
        // Determine the actual period to consider (intersection of agreement and selected period)
        val effectiveStart = maxOf(agreementStart.time, periodStart.time)
        val effectiveEnd = minOf(agreementEnd.time, periodEnd.time)

        if (effectiveStart > effectiveEnd) return 0.0

        // Calculate number of months in the effective period
        val startCal = Calendar.getInstance().apply { timeInMillis = effectiveStart }
        val endCal = Calendar.getInstance().apply { timeInMillis = effectiveEnd }

        var months = 0
        val tempCal = startCal.clone() as Calendar
        while (tempCal.timeInMillis <= endCal.timeInMillis) {
            months++
            tempCal.add(Calendar.MONTH, 1)
        }

        return monthlyRent * months
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
            _categorySummary.value = repository.getExpensesByCategory(start, end)
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

    fun assignTenantToShop(shopId: Long, tenant: Tenant, startDate: Date, endDate: Date) =
        viewModelScope.launch {
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
        _activeAgreement.value = repository.getActiveAgreementForShop(userId!!, shopId)
        refreshActiveTenantName(shopId)
    }

    fun getActiveAgreementForShop(shopId: Long) = refreshActiveAgreement(shopId)

    fun getActiveAgreementForShopAndTenant(shopId: Long, tenantId: Long) = viewModelScope.launch {
        _activeAgreement.value =
            repository.getActiveAgreementForShopAndTenant(userId!!, shopId, tenantId)
    }

    fun refreshActiveTenantName(shopId: Long) = viewModelScope.launch {
        _activeTenantName.value = repository.getActiveTenantName(userId!!, shopId)
    }


    // ---- Rent Payment ----
    fun insertRentPayment(payment: RentPayment) = viewModelScope.launch {
        repository.insertRentPayment(payment)
    }

    // In DashboardViewModel.kt - Update the addRentPayment function
    fun addRentPayment(payment: RentPayment, onError: (String) -> Unit, onSuccess: () -> Unit) =
        viewModelScope.launch {
            // For pro-rated months, we need to check the actual due amount
            val agreement = repository.getAgreementById(userId!!, payment.agreementId)
            val today = Date()

            // Check if this is the first month (pro-rated)
            val isFirstMonth = isInFirstMonth(agreement?.startDate ?: Date(), today)
            val expectedAmount = if (isFirstMonth) {
                calculateProRatedRent(
                    agreement?.monthlyRent ?: 0.0,
                    agreement?.startDate ?: Date(),
                    today
                )
            } else {
                agreement?.monthlyRent ?: 0.0
            }

            val remaining = repository.getRemainingRentForPeriod(
                userId!!,
                payment.agreementId,
                payment.month,
                payment.year
            )

            when {
                payment.amount <= 0 -> onError("Invalid payment amount")
                payment.amount > expectedAmount -> onError(
                    "Cannot pay more than expected amount (PKR ${
                        String.format(
                            "%.2f",
                            expectedAmount
                        )
                    })"
                )

                else -> {
                    repository.insertRentPayment(payment)
                    addActivity("Rent payment of PKR ${payment.amount} added for agreement #${payment.agreementId}")
                    onSuccess()
                }
            }
        }

    // Add these helper functions to DashboardViewModel
    private fun isInFirstMonth(startDate: Date, currentDate: Date): Boolean {
        val startCal = Calendar.getInstance().apply { time = startDate }
        val currentCal = Calendar.getInstance().apply { time = currentDate }

        return startCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)
    }

    private fun calculateProRatedRent(
        monthlyRent: Double,
        startDate: Date,
        currentDate: Date
    ): Double {
        val startCal = Calendar.getInstance().apply { time = startDate }
        val currentCal = Calendar.getInstance().apply { time = currentDate }

        val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysOccupied = daysInMonth - startCal.get(Calendar.DAY_OF_MONTH) + 1

        return (monthlyRent / daysInMonth) * daysOccupied
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

        val tenant = repository.getTenantById(userId, tenantId).firstOrNull()
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
            repository.addActivity(userId!!, "Tenant '${tenant.fullName}' updated")
        } else {
            repository.insertTenant(tenant)
            repository.addActivity(userId!!, "Tenant '${tenant.fullName}' added")
        }
        loadTenants()
    }

    fun deleteTenant(tenant: Tenant, onDeleted: () -> Unit) = viewModelScope.launch {
        repository.deleteTenant(tenant)
        repository.addActivity(userId!!, "Tenant '${tenant.fullName}' deleted")
        loadTenants()
        onDeleted()
    }

    fun getTenantById(id: Long): Flow<Tenant?> = repository.getTenantById(userId!!, id)
}

// In your DateRange.kt file (wherever it's defined)
sealed class DateRange(val label: String, val startDate: Long, val endDate: Long) {
    object ThisWeek : DateRange(
        "This Week",
        startDate = getStartOfWeek(),
        endDate = getEndOfWeek()
    )

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

// Add these helper functions
private fun getStartOfWeek(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    normalizeToStartOfDay(cal)
    return cal.timeInMillis
}

private fun getEndOfWeek(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.add(Calendar.DAY_OF_WEEK, 6)
    normalizeToEndOfDay(cal)
    return cal.timeInMillis
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

// Add these data classes to your repository or viewmodel file
data class FinancialReportsData(
    val totalCollected: Double = 0.0,
    val pendingRent: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<FinancialTransaction> = emptyList()
)

data class FinancialTransaction(
    val tenantName: String,
    val amount: Double,
    val status: String,
    val date: Date,
    val paymentMethod: String
)
