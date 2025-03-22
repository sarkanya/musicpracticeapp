package com.adammusic.practicelog.ui.screens.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showRestoreConfirmation by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = uiState.backupSuccess) {
        if (uiState.backupSuccess) {
            snackbarHostState.showSnackbar("Biztonsági mentés sikeresen létrejött")
            viewModel.clearSuccessMessages()
        }
    }
    
    LaunchedEffect(key1 = uiState.restoreSuccess) {
        if (uiState.restoreSuccess) {
            snackbarHostState.showSnackbar("Mentés sikeresen visszaállítva. Indítsd újra az alkalmazást a változások érvényesítéséhez.")
            viewModel.clearSuccessMessages()
        }
    }
    
    LaunchedEffect(key1 = uiState.backupError) {
        uiState.backupError?.let {
            snackbarHostState.showSnackbar("Mentés hiba: $it")
            viewModel.clearSuccessMessages()
        }
    }
    
    LaunchedEffect(key1 = uiState.restoreError) {
        uiState.restoreError?.let {
            snackbarHostState.showSnackbar("Visszaállítás hiba: $it")
            viewModel.clearSuccessMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biztonsági mentés és visszaállítás") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SimpleBackupCard(
                onBackupClick = { viewModel.createBackup() },
                onRestoreClick = { showRestoreConfirmation = true },
                isCreatingBackup = uiState.isCreatingBackup,
                isRestoring = uiState.isRestoring,
                lastBackupTime = uiState.lastBackupTime,
                formatDate = viewModel::formatDate,
                hasBackup = viewModel.hasLatestBackup()
            )
            
            CloudBackupInfo()
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text("Restore Backup") },
            text = {
                Text("Biztos, hogy a legutóbbi biztonsági mentést akarja visszaállítani? Az összes jelenlegi adatot lecseréljük.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreLatestBackup()
                        showRestoreConfirmation = false
                    }
                ) {
                    Text("Visszaállítás")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirmation = false }
                ) {
                    Text("Mégsem")
                }
            }
        )
    }
}

@Composable
fun SimpleBackupCard(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    isCreatingBackup: Boolean,
    isRestoring: Boolean,
    lastBackupTime: Date?,
    formatDate: (Date) -> String,
    hasBackup: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kézi biztonsági mentés",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (lastBackupTime != null) {
                Text(
                    text = "Utolsó mentés: ${formatDate(lastBackupTime)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = onBackupClick,
                    enabled = !isCreatingBackup && !isRestoring
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Biztonsági mentés készítése")
                }
            }
            Row(
                    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                
                Button(
                    onClick = onRestoreClick,
                    enabled = !isCreatingBackup && !isRestoring && hasBackup
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Legutóbbi visszaállítása")
                }
            }
            
            if (!hasBackup) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Még nem állnak rendelkezésre biztonsági mentések. Hozz létre egyet a kezdéshez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CloudBackupInfo() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Automatikus felhőalapú biztonsági mentés",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Az adatok automatikusan mentődnek a Google-fiókodba az Android beépített biztonsági mentési szolgáltatásának segítségével.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ha új készüléket kapsz, és ugyanazzal a Google-fiókkal jelentkezel be, az alkalmazás újratelepítésekor automatikusan helyreállnak a gyakorlások adatai.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Megjegyzés: Győződj meg róla, hogy a Google biztonsági mentés engedélyezve van a készülék beállításaiban.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 