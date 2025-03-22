package com.adammusic.practicelog.ui.screens.practice.editpractice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adammusic.practicelog.data.model.Category
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPracticeScreen(
    practiceId: Int,
    onPracticeUpdated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: EditPracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.practiceState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is EditPracticeUiState.Success) {
            val practice = (uiState as EditPracticeUiState.Success).practice
            name = practice.name
            description = practice.description
            selectedCategory = practice.category
        }
    }

    val isFormValid = name.isNotBlank() && description.isNotBlank() && selectedCategory != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyakorlat szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = {
                        if (isFormValid) {
                            viewModel.updatePractice(
                                name = name,
                                description = description,
                                category = selectedCategory!!,
                                onComplete = onPracticeUpdated
                            )
                        }
                    },
                    containerColor = if (isFormValid) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mentés",
                        tint = if (isFormValid) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is EditPracticeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EditPracticeUiState.NotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Gyakorlat nem található")
                }
            }
            is EditPracticeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Gyakorlat neve") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = name.isBlank()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Leírás") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = description.isBlank()
                    )

                    OutlinedCard(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Kategória",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (selectedCategory != null) {
                                Surface(
                                    color = Color(selectedCategory!!.color).copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = selectedCategory!!.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Válassz kategóriát",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                if (showCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        title = { Text("Válassz kategóriát") },
                        text = {
                            Column {
                                categories.forEach { category ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        onClick = {
                                            selectedCategory = category
                                            showCategoryDialog = false
                                        },
                                        color = if (category == selectedCategory) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = Color(category.color).copy(alpha = 0.2f),
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = category.name,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCategoryDialog = false }) {
                                Text("Bezár")
                            }
                        }
                    )
                }
            }
        }
    }
} 