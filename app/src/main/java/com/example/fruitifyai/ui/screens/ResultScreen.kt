package com.example.fruitfreshdetector.ui.screens

import android.graphics.drawable.ColorDrawable
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    fruitName: String,
    freshnessStatus: String?,
    confidence: Float,
    onBackClick: () -> Unit
) {
    val safeFruitName = fruitName.ifBlank { "Unknown Fruit" }
    val displayFreshness = freshnessStatus?.takeIf { it.isNotBlank() } ?: "Not available"
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val scrollState = rememberScrollState()
    val showTopBar by remember { derivedStateOf { scrollState.value > 100 } }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Dynamically update status bar color based on scroll position
    LaunchedEffect(showTopBar) {
        activity?.window?.statusBarColor = if (showTopBar)
            colorScheme.surface.toArgb()
        else
            android.graphics.Color.TRANSPARENT
    }

//    Updated DisposableEffect with FIX
//    DisposableEffect(colorScheme) {
//        activity?.let { act ->
//            WindowCompat.setDecorFitsSystemWindows(act.window, false)
//            WindowCompat.getInsetsController(act.window, act.window.decorView)
//                .isAppearanceLightStatusBars = colorScheme.background.luminance() > 0.5f
//            act.window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
//        }
//
//        onDispose {
//            // ✅ Restore default inset behavior
//            activity?.let { act ->
//                WindowCompat.setDecorFitsSystemWindows(act.window, true)
//            }
//        }
//    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 92.dp)
        ) {
            val isUnknown = safeFruitName.equals("unknown", ignoreCase = true)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = if (isUnknown) Alignment.BottomCenter else Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getFruitImageRes(safeFruitName)),
                    contentDescription = safeFruitName,
                    contentScale = if (isUnknown) ContentScale.FillHeight else ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(if (isUnknown) 0.7f else 0.6f)
                        .aspectRatio(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (fruitName.lowercase() != "unknown") SuccessMessage() else AlertMessage()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = safeFruitName,
                    style = typography.displaySmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Freshness: $displayFreshness",
                    style = typography.titleMedium,
                    color = if (freshnessStatus?.contains("Fresh", true) == true)
                        colorScheme.tertiary else colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = colorScheme.onSurface.copy(alpha = 0.12f))

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Accuracy",
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))
                ConfidenceProgressBar(confidence)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = colorScheme.onSurface.copy(alpha = 0.12f))

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nutrition Facts",
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                NutritionGrid(getNutritionFacts(safeFruitName))
                HorizontalDivider(thickness = 1.dp, color = colorScheme.onSurface.copy(alpha = 0.12f))


                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Storage Tips",
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                StorageTipsSection(fruit = safeFruitName, freshness = freshnessStatus)
            }
        }

        // Back Button (always visible)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onPrimaryContainer
            )
        }

        // TopAppBar appears after scroll
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = safeFruitName.uppercase(), // Optional: make all caps like "H I S T O R Y"
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp // Character spacing
                        ),
                        color = MaterialTheme.colorScheme.primary // Match with other titles
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
        }
    }
}
@Composable
fun NutritionCard(label: String, value: String, @DrawableRes iconRes: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(165.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable
fun NutritionGrid(nutritionData: Map<String, String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp) //
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NutritionCard("Calories", nutritionData["Protein"] ?: "-", R.drawable.calories, Modifier.weight(1f))
            NutritionCard("Carbo-hy", nutritionData["Fat"] ?: "-", R.drawable.carbo, Modifier.weight(1f))
            NutritionCard("sugar", nutritionData["Carbs"] ?: "-", R.drawable.sugar, Modifier.weight(1f))

        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NutritionCard("Protein", nutritionData["protein"] ?: "-", R.drawable.protine, Modifier.weight(1f))
            NutritionCard("fiber", nutritionData["fiber"] ?: "-", R.drawable.fiber, Modifier.weight(1f))
            NutritionCard("Vitamins", nutritionData["Vitamins"] ?: "-", R.drawable.fats, Modifier.weight(1f))

        }
    }
}
fun getNutritionFacts(fruitName: String): Map<String, String> {
    return when (fruitName.lowercase()) {
        "banana" -> mapOf(
            "Protein" to "1.3g",
            "Fats" to "0.3g",
            "Carbs" to "27g",
            "Vitamins" to "C, B6"
        )
        "apple" -> mapOf(
            "Protein" to "0.5g",
            "Fats" to "0.2g",
            "Carbs" to "25g",
            "Vitamins" to "C, K"
        )
        "orange" -> mapOf(
            "Protein" to "1.2g",
            "Fats" to "0.1g",
            "Carbs" to "15g",
            "Vitamins" to "C, A"
        )
        else -> mapOf(
            "Protein" to "100g",
            "Fats" to "—",
            "Carbs" to "—",
            "Vitamins" to "—"
        )
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

@DrawableRes
fun getFruitImageRes(fruit: String): Int {
    return when (fruit.lowercase()) {
        "banana" -> R.drawable.bananas
        "apple" -> R.drawable.apple
        "orange" -> R.drawable.orange
        else -> R.drawable.unknown // Default fallback
    }
}


fun getStorageTipsList(fruit: String, freshness: String?): List<String> {
    return when (fruit.lowercase()) {
        "banana" -> if (freshness?.contains("fresh", true) == true) {
            listOf(
                "Keep at room temperature until ripe.",
                "Avoid refrigeration before ripening.",
                "Hang bananas to avoid bruising."
            )
        } else {
            listOf(
                "Use overripe bananas in smoothies or baking.",
                "Store peeled bananas in a sealed container in the freezer.",
                "Avoid leaving overripe bananas on the counter."
            )
        }

        "apple" -> listOf(
            "Store apples in the refrigerator crisper drawer.",
            "Keep away from strong-smelling foods to avoid odor absorption.",
            "Wrap partially eaten apples to reduce oxidation."
        )

        "orange" -> listOf(
            "Store at room temperature for 3–4 days.",
            "For longer storage, refrigerate in a mesh bag.",
            "Avoid airtight containers to prevent mold."
        )

        "grapes" -> listOf(
            "Keep grapes unwashed in the fridge until ready to eat.",
            "Store in a ventilated plastic bag or container.",
            "Wash and pat dry before eating."
        )

        "watermelon" -> listOf(
            "Store whole watermelons at room temperature.",
            "Refrigerate once cut, covered tightly in plastic wrap.",
            "Consume cut watermelon within 3–4 days."
        )

        "strawberry" -> listOf(
            "Refrigerate unwashed strawberries in a breathable container.",
            "Place a paper towel at the bottom to absorb moisture.",
            "Consume within 3–5 days for best taste."
        )

        else -> listOf("Store in a cool, dry place. Check for ripeness or spoilage regularly.")
    }
}
@Composable
fun ConfidenceProgressBar(
    confidence: Float,
    modifier: Modifier = Modifier,
    barHeight: Dp = 15.dp // Add customizable height
) {
    val progress = confidence.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

    val gradientBrush = when {
        progress < 0.3f -> Brush.horizontalGradient(
            listOf(Color(0xFFE53935), Color(0xFFFF7043))
        )
        progress < 0.7f -> Brush.horizontalGradient(
            listOf(Color(0xFFFFA000), Color(0xFFFFEB3B))
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0xFF8BC34A), Color(0xFF2E7D32))
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight) // Use custom height
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradientBrush)
            )
        }
    }
}
@Composable
fun StorageTipsSection(fruit: String, freshness: String?) {
    val tipsList = getStorageTipsList(fruit, freshness)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tipsList.forEach { tip ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}