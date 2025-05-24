package com.example.fruitfreshdetector.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fruitifyai.R

@Composable
fun ResultScreen(
    fruitName: String,
    freshnessStatus: String?, // Nullable for unknown or non-banana fruits
    confidence: Float
) {
    val safeFruitName = fruitName.ifBlank { "Unknown Fruit" }
    val displayFreshness = freshnessStatus?.takeIf { it.isNotBlank() } ?: "Not available"
    val displayConfidence = if (confidence > 0f) "${(confidence * 100).toInt()}%" else "N/A"

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = safeFruitName,
                style = typography.headlineMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fruit Image inside a Card
            // Fruit Image inside a Card (smaller than card)
            Card(
                shape = RoundedCornerShape(46.dp),
//                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = getFruitImageRes(safeFruitName)),
                        contentDescription = safeFruitName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)  // image is 50% of the card's width
                            .aspectRatio(1f)     // keeps the image square
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Freshness: $displayFreshness",
                        style = typography.titleMedium,
                        color = if (freshnessStatus?.contains("Fresh", true) == true)
                            colorScheme.tertiary else colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Confidence: $displayConfidence",
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            InfoCard(
                title = "Nutrition Facts",
                content = getNutritionFacts(safeFruitName),
                iconColor = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                title = "Storage Tips",
                content = getStorageTips(safeFruitName, freshnessStatus),
                iconColor = colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String, iconColor: androidx.compose.ui.graphics.Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = iconColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Returns fruit-specific image from drawable
@DrawableRes
fun getFruitImageRes(fruit: String): Int {
    return when (fruit.lowercase()) {
        "banana" -> R.drawable.bananas
        "apple" -> R.drawable.apple
        "orange" -> R.drawable.orange
        else -> R.drawable.bananas // Default fallback image
    }
}

fun getNutritionFacts(fruit: String): String {
    return when (fruit.lowercase()) {
        "banana" -> "• Calories: 105\n• Potassium: 422 mg\n• Vitamin B6: 0.5 mg\n• Vitamin C: 10 mg\n• Fiber: 3.1 g"
        "apple" -> "• Calories: 95\n• Fiber: 4 g\n• Vitamin C: 14%\n• Potassium: 195 mg"
        "orange" -> "• Calories: 62\n• Vitamin C: 70 mg\n• Fiber: 3 g\n• Folate: 10%"
        else -> "Nutrition data not available for this fruit."
    }
}

fun getStorageTips(fruit: String, freshness: String?): String {
    return when (fruit.lowercase()) {
        "banana" -> if (freshness?.contains("fresh", true) == true) {
            "Keep at room temperature until ripe. Avoid refrigeration before ripening."
        } else {
            "Use quickly or freeze for smoothies. Avoid keeping at room temperature too long."
        }
        "apple" -> "Store in the fridge to keep them fresh for 2–3 weeks."
        "orange" -> "Store in a cool place or refrigerate. Consume within a week for best taste."
        else -> "Store in a cool, dry place. Check regularly for ripeness or spoilage."
    }
}
