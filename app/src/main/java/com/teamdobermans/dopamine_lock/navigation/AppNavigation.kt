package com.teamdobermans.dopamine_lock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teamdobermans.dopamine_lock.ui.analytics.AnalyticsScreen
import com.teamdobermans.dopamine_lock.ui.auth.ForgotPasswordScreen
import com.teamdobermans.dopamine_lock.ui.auth.LoginScreen
import com.teamdobermans.dopamine_lock.ui.auth.RegisterScreen
import com.teamdobermans.dopamine_lock.ui.blockedapps.BlockedAppsScreen
import com.teamdobermans.dopamine_lock.ui.dashboard.DashboardScreen
import com.teamdobermans.dopamine_lock.ui.focus.FocusTimerScreen
import com.teamdobermans.dopamine_lock.ui.focus.MissionModeScreen
import com.teamdobermans.dopamine_lock.ui.onboarding.OnboardingScreen
import com.teamdobermans.dopamine_lock.ui.settings.SettingsScreen
import com.teamdobermans.dopamine_lock.ui.splash.SplashScreen
import com.teamdobermans.dopamine_lock.ui.tasks.AddEditTaskScreen
import com.teamdobermans.dopamine_lock.ui.tasks.TasksScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route ?: Screen.Splash.route

    val bottomNavRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Focus.route,
        Screen.Tasks.route,
        Screen.Analytics.route,
        Screen.Settings.route
    )

    fun navigateBottomNav(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                currentRoute = currentRoute,
                onNavigate = ::navigateBottomNav
            )
        }

        composable(Screen.Focus.route) {
            FocusTimerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMission = {
                    navController.navigate(Screen.Mission.route)
                }
            )
        }

        composable(Screen.Mission.route) {
            MissionModeScreen(
                onAbandonMission = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                currentRoute = currentRoute,
                onNavigate = ::navigateBottomNav,
                onAddTask = {
                    navController.navigate(Screen.AddTask.route)
                }
            )
        }

        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                currentRoute = currentRoute,
                onNavigate = ::navigateBottomNav
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentRoute = currentRoute,
                onNavigate = ::navigateBottomNav,
                onNavigateToBlockedApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BlockedApps.route) {
            BlockedAppsScreen(
                currentRoute = Screen.Settings.route,
                onNavigate = ::navigateBottomNav,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
