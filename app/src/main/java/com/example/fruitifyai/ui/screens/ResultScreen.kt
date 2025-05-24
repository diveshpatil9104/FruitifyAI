package com.example.fruitfreshdetector.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fruitifyai.R
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.luminance

@Composable
fun ResultScreen(
    fruitName: String,
    freshnessStatus: String?,
    confidence: Float
) {
    val safeFruitName = fruitName.ifBlank { "Unknown Fruit" }
    val displayFreshness = freshnessStatus?.takeIf { it.isNotBlank() } ?: "Not available"
    val displayConfidence = if (confidence > 0f) "${(confidence * 100).toInt()}%" else "N/A"
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // Enable edge-to-edge in the activity
    val context = LocalContext.current
    (context as? ComponentActivity)?.let { activity ->
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        // Adjust status bar icon color based on theme (light or dark)
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = colorScheme.background.luminance() > 0.5f
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Transparent to allow image to show through
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // No insets to allow content behind system bars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 48.dp,

                )
        ) {
            // Fruit Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Increased height for prominence
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getFruitImageRes(safeFruitName)),
                    contentDescription = safeFruitName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f)
                )
            }

            // Add spacer to push content below the image
            Spacer(modifier = Modifier.height(16.dp))

            // Ensure content is not obscured by navigation bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = paddingValues.calculateBottomPadding() + 32.dp)
                    .padding(horizontal = 24.dp)
            ) {
                if (fruitName.lowercase() != "unknown") {
                    SuccessMessage()
                } else {
                    AlertMessage()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fruit Name
                Text(
                    text = safeFruitName,
                    style = typography.headlineLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Freshness",
                        value = displayFreshness,
                        valueColor = if (freshnessStatus?.contains("Fresh", true) == true)
                            colorScheme.tertiary else colorScheme.error
                    )
                    StatCard(
                        title = "Confidence",
                        value = displayConfidence,
                        valueColor = colorScheme.onSurfaceVariant
                    )
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

                // Add extra content to ensure scrolling
//                Spacer(modifier = Modifier.height(16.dp))
//                repeat(5) {
//                    Text(
//                        text = "Additional Info $it",
//                      style = typography.bodyMedium
//                   )
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
            }
        }
    }
}

// Rest of your Composables (SuccessMessage, AlertMessage, StatCard, InfoCard) remain unchanged
@Composable
private fun SuccessMessage() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFDFF5E1))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Hurray, we identified the fruit!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlertMessage() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFE0B2))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFE65100),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Sorry, we couldn't identify the fruit.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE65100),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, valueColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
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

@DrawableRes
fun getFruitImageRes(fruit: String): Int {
    return when (fruit.lowercase()) {
        "banana" -> R.drawable.bananas
        "apple" -> R.drawable.apple
        "orange" -> R.drawable.orange
        else -> R.drawable.apple // Default fallback
    }
}

fun getNutritionFacts(fruit: String): String {
    return when (fruit.lowercase()) {
        "banana" -> "• Calories: 105\n• Potassium: 422 mg\n• Vitamin B6: 0.5 mg\n• Vitamin C: 10 mg\n• Fiber: 3.1 g"
        "apple" -> "• Calories: 95\n• Fiber: 4 g\n• Vitamin C: 14%\n• Potassium: 195 mg"
        "orange" -> "• Calories: 62\n• Vitamin C: 70 mg\n• Fiber: 3 g\n• Folate: 10%"
        else -> "Nutrition data not available for  Calories: 95\\n• Fiber: 4 g\\n• Vitamin C: 14%\\n• Potassium: 195 mg\\n\" +\n" +
                "                   \"• Additional Info: Apples are excellent for heart health, aid in digestion, and are low in calories. \" +\n" +
                "                   \"They contain antioxidants like quercetin that may reduce inflammation.\\n\" Calories: 95\n" +
                "• Fiber: 4 g\n" +
                "• Vitamin C: 14%\n" +
                "• Potassium: 195 mgthis fruit."
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