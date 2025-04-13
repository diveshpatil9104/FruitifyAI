package com.example.fruitfreshdetector.navigation

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
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
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
                                modifier = Modifier.size(30.dp) // Increased icon size
                            )
                        } ?: item.iconDrawable?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = item.label,
                                modifier = Modifier.size(30.dp), // Increased icon size
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

        // FAB in the center for Scan
        FloatingActionButton(
            onClick = {
                if (currentRoute != BottomNavItem.Scan.route) {
                    navController.navigate(BottomNavItem.Scan.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
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
                modifier = Modifier.size(35.dp) // Increased FAB icon size
            )
        }
    }
}