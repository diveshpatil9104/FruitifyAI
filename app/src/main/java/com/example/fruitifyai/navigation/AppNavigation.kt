package com.example.fruitfreshdetector.navigation

import android.net.Uri
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
        modifier = modifier
    ) {
        // 🏠 Home Screen
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(BottomNavItem.Scan.route)
                }
            )
        }

        // 📸 Scan Screen
        composable(BottomNavItem.Scan.route) {
            ScanScreen(onPrediction = { fruit, freshness, confidence ->
                val encodedFruit = Uri.encode(fruit)
                val encodedFreshness = Uri.encode(freshness ?: "") // Empty string if null
                val confidenceStr = confidence.toString()

                // Navigate using query parameters (more stable)
                navController.navigate("result_screen?fruitName=$encodedFruit&freshness=$encodedFreshness&confidence=$confidenceStr")
            })
        }

        // 📜 History Screen
        composable(BottomNavItem.History.route) {
            HistoryScreen(navController = navController)
        }

        // ✅ Result Screen with Nullable Freshness
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
                    type = NavType.StringType // safer than FloatType for Uri encoding
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
                confidence = confidence
            )
        }
    }
}