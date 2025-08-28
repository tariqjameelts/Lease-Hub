package com.mindblowers.leasehub.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.room.withTransaction
import com.google.gson.Gson
import com.mindblowers.leasehub.data.AppDatabase
import com.mindblowers.leasehub.data.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {

    companion object {
        private const val PREFIX = "leasehub_backup_"
        private const val EXT = ".json"
        private const val BACKUP_VERSION = 1
    }

    // Data classes for backup format
    data class BackupData(
        val version: Int = BACKUP_VERSION,
        val timestamp: Long = System.currentTimeMillis(),
        val users: List<User> = emptyList(),
        val shops: List<Shop> = emptyList(),
        val tenants: List<Tenant> = emptyList(),
        val leaseAgreements: List<LeaseAgreement> = emptyList(),
        val rentPayments: List<RentPayment> = emptyList(),
        val expenses: List<Expense> = emptyList(),
        val activityLogs: List<ActivityLog> = emptyList()
    )

    private val gson = Gson()

    // ----- Filename helpers -----

    private fun nowStamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    fun suggestedBackupFileName(): String =
        "${PREFIX}${nowStamp()}_v${BACKUP_VERSION}$EXT"

    fun isLikelyLeaseHubBackup(name: String): Boolean =
        name.startsWith(PREFIX) && name.endsWith(EXT)

    // ----- Export functionality -----

    /** Creates a backup with all data and exports to a user-selected SAF Uri */
    suspend fun exportBackupToUri(destUri: Uri, onProgress: (String) -> Unit = {}): Boolean =
        withContext(Dispatchers.IO) {
            try {
                onProgress("Collecting data...")

                // Collect all data for backup
                val backupData = BackupData(
                    users = database.userDao().getAllForBackup(),
                    shops = database.shopDao().getAllForBackup(),
                    tenants = database.tenantDao().getAllForBackup(),
                    leaseAgreements = database.leaseAgreementDao().getAllForBackup(),
                    rentPayments = database.rentPaymentDao().getAllForBackup(),
                    expenses = database.expenseDao().getAllForBackup(),
                    activityLogs = database.activityLogDao().getAllForBackup()
                )

                onProgress("Creating backup file...")
                // Convert to JSON and write to Uri
                val json = gson.toJson(backupData)

                context.contentResolver.openOutputStream(destUri, "w")?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                    outputStream.flush()
                } ?: return@withContext false

                onProgress("Backup completed successfully!")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                onProgress("Backup failed: ${e.message}")
                false
            }
        }

    // ----- Import functionality -----

    /** Import and merge backup data from SAF Uri */
    suspend fun importAndMergeFromUri(backupUri: Uri, currentUserId: Long, onProgress: (String) -> Unit = {}): Boolean =
        withContext(Dispatchers.IO) {
            try {
                onProgress("Reading backup file...")

                // Read backup file
                val json = context.contentResolver.openInputStream(backupUri)?.use { input ->
                    input.bufferedReader().use { it.readText() }
                } ?: return@withContext false

                onProgress("Parsing backup data...")
                // Parse backup data
                val backupData = gson.fromJson(json, BackupData::class.java)

                // Validate backup version
                if (backupData.version != BACKUP_VERSION) {
                    onProgress("Backup version mismatch. Expected: $BACKUP_VERSION, Found: ${backupData.version}")
                    return@withContext false
                }

                onProgress("Merging data...")
                // Use withTransaction instead of runInTransaction for coroutine support
                database.withTransaction {
                    // Merge users (preserve current user, add new ones)
                    mergeUsers(backupData.users, currentUserId, onProgress)

                    // Merge shops (add new ones, update existing with newer timestamps)
                    mergeShops(backupData.shops, currentUserId, onProgress)

                    // Merge tenants (add new ones, update existing with newer timestamps)
                    mergeTenants(backupData.tenants, currentUserId, onProgress)

                    // Merge lease agreements
                    mergeLeaseAgreements(backupData.leaseAgreements, currentUserId, onProgress)

                    // Merge rent payments (avoid duplicates)
                    mergeRentPayments(backupData.rentPayments, currentUserId, onProgress)

                    // Merge expenses (avoid duplicates)
                    mergeExpenses(backupData.expenses, currentUserId, onProgress)

                    // Merge activity logs (add new ones)
                    mergeActivityLogs(backupData.activityLogs, currentUserId, onProgress)
                }

                onProgress("Import completed successfully!")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                onProgress("Import failed: ${e.message}")
                false
            }
        }

    // ----- Individual merge functions (now marked as suspend) -----

    private suspend fun mergeUsers(backupUsers: List<User>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging users...")
        backupUsers.forEach { backupUser ->
            if (backupUser.id != currentUserId) { // Don't overwrite current user
                val existingUser = database.userDao().getUserById(backupUser.id)
                if (existingUser == null) {
                    database.userDao().insert(backupUser)
                } else if (existingUser.lastLogin?.before(backupUser.lastLogin ?: Date(0)) == true) {
                    // Update if backup user is newer
                    database.userDao().update(backupUser)
                }
            }
        }
    }

    private suspend fun mergeShops(backupShops: List<Shop>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging shops...")
        backupShops.forEach { backupShop ->
            if (backupShop.userId == currentUserId) { // Only merge shops belonging to current user
                val existingShop = database.shopDao().getShopByIdSync(backupShop.id)
                if (existingShop == null) {
                    database.shopDao().insert(backupShop)
                } else if (existingShop.createdAt.before(backupShop.createdAt)) {
                    // Update if backup shop is newer
                    database.shopDao().update(backupShop)
                }
            }
        }
    }

    private suspend fun mergeTenants(backupTenants: List<Tenant>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging tenants...")
        backupTenants.forEach { backupTenant ->
            if (backupTenant.userId == currentUserId) {
                val existingTenant = database.tenantDao().getTenantByIdSync(currentUserId, backupTenant.id)
                if (existingTenant == null) {
                    database.tenantDao().insert(backupTenant)
                } else if (existingTenant.createdAt.before(backupTenant.createdAt)) {
                    database.tenantDao().update(backupTenant)
                }
            }
        }
    }

    private suspend fun mergeLeaseAgreements(backupAgreements: List<LeaseAgreement>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging lease agreements...")
        backupAgreements.forEach { backupAgreement ->
            if (backupAgreement.userId == currentUserId) {
                val existingAgreement = database.leaseAgreementDao().getAgreementById(backupAgreement.id, currentUserId)
                if (existingAgreement == null) {
                    database.leaseAgreementDao().insert(backupAgreement)
                } else if (existingAgreement.createdAt.before(backupAgreement.createdAt)) {
                    database.leaseAgreementDao().update(backupAgreement)
                }
            }
        }
    }

    private suspend fun mergeRentPayments(backupPayments: List<RentPayment>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging rent payments...")
        backupPayments.forEach { backupPayment ->
            if (backupPayment.userId == currentUserId) {
                val existingPayment = database.rentPaymentDao().getPaymentByIdSync(backupPayment.id, currentUserId)
                if (existingPayment == null) {
                    database.rentPaymentDao().insert(backupPayment)
                }
                // Don't update existing payments to avoid overwriting recent data
            }
        }
    }

    private suspend fun mergeExpenses(backupExpenses: List<Expense>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging expenses...")
        backupExpenses.forEach { backupExpense ->
            if (backupExpense.userId == currentUserId) {
                val existingExpense = database.expenseDao().getExpenseByIdSync(backupExpense.id)
                if (existingExpense == null) {
                    database.expenseDao().insert(backupExpense)
                }
            }
        }
    }

    private suspend fun mergeActivityLogs(backupLogs: List<ActivityLog>, currentUserId: Long, onProgress: (String) -> Unit) {
        onProgress("Merging activity logs...")
        backupLogs.forEach { backupLog ->
            if (backupLog.userId == currentUserId) {
                val existingLog = database.activityLogDao().getActivityLogByIdSync(backupLog.id)
                if (existingLog == null) {
                    database.activityLogDao().insert(backupLog)
                }
            }
        }
    }
}