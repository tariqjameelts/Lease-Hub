package com.mindblowers.leasehub.data.repository

import com.mindblowers.leasehub.data.dao.ActivityLogDao
import com.mindblowers.leasehub.data.dao.ExpenseDao
import com.mindblowers.leasehub.data.dao.LeaseAgreementDao
import com.mindblowers.leasehub.data.dao.RentPaymentDao
import com.mindblowers.leasehub.data.dao.ShopDao
import com.mindblowers.leasehub.data.dao.TenantDao
import com.mindblowers.leasehub.data.dao.UserDao
import com.mindblowers.leasehub.data.entities.ActivityLog
import com.mindblowers.leasehub.data.entities.AgreementStatus
import com.mindblowers.leasehub.data.entities.Expense
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.data.entities.Tenant
import com.mindblowers.leasehub.data.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val userDao: UserDao,
    private val shopDao: ShopDao,
    private val tenantDao: TenantDao,
    private val leaseAgreementDao: LeaseAgreementDao,
    private val rentPaymentDao: RentPaymentDao,
    private val expenseDao: ExpenseDao,
    private val activityLogDao: ActivityLogDao
) {

    // ✅ Store current logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun setCurrentUser(userId: Long) {
        val user = getUserById(userId)
        _currentUser.value = user
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }
    // User Operations
    suspend fun createUser(user: User) = userDao.insert(user)
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)
    suspend fun updateUserLastLogin(userId: Long, loginTime: Long) =
        userDao.updateLastLogin(userId, loginTime)
    suspend fun updateUser(user: User?) {
        if (user == null) return
        userDao.update(user)
    }
    // AppRepository.kt
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }


    // Shop Operations
    suspend fun insertShop(shop: Shop) = shopDao.insert(shop)
    fun getAllShops() = shopDao.getAllShops()
    fun getShopsByStatus(status: ShopStatus) = shopDao.getShopsByStatus(status)

    // Tenant Operations
    suspend fun insertTenant(tenant: Tenant) :Long = tenantDao.insert(tenant)
    fun getAllTenants() = tenantDao.getAllTenants()
    suspend fun deactivateTenant(tenantId: Long) = tenantDao.deactivateTenant(tenantId)

    // Lease Agreement Operations
    suspend fun insertLeaseAgreement(agreement: LeaseAgreement) = leaseAgreementDao.insert(agreement)
    suspend fun getAgreementWithDetails(agreementId: Long) =
        leaseAgreementDao.getAgreementWithDetails(agreementId)
    fun getActiveAgreementsWithDetails() = leaseAgreementDao.getActiveAgreementsWithDetails()
    suspend fun getActiveAgreementForShop(shopId: Long) =
        leaseAgreementDao.getActiveAgreementForShop(shopId)

    // ✅ NEW
    suspend fun getActiveAgreementForShopAndTenant(shopId: Long, tenantId: Long) =
        leaseAgreementDao.getActiveAgreementForShopAndTenant(shopId, tenantId)
    suspend fun updateAgreementStatus(agreementId: Long, status: AgreementStatus) =
        leaseAgreementDao.updateAgreementStatus(agreementId, status)

    // Rent Payment Operations
    suspend fun insertRentPayment(payment: RentPayment) = rentPaymentDao.insert(payment)
    suspend fun getPaymentWithAgreement(paymentId: Long) =
        rentPaymentDao.getPaymentWithAgreement(paymentId)
    fun getPaymentsWithDetailsForAgreement(agreementId: Long) =
        rentPaymentDao.getPaymentsWithDetailsForAgreement(agreementId)
    suspend fun getMonthlyRevenue(year: Int, month: Int) =
        rentPaymentDao.getMonthlyRevenue(year, month)
    suspend fun getRemainingRentForPeriod(agreementId: Long, month: Int, year: Int): Double {
        val agreement = leaseAgreementDao.getAgreementWithDetails(agreementId).agreement
            ?: throw IllegalArgumentException("Agreement not found")

        val monthlyRent = agreement.monthlyRent
        val alreadyPaid = rentPaymentDao.getTotalPaidForPeriod(agreementId, month, year) ?: 0.0

        return (monthlyRent - alreadyPaid).coerceAtLeast(0.0)

    }


    // Expense Operations
    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    fun getAllExpenses() = expenseDao.getAllExpenses()
    fun getExpensesForShop(shopId: Long) = expenseDao.getExpensesForShop(shopId)
    suspend fun getTotalExpensesBetweenDates(startDate: Date, endDate: Date) =
        expenseDao.getTotalExpensesBetweenDates(startDate, endDate)
    suspend fun getExpensesByCategory(startDate: Date, endDate: Date) =
        expenseDao.getExpensesByCategory(startDate, endDate)

    fun getExpensesBetweenDates(startDate: Date, endDate: Date): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDatesFlow(startDate, endDate)
    }


    // Business Logic Methods
    suspend fun getDashboardStats(): DashboardStats {
        val vacantShops = shopDao.getVacantShopCount()
        val occupiedShops = shopDao.getOccupiedShopCount()
        val activeTenants = tenantDao.getActiveTenantCount()

        val currentDate = Date()
        val thirtyDaysAgo = Date(currentDate.time - 30L * 24 * 60 * 60 * 1000)
        val monthlyRevenue = rentPaymentDao.getMonthlyRevenue(
            currentDate.year + 1900,
            currentDate.month + 1
        )
        val monthlyExpenses = expenseDao.getTotalExpensesBetweenDates(thirtyDaysAgo, currentDate)

        return DashboardStats(
            totalShops = vacantShops + occupiedShops,
            vacantShops = vacantShops,
            occupiedShops = occupiedShops,
            activeTenants = activeTenants,
            monthlyRevenue = monthlyRevenue,
            monthlyExpenses = monthlyExpenses,
            netProfit = monthlyRevenue - monthlyExpenses
        )
    }

    suspend fun getShopById(shopId:Long):Flow<Shop?>{
        return shopDao.getShopById(shopId)
    }

    suspend fun getRentDueReminders(): List<RentDueReminder> {
        val currentDate = Calendar.getInstance()
        val activeAgreements = leaseAgreementDao.getActiveAgreementsWithDetails()
            .first()

        return activeAgreements.mapNotNull { agreement ->
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, agreement.agreement.rentDueDay)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            if (currentDate.after(dueDate)) {
                val existingPayment = rentPaymentDao.getPaymentForPeriod(
                    agreement.agreement.id,
                    currentDate.get(Calendar.MONTH) + 1,
                    currentDate.get(Calendar.YEAR)
                )

                if (existingPayment == null) {
                    val daysOverdue = (currentDate.timeInMillis - dueDate.timeInMillis) / (24 * 60 * 60 * 1000)

                    RentDueReminder(
                        agreement = agreement.agreement,
                        shop = agreement.shop,
                        tenant = agreement.tenant,
                        dueDate = dueDate.time,
                        daysOverdue = daysOverdue
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }


    suspend fun buildRentSummary(agreementId: Long, asOf: Date = Date()): RentSummary {
        val agreementWithDetails = leaseAgreementDao.getAgreementWithDetails(agreementId)
        val agreement = agreementWithDetails.agreement
        val monthlyRent = agreement.monthlyRent

        // define iteration window: from start to min(end, asOf)
        val startCal = Calendar.getInstance().apply { time = agreement.startDate; set(Calendar.DAY_OF_MONTH, 1) }
        val endBound = if (asOf.before(agreement.endDate)) asOf else agreement.endDate
        val endCal = Calendar.getInstance().apply { time = endBound; set(Calendar.DAY_OF_MONTH, 1) }

        // We'll also compute what's "current" (asOf month)
        val asOfCal = Calendar.getInstance().apply { time = asOf }
        val asOfMonth = asOfCal.get(Calendar.MONTH) + 1
        val asOfYear = asOfCal.get(Calendar.YEAR)

        // Iterate months
        val records = mutableListOf<MonthRentRecord>()
        val iter = Calendar.getInstance().apply { time = startCal.time }

        while (iter.before(endCal) || // strictly before end month
            (iter.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) && iter.get(Calendar.YEAR) == endCal.get(Calendar.YEAR))) {
            val m = iter.get(Calendar.MONTH) + 1
            val y = iter.get(Calendar.YEAR)

            val paid = rentPaymentDao.getTotalPaidForPeriod(agreementId, m, y)
            val remaining = (monthlyRent - paid).coerceAtLeast(0.0)
            val status = when {
                remaining == 0.0 && paid > 0.0 -> RentStatus.PAID
                paid in 0.0..(monthlyRent - 0.01) && paid > 0.0 -> RentStatus.PARTIAL
                paid <= 0.0 -> if (monthlyRent <= 0.0) RentStatus.PAID else RentStatus.UNPAID
                else -> RentStatus.PARTIAL
            }

            records += MonthRentRecord(
                month = m,
                year = y,
                rent = monthlyRent,
                paid = paid,
                remaining = remaining,
                status = status
            )

            // next month
            iter.add(Calendar.MONTH, 1)
        }

        // Aggregate
        var currentRemaining = 0.0
        var previousPendingTotal = 0.0

        records.forEach { rec ->
            if (rec.year == asOfYear && rec.month == asOfMonth) {
                currentRemaining = rec.remaining
            } else {
                // strictly before asOf month?
                val isBefore = (rec.year < asOfYear) || (rec.year == asOfYear && rec.month < asOfMonth)
                if (isBefore) previousPendingTotal += rec.remaining
            }
        }

        val totalRemaining = previousPendingTotal + currentRemaining

        return RentSummary(
            currentRemaining = currentRemaining,
            previousPendingTotal = previousPendingTotal,
            totalRemaining = totalRemaining,
            records = records
        )
    }


    suspend fun updateAgreementEndDate(agreementId: Long, newEndDate: Date) {
        val agreement = leaseAgreementDao.getAgreementById(agreementId) ?: return
        leaseAgreementDao.update(agreement.copy(endDate = newEndDate))
    }

    suspend fun deleteAgreement(agreement: LeaseAgreement) {
        leaseAgreementDao.deleteAgreement(agreement)
    }

    suspend fun updateShopStatus(shopId: Long, status: ShopStatus) {
        val shop = shopDao.getShopById(shopId).firstOrNull() // collect once
        shop?.let {
            shopDao.update(it.copy(status = status))
        }
    }


    suspend fun deleteShop(shop: Shop) {
        shopDao.delete(shop)
    }

    // inside AppRepository
    suspend fun getAgreementById(agreementId: Long) =
        leaseAgreementDao.getAgreementById(agreementId)

    // Tenant Operations (existing)
    fun getTenantById(tenantId: Long): Flow<Tenant?> =
        tenantDao.getTenantById(tenantId)

    suspend fun getActiveTenantName(shopId: Long): String? {
        val agreement = leaseAgreementDao.getActiveAgreementForShop(shopId)
        return agreement?.tenantId?.let { tenantId ->
            tenantDao.getTenantById(tenantId).firstOrNull()?.fullName
        }
    }


    suspend fun updateShop(shop: Shop) = shopDao.update(shop)


    // Insert activity
    suspend fun addActivity(message: String) {
        activityLogDao.insert(ActivityLog(message = message))
    }

    // Fetch recent activities
    fun getRecentActivities(): Flow<List<ActivityLog>> = activityLogDao.getRecentActivities()

    fun getActivitiesBetween(startDate: Long, endDate: Long): Flow<List<ActivityLog>> {
        return activityLogDao.getActivitiesBetween(startDate, endDate)
    }

    suspend fun updateTenant(tenant: Tenant) = tenantDao.update(tenant)

    suspend fun deleteTenant(tenant: Tenant) = tenantDao.delete(tenant)

}

data class DashboardStats(
    val totalShops: Int,
    val vacantShops: Int,
    val occupiedShops: Int,
    val activeTenants: Int,
    val monthlyRevenue: Double,
    val monthlyExpenses: Double,
    val netProfit: Double
)

data class RentDueReminder(
    val agreement: LeaseAgreement,
    val shop: Shop,
    val tenant: Tenant,
    val dueDate: Date,
    val daysOverdue: Long
)

enum class RentStatus { PAID, PARTIAL, UNPAID }

data class MonthRentRecord(
    val month: Int,         // 1..12
    val year: Int,
    val rent: Double,       // monthlyRent
    val paid: Double,       // total paid for this month
    val remaining: Double,  // max(rent - paid, 0)
    val status: RentStatus  // PAID | PARTIAL | UNPAID
)

data class RentSummary(
    val currentRemaining: Double,     // Remaining for as-of month (if within agreement)
    val previousPendingTotal: Double, // Sum of remaining for months strictly before as-of month
    val totalRemaining: Double,       // previousPendingTotal + currentRemaining
    val records: List<MonthRentRecord>
)
