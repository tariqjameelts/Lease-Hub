// com.mindblowers.leasehub.utils.BackupManager

package com.mindblowers.leasehub.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import com.mindblowers.leasehub.data.AppDatabase
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
        private const val INTERNAL_BACKUP_DIR = "leasehub_backups"
        private const val PREFIX = "leasehub_backup_"
        private const val EXT = ".db"
        private const val DB_NAME = "shop_leasing_db"
    }

    // ----- Filename helpers -----

    private fun nowStamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    fun suggestedBackupFileName(): String =
        "${PREFIX}${nowStamp()}_v${AppDatabase.VERSION}$EXT"

    /** Example: leasehub_backup_20250826_101010_v2.db -> 2  (null if parse fails) */
    private fun parseSchemaFromName(name: String): Int? {
        val idx = name.lastIndexOf("_v")
        if (idx == -1 || !name.endsWith(EXT)) return null
        val verPart = name.substring(idx + 2, name.length - EXT.length)
        return verPart.toIntOrNull()
    }

    fun isLikelyLeaseHubBackup(name: String): Boolean =
        name.startsWith(PREFIX) && name.endsWith(EXT)

    // ----- Internal create/list/delete -----

    /** Creates an internal (app-private external) backup file. */
    suspend fun createInternalBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir(null), INTERNAL_BACKUP_DIR)
            if (!backupDir.exists()) backupDir.mkdirs()

            val out = File(backupDir, suggestedBackupFileName())

            database.openHelper.writableDatabase.let { db ->
                // Optional: shrink & clean
                db.execSQL("VACUUM")
                val source = File(db.path)
                source.copyTo(out, overwrite = true)
            }
            out
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getInternalBackups(): List<File> {
        val dir = File(context.getExternalFilesDir(null), INTERNAL_BACKUP_DIR)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles { f ->
            f.isFile && isLikelyLeaseHubBackup(f.name)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun deleteInternalBackup(file: File): Boolean = try { file.delete() } catch (_: Exception) { false }

    fun getReadableFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024L * 1024 * 1024)} GB"
    }

    // ----- SAF Export / Import -----

    /**
     * Copy an internal backup file to a user-selected SAF Uri (Export).
     */
    suspend fun exportBackupToUri(internalBackup: File, destUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            if (!internalBackup.exists()) return@withContext false
            try {
                context.contentResolver.openOutputStream(destUri, "w")?.use { out ->
                    FileInputStream(internalBackup).use { input ->
                        input.copyTo(out)
                        out.flush()
                    }
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    /**
     * Restore DB from a user-selected SAF Uri (Import + Overwrite).
     * Validates filename convention and optionally schema version.
     * Overwrites the *entire* database file.
     */
    suspend fun restoreFromUri(backupUri: Uri, pickedDisplayName: String? = null): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // Best-effort filename validation (some pickers won’t give us display name).
                pickedDisplayName?.let { name ->
                    if (!isLikelyLeaseHubBackup(name)) return@withContext false
                    val schema = parseSchemaFromName(name)
                    // Hard fail if schema mismatch. You can soften this if you support migrations.
                    if (schema != null && schema != AppDatabase.VERSION) {
                        return@withContext false
                    }
                }

                val dbFile = context.getDatabasePath(DB_NAME)

                // Close Room database before overwriting the file
                database.close()

                // Copy from SAF Uri -> DB location
                context.contentResolver.openInputStream(backupUri)?.use { input ->
                    FileOutputStream(dbFile, false).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                } ?: return@withContext false

                // Reopen database (ensures AppDatabase is usable again)
                database.openHelper.writableDatabase

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    // Optional: internal restore (from a File already in internal backup dir)
    suspend fun restoreFromInternalFile(file: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists() || !isLikelyLeaseHubBackup(file.name)) return@withContext false
                val schema = parseSchemaFromName(file.name)
                if (schema != null && schema != AppDatabase.VERSION) return@withContext false

                val dbFile = context.getDatabasePath(DB_NAME)
                database.close()
                FileInputStream(file).use { input ->
                    FileOutputStream(dbFile, false).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                }
                database.openHelper.writableDatabase
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}
