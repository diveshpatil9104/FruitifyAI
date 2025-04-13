package com.example.fruitfreshdetector.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fruitfreshdetector.ui.screens.*
import com.example.fruitifyai.ui.screens.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier // ✅ Padding only here
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen( // ⛔ Don’t pass modifier
                onScanClick = {
                    navController.navigate(BottomNavItem.Scan.route)
                }
            )
        }
        composable(BottomNavItem.Scan.route) {
            ScanScreen() // ⛔ Don’t pass modifier again
        }
        composable(BottomNavItem.History.route) {
            HomeScreen() // ✅ Create this screen if not already
        }

    }
}