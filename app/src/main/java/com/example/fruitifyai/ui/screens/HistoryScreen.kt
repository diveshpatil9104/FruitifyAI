package com.example.fruitfreshdetector.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fruitifyai.data.DatabaseProvider
import com.example.fruitifyai.data.ScanResultEntity
import com.example.fruitifyai.data.ScanResultRepository
import com.example.fruitifyai.viewmodel.ScanResultViewModel
import com.example.fruitifyai.viewmodel.ScanResultViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ScanResultViewModel = viewModel(
        factory = ScanResultViewModelFactory(
            ScanResultRepository(
                DatabaseProvider.getDatabase(LocalContext.current).scanResultDao()
            )
        )
    )
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Fresh", "Rotten", "Not Checked")
    val allHistoryItems by viewModel.scanResults.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Count pinned items
    val pinnedCount by remember(allHistoryItems) {
        derivedStateOf { allHistoryItems.count { it.pinned } }
    }

    val filteredItems by remember(allHistoryItems, searchQuery, selectedFilter) {
        derivedStateOf {
            allHistoryItems.filter {
                val freshness = it.freshness ?: "Not Checked"
                (selectedFilter == "All" || freshness.equals(selectedFilter, ignoreCase = true)) &&
                        it.fruitName.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                windowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Horizontal),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
//                    actionColor = MaterialTheme.colorScheme.primary,
//                    tonalElevation = 6.dp
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                top = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchBar(searchQuery, onQueryChange = { searchQuery = it })
            }

            item {
                FilterChips(
                    selectedFilter = selectedFilter,
                    filterOptions = filterOptions,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            items(filteredItems) { item ->
                    HistoryCard(
                        item = item,
                        onClick = {
                            val encodedFruit = Uri.encode(item.fruitName)
                            val encodedFreshness = Uri.encode(item.freshness ?: "Not Checked")
                            val confidenceStr = item.confidence.toString()
                            navController.navigate(
                                "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                            )
                        },
                        onPinToggle = { wantsToPin ->
                            if (wantsToPin && pinnedCount >= 3) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Only 3 pins are allowed. Please unpin an item first.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                viewModel.updatePinned(item.id, wantsToPin)
                            }
                        },
                        onDelete = { viewModel.deleteById(item.id) }
                    )
                }
            }
        }
    }


@Composable
private fun SearchBar(
    searchQuery: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search")
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search fruit...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledIndicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    trailingIcon = {
                        if (searchQuery.text.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange(TextFieldValue("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: String,
    filterOptions: List<String>,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(filterOptions.size) { index ->
            val option = filterOptions[index]
            FilterChip(
                selected = selectedFilter == option,
                onClick = { onFilterSelected(option) },
                label = { Text(option) }
            )
        }
    }
}

@Composable
private fun HistoryCard(
    item: ScanResultEntity,
    onClick: () -> Unit,
    onPinToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Main Row Content
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fruitImageRes = getFruitImageRes(item.fruitName)
                Image(
                    painter = painterResource(id = fruitImageRes),
                    contentDescription = item.fruitName,
                    modifier = Modifier
                        .size(74.dp)
                        .padding(end = 16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.fruitName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Freshness: ${item.freshness ?: "Not Checked"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfidenceProgressBar(
                        confidence = item.confidence / 1f,
                        modifier = Modifier.fillMaxWidth(),
                        barHeight = 8.dp
                    )
                }

                // 3-dot menu
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (item.pinned) "Unpin" else "Pin") },
                            onClick = {
                                expanded = false
                                onPinToggle(!item.pinned)
                            }
                        )

                        Divider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // 📌 Pin emoji overlay
            if (item.pinned) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "star",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }
    }
}