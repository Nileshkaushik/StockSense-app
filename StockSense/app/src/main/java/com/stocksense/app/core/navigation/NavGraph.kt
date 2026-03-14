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
import com.stocksense.app.feature.auth.presentation.LinkGmailScreen
import com.stocksense.app.feature.auth.presentation.OtpVerifyScreen
import com.stocksense.app.feature.auth.presentation.PermissionSetupScreen
import com.stocksense.app.feature.auth.presentation.PhoneAuthScreen
import com.stocksense.app.feature.auth.presentation.SplashScreen

@Composable
fun StockSenseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route      // ← CHANGED from PhoneAuth
    ) {

        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onLoggedIn = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onGmailPending = {
                    // Phone verified but Gmail not linked — resume from LinkGmail
                    navController.navigate(NavRoutes.LinkGmail.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNotLoggedIn = {
                    navController.navigate(NavRoutes.PhoneAuth.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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

        composable(NavRoutes.PermissionSetup.route) {
            PermissionSetupScreen(
                onComplete = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ConnectBroker.route) { }
        composable(NavRoutes.Dashboard.route) { }
    }
}