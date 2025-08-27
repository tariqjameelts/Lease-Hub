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
    fun getAllShops(userId: Long) = shopDao.getAllShops(userId)
    fun getShopsByStatus(userId: Long,status: ShopStatus) = shopDao.getShopsByStatus(userId,status)

    // Tenant Operations
    suspend fun insertTenant(tenant: Tenant) :Long = tenantDao.insert(tenant)
    fun getAllTenants(userId: Long) = tenantDao.getAllTenants(userId)
    suspend fun deactivateTenant(userId: Long,tenantId: Long) = tenantDao.deactivateTenant(userId,tenantId)

    // Lease Agreement Operations
    suspend fun insertLeaseAgreement(agreement: LeaseAgreement) = leaseAgreementDao.insert(agreement)
    suspend fun getAgreementWithDetails(userId: Long,agreementId: Long) =
        leaseAgreementDao.getAgreementWithDetails(userId,agreementId)
    fun getActiveAgreementsWithDetails(userId: Long) = leaseAgreementDao.getActiveAgreementsWithDetails(userId)
    suspend fun getActiveAgreementForShop(userId: Long,shopId: Long) =
        leaseAgreementDao.getActiveAgreementForShop(shopId,userId)

    // ✅ NEW
    suspend fun getActiveAgreementForShopAndTenant(userId: Long,shopId: Long, tenantId: Long) =
        leaseAgreementDao.getActiveAgreementForShopAndTenant(shopId, tenantId, userId)
    suspend fun updateAgreementStatus(agreementId: Long, status: AgreementStatus) =
        leaseAgreementDao.updateAgreementStatus(agreementId, status)

    // Rent Payment Operations
    suspend fun insertRentPayment(payment: RentPayment) = rentPaymentDao.insert(payment)
    suspend fun getPaymentWithAgreement(userId: Long,paymentId: Long) =
        rentPaymentDao.getPaymentWithAgreement(paymentId,userId)
    fun getPaymentsWithDetailsForAgreement(userId: Long,agreementId: Long) =
        rentPaymentDao.getPaymentsWithDetailsForAgreement(agreementId,userId)
    suspend fun getMonthlyRevenue(userId: Long,year: Int, month: Int) =
        rentPaymentDao.getMonthlyRevenue(year, month,userId)
    suspend fun getRemainingRentForPeriod(userId: Long,agreementId: Long, month: Int, year: Int): Double {
        val agreement = leaseAgreementDao.getAgreementWithDetails(userId,agreementId).agreement
            ?: throw IllegalArgumentException("Agreement not found")

        val monthlyRent = agreement.monthlyRent
        val alreadyPaid = rentPaymentDao.getTotalPaidForPeriod(agreementId, month, year, userId) ?: 0.0

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
    suspend fun getDashboardStats(userId: Long): DashboardStats {
        val vacantShops = shopDao.getVacantShopCount(userId)
        val occupiedShops = shopDao.getOccupiedShopCount(userId)
        val activeTenants = tenantDao.getActiveTenantCount(userId)

        val currentDate = Date()
        val thirtyDaysAgo = Date(currentDate.time - 30L * 24 * 60 * 60 * 1000)
        val monthlyRevenue = rentPaymentDao.getMonthlyRevenue(
            currentDate.year + 1900,
            currentDate.month + 1,
            userId
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

    suspend fun getRentDueReminders(userId: Long): List<RentDueReminder> {
        val currentDate = Calendar.getInstance()
        val activeAgreements = leaseAgreementDao.getActiveAgreementsWithDetails(userId)
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
                    currentDate.get(Calendar.YEAR),
                    userId
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


    suspend fun buildRentSummary(userId: Long, agreementId: Long, asOf: Date = Date()): RentSummary {
        val agreementWithDetails = leaseAgreementDao.getAgreementWithDetails(userId,agreementId)
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

            val paid = rentPaymentDao.getTotalPaidForPeriod(agreementId, m, y, userId)
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


    suspend fun updateAgreementEndDate(userId: Long, agreementId: Long, newEndDate: Date) {
        val agreement = leaseAgreementDao.getAgreementById(agreementId,userId) ?: return
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
    suspend fun getAgreementById(userId: Long, agreementId: Long) =
        leaseAgreementDao.getAgreementById(agreementId, userId)

    // Tenant Operations (existing)
    fun getTenantById(userId: Long,tenantId: Long): Flow<Tenant?> =
        tenantDao.getTenantById(userId,tenantId)

    suspend fun getActiveTenantName(userId: Long, shopId: Long): String? {
        val agreement = leaseAgreementDao.getActiveAgreementForShop(shopId, userId)
        return agreement?.tenantId?.let { tenantId ->
            tenantDao.getTenantById(userId,tenantId).firstOrNull()?.fullName
        }
    }


    suspend fun updateShop(shop: Shop) = shopDao.update(shop)


    // Insert activity
    suspend fun addActivity(userId: Long, message: String) {
        activityLogDao.insert(ActivityLog(message = message, userId = userId))
    }

    // Fetch recent activities
    fun getRecentActivities(userId: Long): Flow<List<ActivityLog>> = activityLogDao.getRecentActivities(userId)

    fun getActivitiesBetween(userId: Long,startDate: Long, endDate: Long): Flow<List<ActivityLog>> {
        return activityLogDao.getActivitiesBetween(userId,startDate, endDate)
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
