package com.adammusic.practicelog.ui.screens.practice.editsession

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adammusic.practicelog.data.model.PracticeSession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSessionScreen(
    onBackClick: () -> Unit,
    onSessionUpdated: () -> Unit,
    onSessionDeleted: () -> Unit,
    viewModel: EditSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyakorlás szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Törlés"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is EditSessionUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EditSessionUiState.NotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gyakorlás nem található")
                }
            }
            is EditSessionUiState.Success -> {
                val state = uiState as EditSessionUiState.Success
                val session = state.session
                
                SessionEditForm(
                    session = session,
                    onSave = { date, duration, startingBpm, achievedBpm, notes ->
                        viewModel.updateSession(
                            date = date,
                            duration = duration,
                            startingBpm = startingBpm,
                            achievedBpm = achievedBpm,
                            notes = notes
                        )
                        onSessionUpdated()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Gyakorlás törlése") },
            text = { Text("Biztosan törölni szeretnéd ezt a gyakorlást? Ez a művelet nem vonható vissza.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession()
                        showDeleteConfirmation = false
                        onSessionDeleted()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Törlés")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Mégsem")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionEditForm(
    session: PracticeSession,
    onSave: (LocalDateTime, Int, Int, Int, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var date by remember { mutableStateOf(session.date.toLocalDate()) }
    var time by remember { mutableStateOf(session.date.toLocalTime()) }
    var duration by remember { mutableStateOf(session.duration.toString()) }
    var startingBpm by remember { mutableStateOf(session.startingBpm.toString()) }
    var achievedBpm by remember { mutableStateOf(session.achievedBpm.toString()) }
    var notes by remember { mutableStateOf(session.notes ?: "") }

    var durationError by remember { mutableStateOf(false) }
    var startingBpmError by remember { mutableStateOf(false) }
    var achievedBpmError by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DateTimePickerField(
            date = date,
            time = time,
            onDateChange = { date = it },
            onTimeChange = { time = it }
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { 
                duration = it
                durationError = it.toIntOrNull() == null || it.toIntOrNull()!! <= 0
            },
            label = { Text("Időtartam (másodperc)") },
            isError = durationError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (durationError) {
                    Text("Érvényes időtartamot adj meg")
                }
            }
        )

        OutlinedTextField(
            value = startingBpm,
            onValueChange = { 
                startingBpm = it
                startingBpmError = it.toIntOrNull() == null || it.toIntOrNull()!! <= 0
            },
            label = { Text("Kezdő BPM") },
            isError = startingBpmError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (startingBpmError) {
                    Text("Érvényes BPM értéket adj meg")
                }
            }
        )

        OutlinedTextField(
            value = achievedBpm,
            onValueChange = { 
                achievedBpm = it
                achievedBpmError = it.toIntOrNull() == null || it.toIntOrNull()!! <= 0
            },
            label = { Text("Elért BPM") },
            isError = achievedBpmError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (achievedBpmError) {
                    Text("Érvényes BPM értéket adj meg")
                }
            }
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Megjegyzések") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Button(
            onClick = {
                val dateTime = LocalDateTime.of(date, time)
                onSave(
                    dateTime,
                    duration.toIntOrNull() ?: 0,
                    startingBpm.toIntOrNull() ?: 0,
                    achievedBpm.toIntOrNull() ?: 0,
                    notes.ifBlank { null }
                )
            },
            enabled = !durationError && !startingBpmError && !achievedBpmError &&
                    duration.isNotBlank() && startingBpm.isNotBlank() && achievedBpm.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mentés")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerField(
    date: LocalDate,
    time: LocalTime,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd.")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Időpont",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Dátum",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Dátum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Időpont",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Időpont",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = time.format(timeFormatter),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateChange(newDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Mégsem")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(onClick = {
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeChange(newTime)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Mégsem")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    )
} 