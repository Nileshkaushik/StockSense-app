package com.stocksense.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stocksense.app.feature.auth.presentation.AuthViewModel
import com.stocksense.app.feature.auth.presentation.LinkGmailScreen
import com.stocksense.app.feature.auth.presentation.OtpVerifyScreen
import com.stocksense.app.feature.auth.presentation.PhoneAuthScreen

@Composable
fun StockSenseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // ONE shared ViewModel for the entire auth flow
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.PhoneAuth.route
    ) {
        composable(NavRoutes.PhoneAuth.route) {
            PhoneAuthScreen(
                onOtpSent = { phoneNumber: String ->
                    navController.navigate(NavRoutes.OtpVerify.createRoute(phoneNumber))
                },
                viewModel = authViewModel
            )
        }

        composable(
            route = NavRoutes.OtpVerify.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
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

        composable(NavRoutes.LinkGmail.route) {
            LinkGmailScreen(
                onLinked = {
                    navController.navigate(NavRoutes.PermissionSetup.route) {
                        popUpTo(NavRoutes.PhoneAuth.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(NavRoutes.PermissionSetup.route) { /* Phase 1 next */ }
        composable(NavRoutes.ConnectBroker.route) { }
        composable(NavRoutes.Dashboard.route) { }
    }
}