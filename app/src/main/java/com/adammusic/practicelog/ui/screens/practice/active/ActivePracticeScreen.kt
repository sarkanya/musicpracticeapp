package com.adammusic.practicelog.ui.screens.practice.active

import androidx.compose.ui.unit.min // Required for minOf Dp comparison
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adammusic.practicelog.audio.rememberMetronomeAudioHandler
import com.adammusic.practicelog.utils.MetronomeUtils.calculateBeatInterval
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class TimeSignature(
    val beats: Int,
    val noteValue: Int,
    val displayText: String = "$beats/$noteValue"
)

private val timeSignatures = listOf(
    TimeSignature(2, 4),
    TimeSignature(3, 4),
    TimeSignature(4, 4),
    TimeSignature(5, 4),
    TimeSignature(7, 4),
    TimeSignature(5, 8),
    TimeSignature(6, 8),
    TimeSignature(7, 8),
    TimeSignature(9, 8)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePracticeScreen(
    practiceId: Int,
    onBackClick: () -> Unit,
    onSessionComplete: () -> Unit,
    viewModel: ActivePracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.practiceState.collectAsState()
    
    when (uiState) {
        is ActivePracticeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ActivePracticeUiState.NotFound -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Gyakorlat nem található")
            }
        }
        is ActivePracticeUiState.Success -> {
            val practice = (uiState as ActivePracticeUiState.Success).practice
            val lastBpm = viewModel.getLastAchievedBpm(practice)
            
            var isPlaying by remember { mutableStateOf(false) }
            var currentBpm by remember { mutableIntStateOf(lastBpm) }
            var currentBeat by remember { mutableIntStateOf(0) }
            var visualBeat by remember { mutableIntStateOf(0) }
            var sessionNotes by remember { mutableStateOf("") }
            var elapsedTimeInSeconds by remember { mutableIntStateOf(0) }
            var showConfirmEndDialog by remember { mutableStateOf(false) }
            var selectedTimeSignature by remember { mutableStateOf(timeSignatures[2]) } // 4/4 by default
            var showTimeSignatureMenu by remember { mutableStateOf(false) }
            var soundsReady by remember { mutableStateOf(false) }

            LaunchedEffect(isPlaying) {
                while (isPlaying) {
                    delay(1000)
                    elapsedTimeInSeconds++
                }
            }
            
            val audioHandler = rememberMetronomeAudioHandler()

            LaunchedEffect(Unit) {
                delay(100)
                soundsReady = true
            }

            LaunchedEffect(selectedTimeSignature) {
                currentBeat = 0
                visualBeat = 0
            }

            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    var beatIntervalMs = calculateBeatInterval(currentBpm, selectedTimeSignature.noteValue)

                    visualBeat = currentBeat
                    audioHandler.playTick(currentBeat == 0)

                    var nextTickTime = System.currentTimeMillis() + beatIntervalMs

                    while (isActive) {
                        val currentTime = System.currentTimeMillis()
                        val waitTime = nextTickTime - currentTime

                        if (waitTime > 0) {
                            delay(waitTime)
                        }

                        beatIntervalMs = calculateBeatInterval(currentBpm, selectedTimeSignature.noteValue)

                        currentBeat = (currentBeat + 1) % selectedTimeSignature.beats

                        audioHandler.playTick(currentBeat == 0)
                        visualBeat = currentBeat

                        nextTickTime += beatIntervalMs
                    }
                } else {
                    currentBeat = 0
                    visualBeat = 0
                }
            }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Column {
                                Text("Aktív gyakorlás")
                                Text(
                                    text = practice.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTime(elapsedTimeInSeconds),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        OutlinedButton(
                            onClick = { showTimeSignatureMenu = true },
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Ütem: ${selectedTimeSignature.displayText}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select time signature"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTimeSignatureMenu,
                            onDismissRequest = { showTimeSignatureMenu = false }
                        ) {
                            timeSignatures.forEach { timeSignature ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = timeSignature.displayText,
                                            fontWeight = if (timeSignature == selectedTimeSignature) 
                                                FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        selectedTimeSignature = timeSignature
                                        showTimeSignatureMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    BeatVisualization(
                        timeSignature = selectedTimeSignature,
                        currentBeat = visualBeat,
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MetronomeControls(
                        currentBpm = currentBpm,
                        isPlaying = isPlaying,
                        soundsReady = soundsReady,
                        onBpmChange = { currentBpm = it },
                        onPlayPauseToggle = { isPlaying = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Jegyzetek") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showConfirmEndDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Gyakorlás befejezése")
                    }
                }
            }

            if (showConfirmEndDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmEndDialog = false },
                    title = { Text("Gyakorlás befejezése") },
                    text = { Text("Biztosan befejezed a gyakorlást?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.savePracticeSession(
                                    duration = elapsedTimeInSeconds,
                                    startingBpm = lastBpm,
                                    achievedBpm = currentBpm,
                                    notes = sessionNotes.ifBlank { null },
                                    onComplete = {
                                        showConfirmEndDialog = false
                                        onSessionComplete()
                                    }
                                )
                            }
                        ) {
                            Text("Igen")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmEndDialog = false }) {
                            Text("Nem")
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun BeatVisualization(
    timeSignature: TimeSignature,
    currentBeat: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val spacing = 8.dp
        val numBoxes = timeSignature.beats
        val numSpaces = (numBoxes - 1).coerceAtLeast(0)

        val totalSpacingWidth = spacing * numSpaces

        val availableWidthForBoxes = (maxWidth - totalSpacingWidth).coerceAtLeast(0.dp)
        val widthPerBox = if (numBoxes > 0) availableWidthForBoxes / numBoxes else 0.dp
        val maxAllowedHeight = this.constraints.maxHeight.dp
        val boxSize = min(widthPerBox, maxAllowedHeight)

        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until numBoxes) {
                val isActive = isPlaying && i == currentBeat
                val isFirstBeat = i == 0

                val alpha by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.6f,
                    animationSpec = tween(durationMillis = 100),
                    label = "BeatAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(boxSize)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                isFirstBeat -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isFirstBeat -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (i + 1).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


@Composable
private fun MetronomeControls(
    currentBpm: Int,
    isPlaying: Boolean,
    soundsReady: Boolean,
    onBpmChange: (Int) -> Unit,
    onPlayPauseToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentBpm BPM",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = { onBpmChange((currentBpm - 1).coerceIn(30, 250)) },
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = soundsReady
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Csökkentés",
                        modifier = Modifier.size(48.dp)
                    )
                }

                Button(
                    onClick = { onPlayPauseToggle(!isPlaying) },
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = soundsReady
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Szünet" else "Indítás",
                        modifier = Modifier.size(48.dp)
                    )
                }

                OutlinedButton(
                    onClick = { onBpmChange((currentBpm + 1).coerceIn(30, 250)) },
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    enabled = soundsReady
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Növelés",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}