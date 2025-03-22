package com.adammusic.practicelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.adammusic.practicelog.data.backup.BackupManager
import com.adammusic.practicelog.navigation.AppNavigation
import com.adammusic.practicelog.ui.theme.MusicPracticeAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var backupManager: BackupManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        performAutomaticBackup()
        
        setContent {
            MusicPracticeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
    
    private fun performAutomaticBackup() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                backupManager.performAutomaticBackupIfNeeded()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}