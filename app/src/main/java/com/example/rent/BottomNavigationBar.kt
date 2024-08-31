package com.example.rent

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person


@Composable
fun BottomNavigationBar(navController: NavHostController, currentUser: FirebaseUser?) {
    val homeIcon = Icons.Filled.Home
    val profileIcon = Icons.Filled.Person
    val loginIcon = Icons.Filled.AccountCircle
    val registerIcon = Icons.Filled.AddCircle

    val items = listOf(
        BottomNavItem("Home", "cars", homeIcon),
        BottomNavItem("Profile", "profile", profileIcon)
    )
    val auth = listOf(
        BottomNavItem("Login", "login", loginIcon),
        BottomNavItem("Register", "register", registerIcon)
    )

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        if (currentUser != null) {
            items.forEach { item ->
                BottomNavigationItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    label = { Text(item.label, fontSize = 18.sp) },
                    selected = currentRoute == item.route,
                    selectedContentColor = Color.Black,
                    unselectedContentColor = Color.LightGray,
                    onClick = {
                        if (navController.graph.findNode(item.route) != null) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        }
                    },
                    alwaysShowLabel = true
                )
            }
        } else {
            auth.forEach { authItem ->
                BottomNavigationItem(
                    icon = { Icon(imageVector = authItem.icon, contentDescription = null) },
                    label = { Text(authItem.label, fontSize = 18.sp) },
                    selected = currentRoute == authItem.route,
                    onClick = {
                        if (navController.graph.findNode(authItem.route) != null) {
                            navController.navigate(authItem.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        }
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }
}



data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
