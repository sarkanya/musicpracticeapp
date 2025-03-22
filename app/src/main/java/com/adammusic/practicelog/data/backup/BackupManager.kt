package com.adammusic.practicelog.data.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class BackupManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "BackupManager"
        private const val BACKUP_FILE_PREFIX = "practice_log_backup_"
        private const val BACKUP_FILE_EXTENSION = ".db"
        private const val DATABASE_NAME = "practice_database"

        private const val PREFS_NAME = "backup_prefs"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        private const val DEFAULT_AUTO_BACKUP_INTERVAL_DAYS = 7L
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun createBackup(): Uri? {
        try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) {
                Log.e(TAG, "Az adatbázis fájl nem létezik")
                return null
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val backupFile = File(downloadsDir, backupFileName)

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            updateLastBackupTime()

            return Uri.fromFile(backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "Hiba a mentés létrehozása közben", e)
            return null
        }
    }

    fun restoreBackup(backupFileUri: Uri): Boolean {
        try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)

            dbFile.parentFile?.mkdirs()

            if (dbFile.exists()) {
                dbFile.delete()
            }

            context.contentResolver.openInputStream(backupFileUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return false
            
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Hiba a mentés visszaállítása közben", e)
            return false
        }
    }

    fun getLatestBackupDate(): Date? {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) return null
            
            val backupFiles = downloadsDir.listFiles { file -> 
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            } ?: return null
            
            if (backupFiles.isEmpty()) return null

            val latestBackup = backupFiles.maxByOrNull { it.lastModified() } ?: return null
            
            return Date(latestBackup.lastModified())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest backup date", e)
            return null
        }
    }

    fun getAvailableBackups(): List<BackupFileInfo> {
        val backupInfoList = mutableListOf<BackupFileInfo>()
        
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) return emptyList()
            
            val backupFiles = downloadsDir.listFiles { file -> 
                file.isFile && file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
            } ?: return emptyList()
            
            for (file in backupFiles) {
                backupInfoList.add(
                    BackupFileInfo(
                        uri = Uri.fromFile(file),
                        fileName = file.name,
                        date = Date(file.lastModified()),
                        size = file.length()
                    )
                )
            }

            return backupInfoList.sortedByDescending { it.date }
        } catch (e: Exception) {
            Log.e(TAG, "Hiba a mentések betöltése közben", e)
            return emptyList()
        }
    }

    fun performAutomaticBackupIfNeeded(): Uri? {
        val lastBackupTime = getLastBackupTime()
        val currentTime = System.currentTimeMillis()

        if (lastBackupTime == 0L || 
            (currentTime - lastBackupTime) >= TimeUnit.DAYS.toMillis(DEFAULT_AUTO_BACKUP_INTERVAL_DAYS)) {
            Log.d(TAG, "Automata mentés folyamatban")
            return createBackup()
        }
        
        Log.d(TAG, "Nincs szükség automatikus mentésre")
        return null
    }

    private fun updateLastBackupTime() {
        prefs.edit { putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis()) }
    }

    private fun getLastBackupTime(): Long {
        return prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
    }
}

data class BackupFileInfo(
    val uri: Uri,
    val fileName: String,
    val date: Date,
    val size: Long
) 