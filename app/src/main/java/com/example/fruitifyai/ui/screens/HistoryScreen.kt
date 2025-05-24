package com.example.fruitfreshdetector.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val historyItems = listOf(
        ScanHistoryItem("Banana", "Fresh", 92.3f),
        ScanHistoryItem("Banana", "Rotten", 76.1f),
        ScanHistoryItem("Apple", "Not Checked", 88.0f)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Scan History") })
        },
        content = { padding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(historyItems) { item ->
                    val encodedFruit = Uri.encode(item.fruitName)
                    val encodedFreshness = Uri.encode(item.freshness)
                    val confidenceStr = item.confidence.toString()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                navController.navigate(
                                    "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                                )
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = item.fruitName, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Freshness: ${item.freshness}")
                            Text(text = "Confidence: ${"%.1f".format(item.confidence)}%")
                        }
                    }
                }
            }
        }
    )
}