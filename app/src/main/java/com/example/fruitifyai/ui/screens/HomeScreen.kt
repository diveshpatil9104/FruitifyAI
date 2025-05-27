package com.example.fruitifyai.ui.screens

import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fruitifyai.R
import com.example.fruitifyai.data.DatabaseProvider
import com.example.fruitifyai.data.ScanResultEntity
import com.example.fruitifyai.data.ScanResultRepository
import com.example.fruitifyai.viewmodel.ScanResultViewModel
import com.example.fruitifyai.viewmodel.ScanResultViewModelFactory
import com.example.fruitfreshdetector.navigation.BottomNavItem
import com.example.fruitfreshdetector.ui.screens.getFruitImageRes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit = {},
    navController: NavController,
    viewModel: ScanResultViewModel = viewModel(
        factory = ScanResultViewModelFactory(
            ScanResultRepository(
                DatabaseProvider.getDatabase(LocalContext.current).scanResultDao()
            )
        )
    )
) {
    val allHistoryItems by viewModel.scanResults.collectAsState(initial = emptyList())

    LaunchedEffect(allHistoryItems) {
        Log.d("HomeScreen", "allHistoryItems size: ${allHistoryItems.size}")
        allHistoryItems.forEach { Log.d("HomeScreen", "Scan: $it") }
    }

    // Analytics data
    val currentTime = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L // 1 day
    val weekMillis = 7 * dayMillis // 7 days
    val monthMillis = 30 * dayMillis // 30 days
    val yearMillis = 365 * dayMillis // 365 days

    // Daily data (for today)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val dailyScans = allHistoryItems.filter {
        it.timestamp in startOfToday..currentTime
    }
    val dailyTotalScans = dailyScans.size
    val dailyFreshCount = dailyScans.count { it.freshness == "Fresh" }
    val dailyRottenCount = dailyScans.count { it.freshness == "Rotten" }

    // Time period selection state
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    val timePeriods = listOf("Weekly", "Monthly", "Yearly", "All")

    // Filtered data based on selected period
    val filteredScans = when (selectedPeriod) {
        "Weekly" -> allHistoryItems.filter { it.timestamp >= currentTime - weekMillis }
        "Monthly" -> allHistoryItems.filter { it.timestamp >= currentTime - monthMillis }
        "Yearly" -> allHistoryItems.filter { it.timestamp >= currentTime - yearMillis }
        "All" -> allHistoryItems
        else -> allHistoryItems
    }

    val totalScans = filteredScans.size
    val freshCount = filteredScans.count { it.freshness == "Fresh" }
    val rottenCount = filteredScans.count { it.freshness == "Rotten" }
    val freshPercent = if (totalScans > 0) (freshCount * 100f / totalScans).toInt() else 0
    val rottenPercent = if (totalScans > 0) (rottenCount * 100f / totalScans).toInt() else 0
    val mostScannedFruit = filteredScans.groupBy { it.fruitName }
        .maxByOrNull { it.value.size }?.key ?: "None"

    // Scan trend data
    val trendData = when (selectedPeriod) {
        "Weekly" -> (0..6).map { day ->
            val startOfDay = currentTime - (day + 1) * dayMillis
            val endOfDay = currentTime - day * dayMillis
            filteredScans.count { it.timestamp in startOfDay..endOfDay }
        }.reversed()
        "Monthly" -> (0..3).map { week ->
            val startOfWeek = currentTime - (week + 1) * weekMillis
            val endOfWeek = currentTime - week * weekMillis
            filteredScans.count { it.timestamp in startOfWeek..endOfWeek }
        }.reversed()
        "Yearly" -> (0..11).map { month ->
            val startOfMonth = currentTime - (month + 1) * monthMillis
            val endOfMonth = currentTime - month * monthMillis
            filteredScans.count { it.timestamp in startOfMonth..endOfMonth }
        }.reversed()
        "All" -> {
            val years = ((allHistoryItems.maxOfOrNull { it.timestamp } ?: currentTime) -
                    (allHistoryItems.minOfOrNull { it.timestamp } ?: currentTime)) / yearMillis + 1
            (0 until years.toInt()).map { year ->
                val startOfYear = currentTime - (year + 1) * yearMillis
                val endOfYear = currentTime - year * yearMillis
                filteredScans.count { it.timestamp in startOfYear..endOfYear }
            }.reversed()
        }
        else -> emptyList()
    }

    val recentScans = allHistoryItems.sortedByDescending { it.timestamp }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                Text(
                    text = "FruitifyAI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                val carouselItems: List<Pair<String, Int>> = listOf(
                    Pair("Banana Facts", R.drawable.banana),
                    Pair("Healthy Tips", R.drawable.healthy)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(carouselItems) { item ->
                        val (title, imageRes) = item
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .width(280.dp)
                                .height(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = "$title Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Text(
                                    text = if (title == "Banana Facts") "Did you know? Bananas are berries!" else "Stay hydrated, eat fruits daily.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Detect fruit freshness instantly with your camera.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onScanClick,
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Scan Now", fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                DailySummaryCard(
                    totalScans = dailyTotalScans,
                    freshCount = dailyFreshCount,
                    rottenCount = dailyRottenCount
                )
            }

            item {
                AnalyticsSummarySection(
                    trendData = trendData,
                    freshPercent = freshPercent,
                    rottenPercent = rottenPercent,
                    totalScans = totalScans,
                    freshCount = freshCount,
                    rottenCount = rottenCount,
                    mostScannedFruit = mostScannedFruit,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    timePeriods = timePeriods
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕓 Recent Scans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = {
                        Log.d("HomeScreen", "Navigating to History")
                        navController.navigate(BottomNavItem.History.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (recentScans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "No recent scans available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentScans.take(4)) { item ->
                    RecentScanCard(
                        item = item,
                        onClick = {
                            val encodedFruit = Uri.encode(item.fruitName)
                            val encodedFreshness = Uri.encode(item.freshness ?: "Not Checked")
                            val confidenceStr = item.confidence.toString()
                            navController.navigate(
                                "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                            )
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DailySummaryCard(
    totalScans: Int,
    freshCount: Int,
    rottenCount: Int
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Scans: $totalScans",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Fresh: $freshCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Rotten: $rottenCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF44336)
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.scanner),
                    contentDescription = "Stats Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsSummarySection(
    trendData: List<Int>,
    freshPercent: Int,
    rottenPercent: Int,
    totalScans: Int,
    freshCount: Int,
    rottenCount: Int,
    mostScannedFruit: String,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    timePeriods: List<String>
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Analytical Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            TimePeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                timePeriods = timePeriods
            )
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 0.dp, end = 18.dp),
            pageSpacing = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) { page ->
            when (page) {
                0 -> ScanSummaryCard(
                    totalScans = totalScans,
                    freshCount = freshCount,
                    rottenCount = rottenCount,
                    mostScannedFruit = mostScannedFruit
                )
                1 -> ScanTrendCard(
                    trendData = trendData,
                    selectedPeriod = selectedPeriod
                )
                2 -> FreshRottenPieChart(
                    freshPercent = freshPercent,
                    rottenPercent = rottenPercent
                )
                3 -> FreshRottenDonutChart(
                    freshPercent = freshPercent,
                    rottenPercent = rottenPercent
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(4) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePeriodDropdown(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    timePeriods: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(150.dp)
    ) {
        TextField(
            value = selectedPeriod,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            timePeriods.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period) },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
@Composable
private fun ScanSummaryCard(
    totalScans: Int,
    freshCount: Int,
    rottenCount: Int,
    mostScannedFruit: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(320.dp)
            .height(220.dp)
            .hoverable(interactionSource)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Scan Summary",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp)) // Spacing after title
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow(
                    iconRes = R.drawable.scanner, // Replace with your total scans icon
                    text = "Total Scans: $totalScans",
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    useTint = true // Tint for single-color icon
                )
                StatRow(
                    iconRes = R.drawable.orange, // Replace with your fresh icon
                    text = "Fresh: $freshCount",
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer, // Neutral color
                    useTint = false // No tint to preserve original colors
                )
                StatRow(
                    iconRes = R.drawable.apple, // Replace with your rotten icon
                    text = "Rotten: $rottenCount",
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer, // Neutral color
                    useTint = false // No tint to preserve original colors
                )
                StatRow(
                    iconRes = getFruitImageRes(mostScannedFruit), // Fruit image as icon
                    text = "Most Scanned: $mostScannedFruit",
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    useTint = false // No tint to preserve original colors
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    iconRes: Int,
    text: String,
    textColor: Color,
    useTint: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (useTint) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = textColor, // Apply tint for single-color icons
                modifier = Modifier.size(24.dp)
            )
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                contentScale = ContentScale.Fit, // Ensure image fits
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape) // Circular frame
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}
@Composable
private fun ScanTrendCard(trendData: List<Int>, selectedPeriod: String) {
    val maxBarHeight = 120.dp
    val barWidth = 15.dp // Reduced to fit 12 bars
    val maxScans = trendData.maxOrNull()?.coerceAtLeast(1) ?: 1
    val animatedHeights = trendData.map { count ->
        animateFloatAsState(
            targetValue = (count.toFloat() / maxScans).coerceIn(0f, 1f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ).value
    }

    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val maxBarHeightPx = with(density) { maxBarHeight.toPx() }
    val barWidthPx = with(density) { barWidth.toPx() }
    val barSpacingPx = with(density) { 8.dp.toPx() } // Adjusted spacing
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 100)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(320.dp)
            .height(220.dp)
            .hoverable(interactionSource)
            .scale(scale)
            .alpha(alpha)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trend), // Replace with your trend icon
                    contentDescription = "Trend Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = when (selectedPeriod) {
                        "Weekly" -> "Weekly Scan Trend"
                        "Monthly" -> "Monthly Scan Trend"
                        "Yearly" -> "Yearly Scan Trend"
                        "All" -> "All-Time Scan Trend"
                        else -> "Scan Trend"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp) // Added padding to prevent clipping
            ) {
                val totalBarsWidth = (trendData.size * barWidthPx) + ((trendData.size - 1) * barSpacingPx)
                val startX = (size.width - totalBarsWidth) / 2
                val endX = startX + totalBarsWidth

                // Grid lines and labels
                val gridLineCount = 5
                val gridStep = maxBarHeightPx / (gridLineCount - 1)
                val valueStep = maxScans.toFloat() / (gridLineCount - 1)

                for (i in 0 until gridLineCount) {
                    val y = size.height - (i * gridStep)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f
                    )
                    val value = (i * valueStep).toInt()
                    val yAxisPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 18f
                        textAlign = Paint.Align.RIGHT
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        value.toString(),
                        startX - 12f,
                        y + 6f,
                        yAxisPaint
                    )
                }

                // Draw bars and labels
                trendData.forEachIndexed { index, count ->
                    val barHeight = animatedHeights[index] * maxBarHeightPx
                    val x = startX + index * (barWidthPx + barSpacingPx)
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidthPx, barHeight),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Fill
                    )
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 16f // Reduced to prevent overlap
                        textAlign = Paint.Align.CENTER
                    }
                    val label = when (selectedPeriod) {
                        "Weekly" -> "D${trendData.size - index}"
                        "Monthly" -> "W${trendData.size - index}"
                        "Yearly" -> "M${trendData.size - index}"
                        "All" -> "Y${trendData.size - index}"
                        else -> "${trendData.size - index}"
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x + barWidthPx / 2,
                        size.height + 20f,
                        textPaint
                    )
                }
            }
        }
    }
}

