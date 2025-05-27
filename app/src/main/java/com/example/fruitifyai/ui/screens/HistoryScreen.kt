package com.example.fruitfreshdetector.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.example.fruitifyai.R
import java.util.*

data class ScanHistoryItem(
    val fruitName: String,
    val freshness: String,
    val confidence: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val allHistoryItems = remember {
        listOf(
            ScanHistoryItem("Banana", "Fresh", 97.3f),
            ScanHistoryItem("Banana", "Rotten", 76.1f),
            ScanHistoryItem("Apple", "Not Checked", 88.0f),
            ScanHistoryItem("Banana", "Fresh", 80.5f),
            ScanHistoryItem("Banana", "Fresh", 92.3f)
        )
    }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Fresh", "Rotten", "Not Checked")

    val filteredItems by remember {
        derivedStateOf {
            allHistoryItems.filter {
                (selectedFilter == "All" || it.freshness.equals(selectedFilter, ignoreCase = true)) &&
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
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SearchBar(searchQuery, onQueryChange = { searchQuery = it })
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                FilterChips(
                    selectedFilter = selectedFilter,
                    filterOptions = filterOptions,
                    onFilterSelected = { selectedFilter = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(filteredItems) { item ->
                HistoryCard(item = item) {
                    val encodedFruit = Uri.encode(item.fruitName)
                    val encodedFreshness = Uri.encode(item.freshness)
                    val confidenceStr = item.confidence.toString()
                    navController.navigate(
                        "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                    )
                }
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
                            IconButton(onClick = {
                                onQueryChange(TextFieldValue(""))
                            }) {
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
    item: ScanHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val fruitImageRes = getFruitImageRes(item.fruitName)
            Image(
                painter = painterResource(id = fruitImageRes),
                contentDescription = item.fruitName,
                modifier = Modifier
                    .size(69.dp)
                    .padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fruitName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold , letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary

                )
                Text(
                    text = "Freshness: ${item.freshness}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 🔄 Reuse your existing confidence progress bar with adjusted width
                ConfidenceProgressBar(
                    confidence = item.confidence / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    barHeight = 8.dp // 👈 Smaller height for compact card UI
                )
            }
        }
    }
}
@Composable
private fun getFruitImageRes1(fruitName: String): Int {
    return when (fruitName.lowercase(Locale.ROOT)) {
        "banana" -> R.drawable.bananas
        "apple" -> R.drawable.apple
        // Add more fruits here
        else -> R.drawable.unknown // fallback image
    }
}