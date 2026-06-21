package com.teamdobermans.dopamine_lock.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object Focus : Screen("focus")
    object Mission : Screen("mission")
    object Tasks : Screen("tasks")
    object AddTask : Screen("add_task")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
}
