package com.example.fruitfreshdetector.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.fruitfreshdetector.ui.screens.*
import com.example.fruitifyai.ui.screens.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(BottomNavItem.Scan.route)
                }
            )
        }

        composable(BottomNavItem.Scan.route) {
            ScanScreen(onPrediction = { prediction ->
                // Pass just the string result from Scan
                navController.navigate("result_text/${prediction}")
            })
        }

        composable(BottomNavItem.History.route) {
            HistoryScreen(navController = navController)
        }

        // 📌 Result route from ScanScreen using simple prediction string
        composable(
            route = "result_text/{prediction}",
            arguments = listOf(
                navArgument("prediction") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val prediction = backStackEntry.arguments?.getString("prediction") ?: "No result"
            ResultScreen(prediction = prediction)
        }

        // 📌 Result route from HistoryScreen using individual parameters
        composable(
            route = "result/{fruit}-{freshness}-{confidence}",
            arguments = listOf(
                navArgument("fruit") { type = NavType.StringType },
                navArgument("freshness") { type = NavType.StringType },
                navArgument("confidence") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fruit = backStackEntry.arguments?.getString("fruit") ?: "Unknown"
            val freshness = backStackEntry.arguments?.getString("freshness") ?: "Unknown"
            val confidence = backStackEntry.arguments?.getString("confidence") ?: "0.0"
            val prediction = "$fruit is $freshness\nConfidence: $confidence%"

            ResultScreen(prediction = prediction)
        }
    }
}