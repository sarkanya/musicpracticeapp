package com.adammusic.practicelog.ui.screens.settings.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.AlarmManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.adammusic.practicelog.data.model.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    val reminders by viewModel.reminders.collectAsState()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var canScheduleExactAlarms by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            mutableStateOf(alarmManager.canScheduleExactAlarms())
        } else {
            mutableStateOf(true)
        }
    }

    DisposableEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        
        activity?.lifecycle?.addObserver(lifecycleObserver)
        
        onDispose {
            activity?.lifecycle?.removeObserver(lifecycleObserver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyakorlás emlékeztető") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddReminderDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Emlékeztető hozzáadása")
            }
        }
    ) { padding ->
        if ((!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ||
            (!canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Text(
                            text = "Az emlékeztetők működéséhez szükséges az értesítési engedély.",
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                            Text("Engedély megadása")
                        }
                    }
                    
                    if (!canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Text(
                            text = "Az emlékeztetők megfelelő működéséhez értesítési engedély szükséges.",
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Engedély megadása")
                        }
                    }
                }
            }
        } else if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nincs értesítés beállítva.\nNyomd meg a + gombot a hozzáadáshoz.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onToggleEnabled = { viewModel.toggleReminderEnabled(reminder.id, it) },
                        onDelete = { viewModel.deleteReminder(reminder.id) }
                    )
                }
            }
        }
    }

    if (showAddReminderDialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms) {
            AlertDialog(
                onDismissRequest = { showAddReminderDialog = false },
                title = { Text("Engedély szükséges") },
                text = { Text("Az emlékeztetők megfelelő működéséhez értesítési engedély szükséges. Kérlek add hozzá az engedélyt a beállításokban.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                            showAddReminderDialog = false
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddReminderDialog = false }) {
                        Text("Mégsem")
                    }
                }
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            AlertDialog(
                onDismissRequest = { showAddReminderDialog = false },
                title = { Text("Engedély szükséges") },
                text = { Text("Az emlékeztetők megfelelő működéséhez értesítési engedély szükséges.") },
                confirmButton = {
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            showAddReminderDialog = false
                        }
                    ) {
                        Text("Engedély megadása")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddReminderDialog = false }) {
                        Text("Mégsem")
                    }
                }
            )
        } else {
            AddReminderDialog(
                onDismiss = { showAddReminderDialog = false },
                onAddReminder = { days, hour, minute ->
                    viewModel.addReminder(days, hour, minute)
                    showAddReminderDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(reminder.hourOfDay, reminder.minute),
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Row {
                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = onToggleEnabled
                    )
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Törlés",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Napok: ${formatDays(reminder.days)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAddReminder: (List<Int>, Int, Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gyakorlási emlékeztető hozzáadása") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Válassz napokat:")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayToggleButton("H", 1, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("K", 2, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("SZ", 3, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("CS", 4, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("P", 5, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("SZO", 6, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                    DayToggleButton("V", 7, selectedDays) { day, selected ->
                        selectedDays = if (selected) selectedDays + day else selectedDays - day
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Idő beállítása: ${formatTime(hour, minute)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onAddReminder(selectedDays.toList(), hour, minute) 
                },
                enabled = selectedDays.isNotEmpty()
            ) {
                Text("Hozzáadás")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégsem")
            }
        }
    )
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Idő választása") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        hour = timePickerState.hour
                        minute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Mégsem")
                }
            }
        )
    }
}

@Composable
fun DayToggleButton(
    label: String,
    day: Int,
    selectedDays: Set<Int>,
    onToggle: (Int, Boolean) -> Unit
) {
    val selected = selectedDays.contains(day)
    
    FilledTonalButton(
        onClick = { onToggle(day, !selected) },
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(label)
    }
}

fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}

fun formatDays(days: List<Int>): String {
    if (days.isEmpty()) return "Nincs"
    
    val dayNames = days.sorted().map { day ->
        when (day) {
            1 -> "Hétfő"
            2 -> "Kedd"
            3 -> "Szerda"
            4 -> "Csütörtök"
            5 -> "Péntek"
            6 -> "Szombat"
            7 -> "Vasárnap"
            else -> ""
        }
    }
    
    return dayNames.joinToString(", ")
} 