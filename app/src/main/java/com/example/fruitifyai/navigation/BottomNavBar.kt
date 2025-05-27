package com.example.fruitfreshdetector.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fruitifyai.R

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconVector: ImageVector? = null,
    val iconDrawable: Int? = null
) {
    object Home : BottomNavItem("home", "Home", iconVector = Icons.Default.Home)
    object Scan : BottomNavItem("scan", "Scan", iconDrawable = R.drawable.scanner)
    object History : BottomNavItem("history", "History", iconDrawable = R.drawable.history)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.History
)

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Log.d("BottomNavBar", "Current route: $currentRoute")

    Box {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            Log.d("BottomNavBar", "Navigating to ${item.route}")
                            navController.navigate(item.route) {
                                popUpTo(BottomNavItem.Home.route) { // Explicitly pop to Home
                                    inclusive = false // Keep Home in stack
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        item.iconVector?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = item.label,
                                modifier = Modifier.size(30.dp)
                            )
                        } ?: item.iconDrawable?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = item.label,
                                modifier = Modifier.size(30.dp),
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    label = { Text(item.label) },
                    alwaysShowLabel = true
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (currentRoute != BottomNavItem.Scan.route) {
                    Log.d("BottomNavBar", "Navigating to scan")
                    navController.navigate(BottomNavItem.Scan.route) {
                        popUpTo(BottomNavItem.Home.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(74.dp),
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = R.drawable.scanner),
                contentDescription = "Scan",
                modifier = Modifier.size(35.dp)
            )
        }
    }
}