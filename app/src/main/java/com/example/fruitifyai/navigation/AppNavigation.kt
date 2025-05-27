package com.example.fruitfreshdetector.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fruitfreshdetector.ui.screens.*
import com.example.fruitifyai.ui.screens.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
    ) {
        // 🏠 Home Screen
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                modifier = modifier,
                onScanClick = { navController.navigate(BottomNavItem.Scan.route) },
                navController = navController
            )
        }


        // 📸 Scan Screen — Full screen (no modifier padding)
        composable(BottomNavItem.Scan.route) {
            ScanScreen(
                navController = navController,
                modifier = Modifier.fillMaxSize(), // Full edge-to-edge
                onPrediction = { fruit, freshness, confidence ->
                    val encodedFruit = Uri.encode(fruit)
                    val encodedFreshness = Uri.encode(freshness ?: "")
                    val confidenceStr = confidence.toString()

                    navController.navigate(
                        "result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr"
                    )
                }
            )
        }

        // 📜 History Screen
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                modifier = modifier,
                navController = navController
            )
        }

        // ✅ Result Screen
        composable(
            route = "result_screen?fruitName={fruitName}&freshness={freshness}&confidence={confidence}",
            arguments = listOf(
                navArgument("fruitName") {
                    type = NavType.StringType
                    defaultValue = "Unknown"
                },
                navArgument("freshness") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("confidence") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "Unknown"
            val freshness = backStackEntry.arguments?.getString("freshness")?.takeIf { it.isNotBlank() }
            val confidenceStr = backStackEntry.arguments?.getString("confidence") ?: "0.0"
            val confidence = confidenceStr.toFloatOrNull() ?: 0f

            ResultScreen(
                fruitName = fruitName,
                freshnessStatus = freshness,
                confidence = confidence,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}