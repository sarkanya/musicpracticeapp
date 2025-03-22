package com.adammusic.practicelog.ui.screens.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        checkLatestBackup()
    }

    private fun checkLatestBackup() {
        viewModelScope.launch {
            val latestBackupDate = backupManager.getLatestBackupDate()
            if (latestBackupDate != null) {
                _uiState.update { it.copy(
                    lastBackupTime = latestBackupDate
                ) }
            }
        }
    }

    fun hasLatestBackup(): Boolean {
        return backupManager.getLatestBackupDate() != null
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBackup = true, backupError = null) }
            
            try {
                val backupUri = backupManager.createBackup()
                if (backupUri != null) {
                    _uiState.update { it.copy(
                        isCreatingBackup = false,
                        backupSuccess = true,
                        lastBackupTime = Date()
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isCreatingBackup = false,
                        backupError = "Hiba a mentés létrehozásakor"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isCreatingBackup = false,
                    backupError = "Hiba: ${e.message}"
                ) }
            }
        }
    }

    fun restoreLatestBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, restoreError = null) }
            
            try {
                val backups = backupManager.getAvailableBackups()
                if (backups.isNotEmpty()) {
                    val latestBackup = backups.first()
                    val success = backupManager.restoreBackup(latestBackup.uri)
                    
                    if (success) {
                        _uiState.update { it.copy(
                            isRestoring = false,
                            restoreSuccess = true
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            isRestoring = false,
                            restoreError = "Nem sikerült visszaállítani az automatikus mentést"
                        ) }
                    }
                } else {
                    _uiState.update { it.copy(
                        isRestoring = false,
                        restoreError = "Nincs elérhető mentés"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isRestoring = false,
                    restoreError = "Hiba: ${e.message}"
                ) }
            }
        }
    }

    fun clearSuccessMessages() {
        _uiState.update { it.copy(
            backupSuccess = false,
            restoreSuccess = false,
            backupError = null,
            restoreError = null
        ) }
    }
    
    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy MMM d, HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}

data class BackupUiState(
    val isCreatingBackup: Boolean = false,
    val isRestoring: Boolean = false,
    val lastBackupTime: Date? = null,
    val backupSuccess: Boolean = false,
    val restoreSuccess: Boolean = false,
    val backupError: String? = null,
    val restoreError: String? = null
) 