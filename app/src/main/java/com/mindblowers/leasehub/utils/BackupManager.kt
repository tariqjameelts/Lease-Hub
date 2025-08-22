package com.mindblowers.leasehub.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
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
        private const val BACKUP_DIRECTORY = "leasehub_backups"
        private const val BACKUP_FILE_PREFIX = "leasehub_backup_"
        private const val BACKUP_FILE_EXTENSION = ".db"
    }

    /**
     * Creates a backup of the database
     * @return File object pointing to the backup file, or null if backup failed
     */
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            // Create backup directory if it doesn't exist
            val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIRECTORY)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Generate backup filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "${BACKUP_FILE_PREFIX}$timestamp$BACKUP_FILE_EXTENSION")

            // Perform database vacuum and backup
            database.openHelper.writableDatabase.let { db ->
                // Vacuum the database to optimize it before backup
                db.execSQL("VACUUM")

                // Copy the database file
                val originalDbFile = File(db.path)
                originalDbFile.copyTo(backupFile, overwrite = true)
            }

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restores the database from a backup file
     * @param backupUri The URI of the backup file to restore from
     * @return True if restore was successful, false otherwise
     */
    suspend fun restoreBackup(backupUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = getFileFromUri(backupUri) ?: return@withContext false
            val dbFile = context.getDatabasePath("shop_leasing_db")

            // Close the database before restore
            database.close()

            // Copy backup file to database location
            backupFile.copyTo(dbFile, overwrite = true)

            // Reopen the database
            database.openHelper.writableDatabase

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets a list of all available backup files
     * @return List of backup files sorted by date (newest first)
     */
    fun getAvailableBackups(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIRECTORY)
        if (!backupDir.exists() || !backupDir.isDirectory) {
            return emptyList()
        }

        return backupDir.listFiles { file ->
            file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Deletes a specific backup file
     * @param backupFile The backup file to delete
     * @return True if deletion was successful, false otherwise
     */
    fun deleteBackup(backupFile: File): Boolean {
        return try {
            backupFile.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes all backup files
     * @return Number of backup files successfully deleted
     */
    fun deleteAllBackups(): Int {
        val backups = getAvailableBackups()
        var deletedCount = 0

        backups.forEach { backup ->
            if (backup.delete()) {
                deletedCount++
            }
        }

        return deletedCount
    }

    /**
     * Gets the size of all backup files
     * @return Total size of all backups in bytes
     */
    fun getTotalBackupSize(): Long {
        return getAvailableBackups().sumOf { it.length() }
    }

    /**
     * Converts URI to File object
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> File(uri.path!!)
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val tempFile = File.createTempFile("restore_temp", ".db")
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        tempFile
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets a readable backup file size string
     */
    fun getReadableFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            sizeInBytes < 1024 * 1024 * 1024 -> "${sizeInBytes / (1024 * 1024)} MB"
            else -> "${sizeInBytes / (1024 * 1024 * 1024)} GB"
        }
    }
}