package com.adammusic.practicelog.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import com.adammusic.practicelog.data.model.Category
import com.adammusic.practicelog.data.model.Practice
import java.time.LocalDateTime
import kotlinx.coroutines.launch

private enum class SortOrder(val label: String, val icon: @Composable () -> Unit) {
    NEWEST(
        "Legutóbbi először",
        { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
    ),
    OLDEST(
        "Legrégebbi először",
        { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPracticeClick: (Int) -> Unit = {},
    onNewPracticeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val practices by viewModel.practices.collectAsState()
    
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val searchBarVisible = remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || 
            (scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset > 200)
        }
    }

    LaunchedEffect(searchBarVisible.value) {
        showSearchBar = searchBarVisible.value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gyakorlatok") },
                actions = {
                    IconButton(onClick = { 
                        showSearchBar = !showSearchBar
                        if (showSearchBar) {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Keresés")
                    }
                    
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Beállítások")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewPracticeClick) {
                Icon(Icons.Default.Add, contentDescription = "Új gyakorlat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Keresés név alapján...") },
                    singleLine = true,
                    leadingIcon = { 
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
            }

            CategorySelector(
                categories = categories,
                selectedCategoryId = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rendezés:")
                
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sortOrder.icon()
                            Text(
                                text = sortOrder.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Rendezési opciók",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        option.icon()
                                        Text(option.label)
                                    }
                                },
                                onClick = {
                                    sortOrder = option
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == option) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Kiválasztva",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredPractices = practices
                    .filter {
                        (selectedCategory == null || it.category.id == selectedCategory) &&
                                it.name.contains(searchQuery, ignoreCase = true)
                    }
                    .let { practices ->
                        when (sortOrder) {
                            SortOrder.NEWEST -> practices.sortedByDescending { practice ->
                                practice.sessions.maxOfOrNull { it.date } ?: LocalDateTime.MIN
                            }
                            SortOrder.OLDEST -> practices.sortedBy { practice ->
                                practice.sessions.maxOfOrNull { it.date } ?: LocalDateTime.MIN
                            }
                        }
                    }

                items(filteredPractices) { practice ->
                    PracticeCard(
                        practice = practice,
                        onClick = { onPracticeClick(practice.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Összes") }
            )
        }

        items(categories) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(category.color)
                )
            )
        }
    }
}

@Composable
fun PracticeCard(
    practice: Practice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.wrapContentSize(),
                color = Color(practice.category.color).copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = practice.category.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = practice.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = practice.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            practice.sessions.maxByOrNull { it.date }?.let { lastSession ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Utolsó gyakorlás: ${lastSession.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Elért tempó: ${lastSession.achievedBpm} BPM",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}