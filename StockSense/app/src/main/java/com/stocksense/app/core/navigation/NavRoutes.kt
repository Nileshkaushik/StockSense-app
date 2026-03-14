package com.stocksense.app.core.navigation

sealed class NavRoutes(val route: String) {

    // Auth flow
    object Splash : NavRoutes("splash")
    object PhoneAuth : NavRoutes("phone_auth")
    object OtpVerify : NavRoutes("otp_verify/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verify/$phoneNumber"
    }
    object LinkGmail : NavRoutes("link_gmail")

    // Main app
    object Dashboard : NavRoutes("dashboard")
    object StockDetail : NavRoutes("stock_detail/{stockSymbol}") {
        fun createRoute(stockSymbol: String) = "stock_detail/$stockSymbol"
    }
    object Alerts : NavRoutes("alerts")
    object History : NavRoutes("history")
    object Settings : NavRoutes("settings")

    // Onboarding
    object ConnectBroker : NavRoutes("connect_broker")
    object PermissionSetup : NavRoutes("permission_setup")
}