@Composable
private fun FreshRottenPieChart(freshPercent: Int, rottenPercent: Int) {
    val freshAngle by animateFloatAsState(
        targetValue = (freshPercent / 100f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val rottenAngle by animateFloatAsState(
        targetValue = (rottenPercent / 100f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(320.dp)
            .height(220.dp)
            .hoverable(interactionSource)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Fresh vs Rotten",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val radius = size.minDimension / 2
                    var startAngle = 0f

                    // Fresh slice
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = startAngle,
                        sweepAngle = freshAngle,
                        useCenter = true,
                        style = Fill
                    )
                    startAngle += freshAngle

                    // Rotten slice
                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = startAngle,
                        sweepAngle = rottenAngle,
                        useCenter = true,
                        style = Fill
                    )

                    // Percentage labels
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 20f
                        textAlign = Paint.Align.CENTER
                    }
                    if (freshPercent > 0) {
                        val freshMidAngle = (freshAngle / 2).toDouble()
                        val freshLabelX = (radius * 0.6f * kotlin.math.cos(Math.toRadians(freshMidAngle))).toFloat() + size.width / 2
                        val freshLabelY = (radius * 0.6f * kotlin.math.sin(Math.toRadians(freshMidAngle))).toFloat() + size.height / 2 + 6f
                        drawContext.canvas.nativeCanvas.drawText(
                            "$freshPercent%",
                            freshLabelX,
                            freshLabelY,
                            textPaint
                        )
                    }
                    if (rottenPercent > 0) {
                        val rottenMidAngle = freshAngle + (rottenAngle / 2).toDouble()
                        val rottenLabelX = (radius * 0.6f * kotlin.math.cos(Math.toRadians(rottenMidAngle))).toFloat() + size.width / 2
                        val rottenLabelY = (radius * 0.6f * kotlin.math.sin(Math.toRadians(rottenMidAngle))).toFloat() + size.height / 2 + 6f
                        drawContext.canvas.nativeCanvas.drawText(
                            "$rottenPercent%",
                            rottenLabelX,
                            rottenLabelY,
                            textPaint
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fresh: $freshPercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFF44336), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rotten: $rottenPercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreshRottenDonutChart(freshPercent: Int, rottenPercent: Int) {
    val freshAngle by animateFloatAsState(
        targetValue = (freshPercent / 100f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val rottenAngle by animateFloatAsState(
        targetValue = (rottenPercent / 100f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val centerTextScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    val density = LocalDensity.current
    val backgroundRingColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val strokeWidth = with(density) { 16.dp.toPx() }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(320.dp)
            .height(220.dp)
            .hoverable(interactionSource)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Freshness Breakdown",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val radius = size.minDimension / 2
                    var startAngle = 0f

                    drawArc(
                        color = backgroundRingColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )

                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = startAngle,
                        sweepAngle = freshAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += freshAngle

                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = startAngle,
                        sweepAngle = rottenAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }

                Text(
                    text = "${freshPercent + rottenPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.scale(centerTextScale)
                )
            }
        }
    }
}

@Composable
private fun RecentScanCard(
    item: ScanResultEntity,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getFruitImageRes(item.fruitName)),
                    contentDescription = item.fruitName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fruitName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Freshness: ${item.freshness ?: "Not Checked"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Confidence: ${(item.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Scanned: ${formatTimestamp(item.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}