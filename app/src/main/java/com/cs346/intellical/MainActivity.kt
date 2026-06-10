package com.cs346.intellical

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cs346.intellical.data.MockDatabase
import com.cs346.intellical.data.SupabaseRepository
import com.cs346.intellical.domain.AppModel
import com.cs346.intellical.navigation.IntelliCalNavHost
import com.cs346.intellical.navigation.Screen
import com.cs346.intellical.navigation.bottomNavItems
import com.cs346.intellical.ui.theme.IntelliCalTheme

class MainActivity : ComponentActivity() {

    companion object {
        // Set to true to skip login and use local seed data (MockDatabase).
        // Set to false for the real Supabase login flow.
        private const val USE_MOCK_DATA = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            IntelliCalTheme {
                var isLoading by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf(Screen.Login.route) }

                LaunchedEffect(Unit) {
                    if (USE_MOCK_DATA) {
                        AppModel.initialize(MockDatabase())
                        AppModel.load(1)
                        startDestination = Screen.Home.route
                    } else {
                        try {
                            AppModel.initialize(MockDatabase())
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to initialize AppModel", e)
                        }
                        val sessionRestored = com.cs346.intellical.data.AuthRepository.restoreSession()
                        if (sessionRestored) {
                            val userId = com.cs346.intellical.data.AuthRepository.getCurrentUserId()
                            if (userId != null && userId != 0) {
                                AppModel.initialize(SupabaseRepository())
                                AppModel.load(userId)
                                startDestination = Screen.Home.route
                            }
                        }
                    }
                    isLoading = false
                }

                if (isLoading) {
                    LoadingScreen()
                } else {
                    IntelliCalApp(startDestination)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Initializing...")
        }
    }
}

@Composable
fun IntelliCalApp(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Forest.route,
        Screen.Camera.route,
        Screen.Settings.route,
        Screen.Profile.route
    )
    
    val shouldShowBottomBar = currentDestination?.route in bottomNavScreens
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        IntelliCalNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
