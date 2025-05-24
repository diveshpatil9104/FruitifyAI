package com.example.fruitifyai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fruitfreshdetector.navigation.AppNavigation
import com.example.fruitfreshdetector.navigation.BottomNavBar
import com.example.fruitfreshdetector.navigation.BottomNavItem
import com.example.fruitifyai.ui.theme.FruitifyAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status bar transparent and enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        enableEdgeToEdge()

        setContent {
            FruitifyAITheme {
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                val showBottomBar = currentRoute != BottomNavItem.Scan.route

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    // Pass innerPadding only to screens that need it (not ScanScreen)
                    AppNavigation(
                        navController = navController,
                        modifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier
                    )
                }
            }
        }
    }
}