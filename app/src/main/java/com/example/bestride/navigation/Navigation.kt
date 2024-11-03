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
                    "sourceLat=${sourceLat ?: ""}&" +
                    "sourceLng=${sourceLng ?: ""}&" +
                    "destLat=${destLat ?: ""}&" +
                    "destLng=${destLng ?: ""}&" +
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
            route = "cab_comparison?sourceLat={sourceLat}&sourceLng={sourceLng}&destLat={destLat}&destLng={destLng}",
            arguments = listOf(
                navArgument("sourceLat") { type = NavType.StringType; nullable = true },
                navArgument("sourceLng") { type = NavType.StringType; nullable = true },
                navArgument("destLat") { type = NavType.StringType; nullable = true },
                navArgument("destLng") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val sourceLat = backStackEntry.arguments?.getString("sourceLat")?.toDoubleOrNull()
            val sourceLng = backStackEntry.arguments?.getString("sourceLng")?.toDoubleOrNull()
            val destLat = backStackEntry.arguments?.getString("destLat")?.toDoubleOrNull()
            val destLng = backStackEntry.arguments?.getString("destLng")?.toDoubleOrNull()

            CabComparisonScreen(
                navController = navController,
                sourceLat = sourceLat,
                sourceLng = sourceLng,
                destLat = destLat,
                destLng = destLng
            )
        }
    }
}



// Extension functions for navigation
fun NavHostController.navigateToRegister() {
    this.navigate(Screen.Register.route){
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
    val route = Screen.CabComparison.route +
            "?sourceLat=${sourceLat}" +
            "&sourceLng=${sourceLng}" +
            "&destLat=${destLat}" +
            "&destLng=${destLng}" +
            "&sourceAddress=${sourceAddress}" +
            "&destAddress=${destAddress}"
    this.navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToLogin() {
    this.navigate(Screen.Login.route) {
        // Clear the back stack up to login
        popUpTo(Screen.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToBooking() {
    this.navigate(Screen.Booking.route) {
        // Clear the back stack up to booking
        popUpTo(Screen.Login.route) { inclusive = true }
        launchSingleTop = true
    }
}