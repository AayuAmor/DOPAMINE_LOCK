package com.teamdobermans.dopamine_lock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.teamdobermans.dopamine_lock.BuildConfig
import com.teamdobermans.dopamine_lock.data.repositoryImpl.AuthRepositoryImpl
import com.teamdobermans.dopamine_lock.data.repositoryImpl.FocusSessionRepositoryImpl
import com.teamdobermans.dopamine_lock.data.repositoryImpl.MissionRepositoryImpl
import com.teamdobermans.dopamine_lock.data.repositoryImpl.UserRepositoryImpl
import com.teamdobermans.dopamine_lock.firebase.FirebaseProvider
import com.teamdobermans.dopamine_lock.ui.analytics.AnalyticsScreen
import com.teamdobermans.dopamine_lock.ui.auth.ForgotPasswordScreen
import com.teamdobermans.dopamine_lock.ui.auth.LoginScreen
import com.teamdobermans.dopamine_lock.ui.auth.RegisterScreen
import com.teamdobermans.dopamine_lock.ui.blockedapps.BlockedAppOverlayScreen
import com.teamdobermans.dopamine_lock.ui.blockedapps.BlockedAppsScreen
import com.teamdobermans.dopamine_lock.ui.dashboard.DashboardScreen
import com.teamdobermans.dopamine_lock.ui.discipline.DisciplineScoreScreen
import com.teamdobermans.dopamine_lock.ui.focus.FocusTimerScreen
import com.teamdobermans.dopamine_lock.ui.focus.MissionModeScreen
import com.teamdobermans.dopamine_lock.ui.goals.GoalTrackingScreen
import com.teamdobermans.dopamine_lock.ui.history.SessionHistoryScreen
import com.teamdobermans.dopamine_lock.ui.mission.CreateMissionScreen
import com.teamdobermans.dopamine_lock.ui.onboarding.OnboardingScreen
import com.teamdobermans.dopamine_lock.ui.settings.SettingsScreen
import com.teamdobermans.dopamine_lock.ui.splash.SplashScreen
import com.teamdobermans.dopamine_lock.ui.streak.StreakCalendarScreen
import com.teamdobermans.dopamine_lock.ui.tasks.AddEditTaskScreen
import com.teamdobermans.dopamine_lock.ui.tasks.TasksScreen
import com.teamdobermans.dopamine_lock.viewModel.AuthViewModel
import com.teamdobermans.dopamine_lock.viewModel.AuthViewModelFactory
import com.teamdobermans.dopamine_lock.ui.auth.AuthProvider
import com.teamdobermans.dopamine_lock.viewModel.FocusSessionViewModel
import com.teamdobermans.dopamine_lock.viewModel.FocusSessionViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.MissionViewModel
import com.teamdobermans.dopamine_lock.viewModel.MissionViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.UserViewModel
import com.teamdobermans.dopamine_lock.viewModel.UserViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            AuthRepositoryImpl(
                auth = FirebaseProvider.auth,
                database = FirebaseProvider.database
            )
        )
    )
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            UserRepositoryImpl(
                auth = FirebaseProvider.auth,
                database = FirebaseProvider.database
            )
        )
    )
    val userRepository = UserRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database
    )
    val focusSessionViewModel: FocusSessionViewModel = viewModel(
        factory = FocusSessionViewModelFactory(
            FocusSessionRepositoryImpl(
                auth = FirebaseProvider.auth,
                database = FirebaseProvider.database,
                userRepository = userRepository
            )
        )
    )
    val missionViewModel: MissionViewModel = viewModel(
        factory = MissionViewModelFactory(
            MissionRepositoryImpl(
                auth = FirebaseProvider.auth,
                database = FirebaseProvider.database,
                userRepository = userRepository
            )
        )
    )
    val authUiState by authViewModel.uiState.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()
    val focusSessionUiState by focusSessionViewModel.uiState.collectAsState()
    val missionUiState by missionViewModel.uiState.collectAsState()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route ?: Screen.Splash.route

    val bottomNavRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Focus.route,
        Screen.Tasks.route,
        Screen.Analytics.route,
        Screen.Settings.route
    )

    val publicRoutes = setOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route
    )

    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
    }

    LaunchedEffect(authUiState.hasCheckedAuthState, authUiState.isAuthenticated, currentRoute) {
        if (!authUiState.hasCheckedAuthState) return@LaunchedEffect

        if (authUiState.isAuthenticated && currentRoute in publicRoutes && currentRoute != Screen.Splash.route) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }

        if (!authUiState.isAuthenticated && currentRoute !in publicRoutes) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authUiState.isAuthenticated, authUiState.user?.uid) {
        if (authUiState.isAuthenticated) {
            userViewModel.observeCurrentUserProfile()
            focusSessionViewModel.observeSessions()
            focusSessionViewModel.observeActiveSession()
            missionViewModel.observeMissions()
            missionViewModel.observeActiveMission()
        } else {
            userViewModel.clearUser()
            focusSessionViewModel.clear()
            missionViewModel.clear()
        }
    }

    fun navigateBottomNav(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun launchGoogleSignIn() {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            authViewModel.setError("Google Sign-In is not configured.")
            return
        }

        authViewModel.startOAuthLoading(AuthProvider.Google)
        coroutineScope.launch {
            runCatching {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                credentialManager.getCredential(context, request).credential
            }.onSuccess { credential ->
                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authViewModel.googleSignIn(googleCredential.idToken)
                } else {
                    authViewModel.setError("Google Sign-In failed. Please try again.")
                }
            }.onFailure { exception ->
                val message = when (exception) {
                    is GetCredentialCancellationException -> "Google Sign-In cancelled."
                    is GetCredentialException -> "Google Sign-In failed. Please try again."
                    else -> "Google Sign-In failed. Please try again."
                }
                authViewModel.setError(message)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    val destination = if (authUiState.isAuthenticated) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Onboarding.route
                    }
                    navController.navigate(destination) {
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
                },
                authUiState = authUiState,
                onLogin = authViewModel::login,
                onGoogleSignInClick = ::launchGoogleSignIn,
                onGitHubSignInClick = authViewModel::githubSignIn,
                onClearMessages = authViewModel::clearMessages
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
                },
                authUiState = authUiState,
                onRegister = authViewModel::register,
                onGoogleSignInClick = ::launchGoogleSignIn,
                onGitHubSignInClick = authViewModel::githubSignIn,
                onClearMessages = authViewModel::clearMessages
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                authUiState = authUiState,
                onSendReset = authViewModel::forgotPassword,
                onClearMessages = authViewModel::clearMessages
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                currentRoute = currentRoute,
                user = userUiState.user,
                sessions = focusSessionUiState.sessions,
                todayFocusHours = focusSessionUiState.todayFocusHours,
                todaySessionCount = focusSessionUiState.todaySessionCount,
                onNavigate = ::navigateBottomNav,
                onStartFocus = {
                    navController.navigate(Screen.CreateMission.route)
                },
                onSeeAllSessions = {
                    navController.navigate(Screen.SessionHistory.route)
                },
                onOpenStreakCalendar = {
                    navController.navigate(Screen.StreakCalendar.route)
                },
                onOpenGoalTracking = {
                    navController.navigate(Screen.GoalTracking.route)
                }
            )
        }

        composable(Screen.CreateMission.route) {
            CreateMissionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onManageApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onStartMission = {
                    missionName,
                    missionGoal,
                    missionType,
                    durationMinutes,
                    blockedApps ->
                    missionViewModel.createMission(
                        title = missionName,
                        goal = missionGoal,
                        missionType = missionType,
                        durationMinutes = durationMinutes,
                        blockedApps = blockedApps,
                        onSuccess = { mission ->
                            missionViewModel.startMission(
                                missionId = mission.missionId,
                                onSuccess = {
                                    focusSessionViewModel.startSession(
                                        missionName = missionName,
                                        missionGoal = missionGoal,
                                        missionType = missionType,
                                        durationMinutes = durationMinutes,
                                        blockedApps = blockedApps,
                                        onSuccess = {
                                            navController.navigate(Screen.Mission.route)
                                        }
                                    )
                                }
                            )
                        }
                    )
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Focus.route) {
            FocusTimerScreen(
                activeSession = focusSessionUiState.activeSession,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMission = {
                    navController.navigate(Screen.Mission.route)
                },
                onCompleteSession = { sessionId, elapsedSeconds ->
                    val hasMissionReward = missionUiState.activeMission?.missionId?.isNotBlank() == true
                    focusSessionViewModel.completeSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = !hasMissionReward
                    )
                    missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }?.let {
                        missionViewModel.completeMission(it)
                    }
                },
                onAbandonSession = { sessionId, elapsedSeconds ->
                    val hasMissionPenalty = missionUiState.activeMission?.missionId?.isNotBlank() == true
                    focusSessionViewModel.abandonSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = !hasMissionPenalty
                    )
                    missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }?.let {
                        missionViewModel.abandonMission(it)
                    }
                }
            )
        }

        composable(Screen.Mission.route) {
            MissionModeScreen(
                activeSession = focusSessionUiState.activeSession,
                activeMission = missionUiState.activeMission,
                onAbandonMission = { sessionId, elapsedSeconds ->
                    val hasMissionPenalty = missionUiState.activeMission?.missionId?.isNotBlank() == true
                    focusSessionViewModel.abandonSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = !hasMissionPenalty
                    )
                    missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }?.let { missionId ->
                        missionViewModel.abandonMission(missionId)
                    }
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.BlockedAppOverlay.route) {
            BlockedAppOverlayScreen(
                onReturnToMission = {
                    navController.navigate(Screen.Mission.route)
                },
                onViewMissionRules = {
                    navController.navigate(Screen.Mission.route)
                },
                onAbandonMission = {
                    navController.navigate(Screen.Focus.route)
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
                sessions = focusSessionUiState.sessions,
                totalFocusHours = focusSessionUiState.totalFocusHours,
                completedSessions = focusSessionUiState.completedSessions,
                successRate = focusSessionUiState.successRate,
                weeklyFocusHours = focusSessionUiState.weeklyFocusHours,
                onNavigate = ::navigateBottomNav,
                onOpenStreakCalendar = {
                    navController.navigate(Screen.StreakCalendar.route)
                },
                onOpenDisciplineScore = {
                    navController.navigate(Screen.DisciplineScore.route)
                }
            )
        }

        composable(Screen.SessionHistory.route) {
            SessionHistoryScreen(
                currentRoute = Screen.Dashboard.route,
                sessions = focusSessionUiState.sessions,
                missions = missionUiState.missions,
                totalFocusHours = focusSessionUiState.totalFocusHours,
                completedSessions = focusSessionUiState.completedSessions,
                successRate = focusSessionUiState.successRate,
                onNavigate = ::navigateBottomNav
            )
        }

        composable(Screen.StreakCalendar.route) {
            StreakCalendarScreen(
                currentRoute = Screen.Dashboard.route,
                user = userUiState.user,
                onNavigate = ::navigateBottomNav,
                onStartMission = {
                    navController.navigate(Screen.CreateMission.route)
                }
            )
        }

        composable(Screen.DisciplineScore.route) {
            DisciplineScoreScreen(
                currentRoute = Screen.Analytics.route,
                user = userUiState.user,
                onNavigate = ::navigateBottomNav,
                onViewStreakCalendar = {
                    navController.navigate(Screen.StreakCalendar.route)
                },
                onStartMission = {
                    navController.navigate(Screen.CreateMission.route)
                }
            )
        }

        composable(Screen.GoalTracking.route) {
            GoalTrackingScreen(
                currentRoute = Screen.Dashboard.route,
                onNavigate = ::navigateBottomNav,
                onStartMission = {
                    navController.navigate(Screen.CreateMission.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentRoute = currentRoute,
                user = userUiState.user,
                onNavigate = ::navigateBottomNav,
                onNavigateToBlockedApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onLogout = {
                    authViewModel.logout()
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
