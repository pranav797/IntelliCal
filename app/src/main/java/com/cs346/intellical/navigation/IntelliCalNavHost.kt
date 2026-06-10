package com.cs346.intellical.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cs346.intellical.ui.screens.about.AboutScreen
import com.cs346.intellical.ui.screens.camera.CameraScreen
import com.cs346.intellical.ui.screens.forest.ForestView
import com.cs346.intellical.ui.screens.home.HomeScreen
import com.cs346.intellical.ui.screens.login.LoginScreen
import com.cs346.intellical.ui.screens.login.SignUpScreen
import com.cs346.intellical.ui.screens.profile.ProfileView
import com.cs346.intellical.ui.screens.review.ReviewScreen
import com.cs346.intellical.ui.screens.settings.SettingsView
import kotlinx.coroutines.launch

@Composable
fun IntelliCalNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    val scope = rememberCoroutineScope()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onForestClick = { navController.navigate(Screen.Forest.route) },
                onCameraClick = { navController.navigate(Screen.Camera.route) },
                onLevelUp = {
                    navController.navigate(Screen.Forest.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(Screen.Forest.route) {
            ForestView(
                onLogMealClick = { navController.navigate(Screen.Camera.route) }
            )
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel(),
                onShutterClick = { navController.navigate(Screen.Review.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsView(
                onAboutClick = {
                    navController.navigate(Screen.AboutIntelliCal.route)
                },
                onLogoutClick = {
                    scope.launch {
                        com.cs346.intellical.data.AuthRepository.signOut()
                        com.cs346.intellical.domain.AppModel.reset()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileView()
        }
        
        composable(Screen.Review.route) {
            ReviewScreen(
                onConfirmClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AboutIntelliCal.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
