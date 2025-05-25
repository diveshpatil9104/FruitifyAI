package com.example.fruitfreshdetector.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController

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
            ScanHistoryItem("Banana", "Fresh", 92.3f),
            ScanHistoryItem("Banana", "Rotten", 76.1f),
            ScanHistoryItem("Apple", "Not Checked", 88.0f),
            ScanHistoryItem("Banana", "Fresh", 80.5f)
        )
    }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Fresh", "Rotten", "Not Checked")

    val filteredItems = allHistoryItems.filter {
        (selectedFilter == "All" || it.freshness.equals(selectedFilter, ignoreCase = true)) &&
                it.fruitName.contains(searchQuery.text, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("H i s t o r y") })
        },
        content = { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    )
            ) {
                // Rounded Search Bar inside a Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { option ->
                        FilterChip(
                            selected = selectedFilter == option,
                            onClick = { selectedFilter = option },
                            label = { Text(option) }
                        )
                    }
                }

                // History List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        val encodedFruit = Uri.encode(item.fruitName)
                        val encodedFreshness = Uri.encode(item.freshness)
                        val confidenceStr = item.confidence.toString()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                                    )
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = item.fruitName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(text = "Freshness: ${item.freshness}")
                                Text(text = "Confidence: ${"%.1f".format(item.confidence)}%")
                            }
                        }
                    }
                }
            }
        }
    )
}