package com.example.bestride.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.bestride.ui.screens.LoginScreen
import com.example.bestride.ui.screens.RegisterScreen
import com.example.bestride.ui.screens.BookingScreen
import com.example.bestride.ui.screens.CabComparisonScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Booking : Screen("booking")
    object CabComparison : Screen("cab_comparison") {
        fun createRoute(
            sourceLat: Double?,
            sourceLng: Double?,
            destLat: Double?,
            destLng: Double?,
            sourceAddress: String,
            destAddress: String
        ): String {
            return "$route?" +
                    "sourceLat=${sourceLat ?: 0.0}&" +
                    "sourceLng=${sourceLng ?: 0.0}&" +
                    "destLat=${destLat ?: 0.0}&" +
                    "destLng=${destLng ?: 0.0}&" +
                    "sourceAddress=${sourceAddress}&" +
                    "destAddress=${destAddress}"
        }
    }
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Login.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Booking.route) {
            BookingScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = "${Screen.CabComparison.route}?" +
                    "sourceLat={sourceLat}&" +
                    "sourceLng={sourceLng}&" +
                    "destLat={destLat}&" +
                    "destLng={destLng}&" +
                    "sourceAddress={sourceAddress}&" +
                    "destAddress={destAddress}",
            arguments = listOf(
                navArgument("sourceLat") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("sourceLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("destLat") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("destLng") {
                    type = NavType.FloatType
                    defaultValue = 0f
                },
                navArgument("sourceAddress") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("destAddress") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            CabComparisonScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}

// Extension functions for navigation
fun NavHostController.navigateToRegister() {
    this.navigate(Screen.Register.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToCabComparison(
    sourceLat: Double?,
    sourceLng: Double?,
    destLat: Double?,
    destLng: Double?,
    sourceAddress: String,
    destAddress: String
) {
    val route = Screen.CabComparison.createRoute(
        sourceLat = sourceLat,
        sourceLng = sourceLng,
        destLat = destLat,
        destLng = destLng,
        sourceAddress = sourceAddress,
        destAddress = destAddress
    )
    this.navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToLogin() {
    this.navigate(Screen.Login.route) {
        popUpTo(Screen.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToBooking() {
    this.navigate(Screen.Booking.route) {
        popUpTo(Screen.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}