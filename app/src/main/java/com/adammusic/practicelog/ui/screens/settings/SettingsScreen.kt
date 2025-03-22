package com.adammusic.practicelog.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adammusic.practicelog.data.model.Category
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ListItem
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState()
    val deleteCategoryResult by viewModel.deleteCategoryResult.collectAsState()

    deleteCategoryResult?.let { result ->
        when (result) {
            is DeleteCategoryResult.HasPractices -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearDeleteCategoryResult() },
                    title = { Text("Nem törölhető") },
                    text = { Text("Ez a kategória nem törölhető, mert ${result.practiceCount} gyakorlat tartozik hozzá.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearDeleteCategoryResult() }) {
                            Text("Rendben")
                        }
                    }
                )
            }
            DeleteCategoryResult.Success -> {
                LaunchedEffect(Unit) {
                    viewModel.clearDeleteCategoryResult()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beállítások") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SettingsSection(
                    title = "Kategóriák",
                    onAddClick = { showAddCategoryDialog = true }
                )
            }

            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onDelete = { viewModel.deleteCategory(category.id) }
                )
            }
            
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Gyakorlási emlékeztetők") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Emlékeztetők"
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = onNavigateToReminders) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Emlékeztetőkhöz"
                            )
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToReminders() }
                )
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Biztonsági mentés és visszaállítás") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = "Biztonsági mentés és visszaállítás"
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = onNavigateToBackup) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Biztonsági mentéshez"
                            )
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToBackup() }
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategoryAdd = {  name, color ->
                viewModel.addCategory(name, color)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Hozzáadás")
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(category.name) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(category.color),
                        shape = MaterialTheme.shapes.small
                    )
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Törlés",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdd: (String, Long) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(HsvColor.from(Color(0xFF2196F3))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Új kategória") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Kategória neve") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Válassz színt:")
                HarmonyColorPicker(
                    harmonyMode = ColorHarmonyMode.NONE,
                    color = selectedColor,
                    onColorChanged = { hsvColor ->
                        selectedColor = hsvColor
                    },
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCategoryAdd(
                    categoryName,
                    selectedColor.toColor().toArgb().toLong()
                ) },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Hozzáadás")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse")
            }
        }
    )
}