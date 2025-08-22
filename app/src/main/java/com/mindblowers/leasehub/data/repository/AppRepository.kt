package com.mindblowers.leasehub.data.repository

import com.mindblowers.leasehub.data.dao.ExpenseDao
import com.mindblowers.leasehub.data.dao.LeaseAgreementDao
import com.mindblowers.leasehub.data.dao.RentPaymentDao
import com.mindblowers.leasehub.data.dao.ShopDao
import com.mindblowers.leasehub.data.dao.TenantDao
import com.mindblowers.leasehub.data.dao.UserDao
import com.mindblowers.leasehub.data.entities.AgreementStatus
import com.mindblowers.leasehub.data.entities.Expense
import com.mindblowers.leasehub.data.entities.LeaseAgreement
import com.mindblowers.leasehub.data.entities.RentPayment
import com.mindblowers.leasehub.data.entities.Shop
import com.mindblowers.leasehub.data.entities.ShopStatus
import com.mindblowers.leasehub.data.entities.Tenant
import com.mindblowers.leasehub.data.entities.User
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val userDao: UserDao,
    private val shopDao: ShopDao,
    private val tenantDao: TenantDao,
    private val leaseAgreementDao: LeaseAgreementDao,
    private val rentPaymentDao: RentPaymentDao,
    private val expenseDao: ExpenseDao
) {
    // User Operations
    suspend fun createUser(user: User) = userDao.insert(user)
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)
    suspend fun updateUserLastLogin(userId: Long, loginTime: Long) =
        userDao.updateLastLogin(userId, loginTime)

    // Shop Operations
    suspend fun insertShop(shop: Shop) = shopDao.insert(shop)
    fun getAllShops() = shopDao.getAllShops()
    fun getShopsByStatus(status: ShopStatus) = shopDao.getShopsByStatus(status)
    suspend fun updateShopStatus(shopId: Long, status: ShopStatus) =
        shopDao.updateShopStatus(shopId, status)

    // Tenant Operations
    suspend fun insertTenant(tenant: Tenant) = tenantDao.insert(tenant)
    fun getAllTenants() = tenantDao.getAllTenants()
    suspend fun deactivateTenant(tenantId: Long) = tenantDao.deactivateTenant(tenantId)

    // Lease Agreement Operations
    suspend fun insertLeaseAgreement(agreement: LeaseAgreement) = leaseAgreementDao.insert(agreement)
    suspend fun getAgreementWithDetails(agreementId: Long) =
        leaseAgreementDao.getAgreementWithDetails(agreementId)
    fun getActiveAgreementsWithDetails() = leaseAgreementDao.getActiveAgreementsWithDetails()
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

    // Expense Operations
    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    fun getAllExpenses() = expenseDao.getAllExpenses()
    fun getExpensesForShop(shopId: Long) = expenseDao.getExpensesForShop(shopId)
    suspend fun getTotalExpensesBetweenDates(startDate: Date, endDate: Date) =
        expenseDao.getTotalExpensesBetweenDates(startDate, endDate)
    suspend fun getExpensesByCategory(startDate: Date, endDate: Date) =
        expenseDao.getExpensesByCategory(startDate, endDate)

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