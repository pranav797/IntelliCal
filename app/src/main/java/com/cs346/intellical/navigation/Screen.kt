package com.cs346.intellical.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Forest : Screen("forest")
    data object Camera : Screen("camera")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object Review : Screen("review")
    data object AboutIntelliCal : Screen("about")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
}
