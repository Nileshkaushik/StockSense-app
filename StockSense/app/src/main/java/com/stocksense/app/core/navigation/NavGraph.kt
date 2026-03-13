package com.stocksense.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stocksense.app.feature.auth.presentation.AuthViewModel
import com.stocksense.app.feature.auth.presentation.OtpVerifyScreen
import com.stocksense.app.feature.auth.presentation.PhoneAuthScreen

@Composable
fun StockSenseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Single shared ViewModel for the entire auth flow
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.PhoneAuth.route
    ) {
        composable(NavRoutes.PhoneAuth.route) {
            PhoneAuthScreen(
                onOtpSent = { phoneNumber: String ->
                    navController.navigate(
                        NavRoutes.OtpVerify.createRoute(phoneNumber)
                    )
                },
                viewModel = authViewModel
            )
        }
        composable(
            route = NavRoutes.OtpVerify.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpVerifyScreen(
                phoneNumber = phoneNumber,
                onVerified = {
                    navController.navigate(NavRoutes.LinkGmail.route) {
                        popUpTo(NavRoutes.PhoneAuth.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        composable(NavRoutes.Dashboard.route) { }
        composable(NavRoutes.LinkGmail.route) { }
        composable(NavRoutes.ConnectBroker.route) { }
        composable(NavRoutes.PermissionSetup.route) { }
    }
}