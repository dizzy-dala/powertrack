package com.abdallah.powertrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abdallah.powertrack.ui.theme.screens.history.HistoryScreen
import com.abdallah.powertrack.ui.theme.screens.main.MainScreen
import com.abdallah.powertrack.ui.theme.screens.token.AddTokenScreen
import com.abdallah.powertrack.ui.theme.screens.usage.UsageScreen
import com.abdallah.powertrack.ui.theme.screens.settings.SettingsScreen
import com.abdallah.powertrack.ui.theme.screens.setup.SetupScreen
import com.abdallah.powertrack.ui.theme.screens.topup.TopUpScreen
import com.abdallah.powertrack.ui.theme.screens.welcome.WelcomeScreen
import com.abdallah.powertrack.ui.theme.screens.register.RegisterScreen
import com.abdallah.powertrack.ui.theme.screens.login.LoginScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("powertrack", 0)
    val auth = FirebaseAuth.getInstance()
    
    val currentUser = auth.currentUser
    val isSetupComplete = prefs.getBoolean("is_setup_complete", false)

    val startDestination = when {
        currentUser == null -> ROUTE_WELCOME
        !isSetupComplete -> ROUTE_SETUP
        else -> ROUTE_DASHBOARD
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(ROUTE_WELCOME) {
                WelcomeScreen {
                    navController.navigate(ROUTE_LOGIN)
                }
            }
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(ROUTE_WELCOME) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(ROUTE_REGISTER)
                    }
                )
            }
            composable(ROUTE_REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(ROUTE_SETUP) {
                            popUpTo(ROUTE_WELCOME) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(ROUTE_LOGIN)
                    }
                )
            }
            composable(ROUTE_SETUP) {
                SetupScreen {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_REGISTER) { inclusive = true }
                    }
                }
            }
            composable(ROUTE_DASHBOARD) {
                MainScreen(navController)
            }
            composable(ROUTE_ADD_TOKEN) {
                AddTokenScreen { navController.popBackStack() }
            }
            composable(ROUTE_HISTORY) {
                HistoryScreen()
            }
            composable(ROUTE_USAGE) {
                UsageScreen { navController.popBackStack() }
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(navController) { navController.popBackStack() }
            }
            composable(ROUTE_TOPUP) {
                TopUpScreen(navController)
            }
        }
    }
}
