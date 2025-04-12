package com.adammusic.practicelog.ui.screens.practice.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adammusic.practicelog.data.model.Practice
import com.adammusic.practicelog.data.model.PracticeSession
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeDetailScreen(
    practiceId: Int,
    onStartPractice: () -> Unit,
    onBackClick: () -> Unit,
    onEditPractice: () -> Unit,
    onSessionClick: (Int) -> Unit,
    onPracticeDeleted: () -> Unit,
    viewModel: PracticeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.practiceState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    when (uiState) {
                        is PracticeDetailUiState.Success -> Text((uiState as PracticeDetailUiState.Success).practice.name)
                        else -> Text("Gyakorlat részletei")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                },
                actions = {
                    if (uiState is PracticeDetailUiState.Success) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Törlés"
                            )
                        }

                        IconButton(onClick = onEditPractice) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Szerkesztés"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is PracticeDetailUiState.Success) {
                FloatingActionButton(onClick = onStartPractice) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Gyakorlás indítása")
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is PracticeDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PracticeDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gyakorlat nem található")
                }
            }
            is PracticeDetailUiState.Success -> {
                val practice = (uiState as PracticeDetailUiState.Success).practice
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    item {
                        HeaderSection(practice)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            "Korábbi gyakorlások",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (practice.sessions.isEmpty()) {
                        item {
                            Text(
                                "Még nincs rögzített gyakorlás",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        items(practice.sessions.sortedByDescending { it.date }) { session ->
                            SessionCard(
                                session = session,
                                onClick = { onSessionClick(session.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Text(
                                "Gyakorlási statisztikák",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PracticeStatistics(practice.sessions)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Text(
                                "Fejlődési grafikon",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BpmChart(practice.sessions)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Gyakorlat törlése") },
            text = { 
                Text("Biztosan törölni szeretnéd ezt a gyakorlatot? Ez a művelet nem vonható vissza, és az összes hozzá tartozó gyakorlás is törlődni fog.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePractice()
                        showDeleteConfirmation = false
                        onPracticeDeleted()
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

@Composable
private fun HeaderSection(practice: Practice) {
    val categoryColor = Color(practice.category.color)
    val luminance = categoryColor.luminance()
    val textColor = if (luminance > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
    Column {
        Surface(
            color = categoryColor,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = practice.category.name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = practice.description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SessionCard(
    session: PracticeSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm")),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Időtartam: ${formatDuration(session.duration)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "BPM: ${if (session.startingBpm == session.achievedBpm) session.startingBpm else "${session.startingBpm} -> ${session.achievedBpm}"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            session.notes?.let { notes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BpmChart(sessions: List<PracticeSession>) {
    val sortedSessions = remember { sessions.sortedBy { it.date } }

    if (sortedSessions.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Még nincs elég adat a grafikon megjelenítéséhez",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val achievedBpms = sortedSessions.map { it.achievedBpm }
    val maxBpm = remember { achievedBpms.maxOrNull()?.toFloat() ?: 0f }
    val minBpm = remember { 
        val min = achievedBpms.minOrNull()?.toFloat() ?: 0f
        (min * 0.9f).coerceAtLeast(0f)
    }
    val bpmRange = maxBpm - minBpm

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM.dd") }
    val dates = sortedSessions.map { it.date.format(dateFormatter) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            "BPM Fejlődés",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(200.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = maxBpm.toInt().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ((maxBpm + minBpm) / 2).toInt().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = minBpm.toInt().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceColor = MaterialTheme.colorScheme.surface
            val gridLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(start = 24.dp)
            ) {
                val width = size.width
                val height = size.height
                val xStep = width / (sortedSessions.size - 1)
                val yScale = height / bpmRange

                val dashPathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, 0f),
                    end = Offset(width, 0f),
                    strokeWidth = 1f,
                    pathEffect = dashPathEffect
                )
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, height / 2),
                    end = Offset(width, height / 2),
                    strokeWidth = 1f,
                    pathEffect = dashPathEffect
                )
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, height),
                    end = Offset(width, height),
                    strokeWidth = 1f,
                    pathEffect = dashPathEffect
                )

                val points = sortedSessions.mapIndexed { index, session ->
                    val x = index * xStep
                    val y = height - ((session.achievedBpm - minBpm) * yScale)
                    Offset(x, y)
                }

                val path = androidx.compose.ui.graphics.Path()
                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                points.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = surfaceColor,
                        radius = 3f,
                        center = point
                    )
                }
            }
        }

        if (sortedSessions.size <= 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dates.forEach { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dates.first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dates[dates.size / 2],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dates.last(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Elért BPM", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PracticeStatistics(sessions: List<PracticeSession>) {
    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Még nincs elég adat a statisztikák megjelenítéséhez",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val totalSessions = sessions.size
    val totalDuration = sessions.sumOf { it.duration }
    val averageDuration = totalDuration / totalSessions

    val firstSessionDate = sessions.minByOrNull { it.date }?.date
    val lastSessionDate = sessions.maxByOrNull { it.date }?.date
    
    val frequencyText = if (firstSessionDate != null && lastSessionDate != null) {
        val daysBetween = java.time.Duration.between(firstSessionDate, lastSessionDate).toDays() + 1
        val weeksActive = (daysBetween / 7.0).coerceAtLeast(1.0)
        val sessionsPerWeek = (totalSessions / weeksActive).toFloat()
        String.format("%.1f alkalom/hét", sessionsPerWeek)
    } else {
        "Nem meghatározható"
    }

    val lastPracticeDate = sessions.maxByOrNull { it.date }?.date
    val lastPracticeDateText = lastPracticeDate?.format(DateTimeFormatter.ofPattern("yyyy. MM. dd.")) ?: "Nincs adat"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatisticItem(
                title = "Összes gyakorlás",
                value = totalSessions.toString() + " alkalom"
            )
            StatisticItem(
                title = "Gyakoriság",
                value = frequencyText
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatisticItem(
                title = "Átlagos időtartam",
                value = formatDuration(averageDuration)
            )
            StatisticItem(
                title = "Összes időtartam",
                value = formatDuration(totalDuration)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatisticItem(
                title = "Utolsó gyakorlás",
                value = lastPracticeDateText
            )
        }
    }
}

@Composable
private fun StatisticItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatDuration(durationInSeconds: Int): String {
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}