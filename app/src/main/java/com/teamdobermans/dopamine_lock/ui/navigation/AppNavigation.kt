package com.teamdobermans.dopamine_lock.navigation

import android.app.Activity
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teamdobermans.dopamine_lock.model.MissionStatus
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.teamdobermans.dopamine_lock.BuildConfig
import com.teamdobermans.dopamine_lock.enforcement.PermissionManager
import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.repo.AnalyticsRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.AuthRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.DisciplineRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.EnforcementRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.FocusSessionRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.GoalRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.MissionRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.StreakRepositoryImpl
import com.teamdobermans.dopamine_lock.repo.UserRepositoryImpl
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
import com.teamdobermans.dopamine_lock.ui.history.SessionDetailsScreen
import com.teamdobermans.dopamine_lock.ui.history.SessionHistoryScreen
import com.teamdobermans.dopamine_lock.ui.focus.MissionResultScreen
import com.teamdobermans.dopamine_lock.ui.mission.CreateMissionScreen
import com.teamdobermans.dopamine_lock.ui.mission.MissionDetailsScreen
import com.teamdobermans.dopamine_lock.ui.mission.MissionHistoryScreen
import com.teamdobermans.dopamine_lock.ui.mission.MissionHomeScreen
import com.teamdobermans.dopamine_lock.ui.mission.MissionTimerScreen
import com.teamdobermans.dopamine_lock.ui.onboarding.OnboardingScreen
import com.teamdobermans.dopamine_lock.ui.settings.SettingsScreen
import com.teamdobermans.dopamine_lock.ui.splash.SplashScreen
import com.teamdobermans.dopamine_lock.ui.streak.StreakCalendarScreen
import com.teamdobermans.dopamine_lock.ui.tasks.AddEditTaskScreen
import com.teamdobermans.dopamine_lock.ui.tasks.TasksScreen
import com.teamdobermans.dopamine_lock.viewModel.AnalyticsViewModel
import com.teamdobermans.dopamine_lock.viewModel.AnalyticsViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.AuthViewModel
import com.teamdobermans.dopamine_lock.viewModel.AuthViewModelFactory
import com.teamdobermans.dopamine_lock.ui.auth.AuthProvider
import com.teamdobermans.dopamine_lock.viewModel.DisciplineViewModel
import com.teamdobermans.dopamine_lock.viewModel.DisciplineViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.EnforcementViewModel
import com.teamdobermans.dopamine_lock.viewModel.EnforcementViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.FocusSessionViewModel
import com.teamdobermans.dopamine_lock.viewModel.FocusSessionViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.GoalViewModel
import com.teamdobermans.dopamine_lock.viewModel.GoalViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.MissionViewModel
import com.teamdobermans.dopamine_lock.viewModel.MissionViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.NotificationViewModel
import com.teamdobermans.dopamine_lock.viewModel.NotificationViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.UserViewModel
import com.teamdobermans.dopamine_lock.viewModel.UserViewModelFactory
import com.teamdobermans.dopamine_lock.repo.TaskRepositoryImpl
import com.teamdobermans.dopamine_lock.viewModel.StreakViewModel
import com.teamdobermans.dopamine_lock.viewModel.StreakViewModelFactory
import com.teamdobermans.dopamine_lock.viewModel.TaskViewModel
import com.teamdobermans.dopamine_lock.viewModel.TaskViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    externalDestination: String? = null,
    onExternalDestinationConsumed: () -> Unit = {}
) {
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
    val disciplineRepository = DisciplineRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database,
        userRepository = userRepository
    )
    val notificationRepository = NotificationRepositoryImpl(context)
    val enforcementRepository = EnforcementRepositoryImpl(context)
    val permissionManager = PermissionManager(context)
    val goalRepository = GoalRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database,
        disciplineRepository = disciplineRepository,
        notificationRepository = notificationRepository
    )
    val streakRepository = StreakRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database,
        userRepository = userRepository,
        disciplineRepository = disciplineRepository,
        notificationRepository = notificationRepository
    )
    val focusSessionRepository = FocusSessionRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database,
        userRepository = userRepository,
        streakRepository = streakRepository,
        disciplineRepository = disciplineRepository,
        goalRepository = goalRepository
    )
    val missionRepository = MissionRepositoryImpl(
        auth = FirebaseProvider.auth,
        database = FirebaseProvider.database,
        userRepository = userRepository,
        streakRepository = streakRepository,
        disciplineRepository = disciplineRepository,
        goalRepository = goalRepository,
        notificationRepository = notificationRepository,
        enforcementRepository = enforcementRepository
    )
    val focusSessionViewModel: FocusSessionViewModel = viewModel(
        factory = FocusSessionViewModelFactory(focusSessionRepository)
    )
    val missionViewModel: MissionViewModel = viewModel(
        factory = MissionViewModelFactory(missionRepository)
    )
    val disciplineViewModel: DisciplineViewModel = viewModel(
        factory = DisciplineViewModelFactory(disciplineRepository)
    )
    val goalViewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(goalRepository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(
            AnalyticsRepositoryImpl(
                focusSessionRepository = focusSessionRepository,
                missionRepository = missionRepository,
                goalRepository = goalRepository,
                streakRepository = streakRepository,
                userRepository = userRepository,
                disciplineRepository = disciplineRepository
            )
        )
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(notificationRepository)
    )
    val enforcementViewModel: EnforcementViewModel = viewModel(
        factory = EnforcementViewModelFactory(
            repository = enforcementRepository,
            permissionManager = permissionManager
        )
    )
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            TaskRepositoryImpl(
                auth = FirebaseProvider.auth,
                database = FirebaseProvider.database
            )
        )
    )
    val streakViewModel: StreakViewModel = viewModel(
        factory = StreakViewModelFactory(streakRepository)
    )
    val authUiState by authViewModel.uiState.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()
    val focusSessionUiState by focusSessionViewModel.uiState.collectAsState()
    val missionUiState by missionViewModel.uiState.collectAsState()
    val disciplineUiState by disciplineViewModel.uiState.collectAsState()
    val goalUiState by goalViewModel.uiState.collectAsState()
    val analyticsUiState by analyticsViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val enforcementUiState by enforcementViewModel.uiState.collectAsState()
    val taskUiState by taskViewModel.uiState.collectAsState()
    val streakUiState by streakViewModel.uiState.collectAsState()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route ?: Screen.Splash.route

    val bottomNavRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Focus.route,
        Screen.MissionHome.route,
        Screen.MissionTimer.route,
        Screen.Tasks.route,
        Screen.Analytics.route,
        Screen.Settings.route,
        Screen.SessionHistory.route,
        Screen.StreakCalendar.route,
        Screen.DisciplineScore.route,
        Screen.GoalTracking.route,
        Screen.BlockedApps.route
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

    LaunchedEffect(externalDestination, authUiState.isAuthenticated) {
        val destination = externalDestination ?: return@LaunchedEffect
        if (!authUiState.isAuthenticated) return@LaunchedEffect

        when (destination) {
            Screen.BlockedAppOverlay.route,
            "blocked_app_overlay" -> navController.navigate(Screen.BlockedAppOverlay.route) {
                launchSingleTop = true
            }

            Screen.MissionHome.route,
            Screen.Mission.route,
            "mission" -> navController.navigate(Screen.MissionHome.route) {
                launchSingleTop = true
            }

            Screen.Dashboard.route,
            "dashboard" -> navController.navigate(Screen.Dashboard.route) {
                launchSingleTop = true
            }

            Screen.CreateMission.route,
            "create_mission" -> navController.navigate(Screen.CreateMission.route) {
                launchSingleTop = true
            }

            Screen.GoalTracking.route,
            "goal_tracking" -> navController.navigate(Screen.GoalTracking.route) {
                launchSingleTop = true
            }

            Screen.DisciplineScore.route,
            "discipline_score" -> navController.navigate(Screen.DisciplineScore.route) {
                launchSingleTop = true
            }
        }
        onExternalDestinationConsumed()
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
            disciplineViewModel.observeScore()
            disciplineViewModel.observeRank()
            disciplineViewModel.observeHistory()
            goalViewModel.observeGoals()
            analyticsViewModel.loadAnalytics()
            notificationViewModel.loadPreferences()
            notificationViewModel.scheduleNotifications()
            enforcementViewModel.checkPermissions()
            taskViewModel.observeTasks()
            streakViewModel.observeStreakRecords()
        } else {
            userViewModel.clearUser()
            focusSessionViewModel.clear()
            missionViewModel.clear()
            disciplineViewModel.clear()
            goalViewModel.clear()
            analyticsViewModel.clear()
            notificationViewModel.clear()
            enforcementViewModel.stopEnforcement()
            taskViewModel.clear()
            streakViewModel.clear()
        }
    }

    LaunchedEffect(
        authUiState.isAuthenticated,
        focusSessionUiState.sessions.size,
        missionUiState.missions.size,
        goalUiState.goals.size,
        goalUiState.completedGoals.size,
        disciplineUiState.score
    ) {
        if (authUiState.isAuthenticated) {
            analyticsViewModel.refreshAnalytics()
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

    fun currentActivity(): Activity? = context as? Activity

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

    fun launchGitHubSignIn() {
        val activity = currentActivity()
        if (activity == null) {
            authViewModel.setError("GitHub Sign-In is unavailable on this device.")
            return
        }
        authViewModel.githubSignIn(activity)
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
                onGitHubSignInClick = ::launchGitHubSignIn,
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
                onGitHubSignInClick = ::launchGitHubSignIn,
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
                disciplineScore = analyticsUiState.summary.disciplineScore,
                disciplineRank = disciplineUiState.rank,
                recentDisciplineEvent = disciplineUiState.recentEvents.firstOrNull(),
                dailyGoals = goalUiState.dailyGoals,
                weeklyGoals = goalUiState.weeklyGoals,
                monthlyGoals = goalUiState.monthlyGoals,
                successRate = analyticsUiState.summary.successRate,
                bestFocusDay = analyticsUiState.summary.bestFocusDay,
                todayFocusHours = focusSessionUiState.todayFocusHours,
                todaySessionCount = focusSessionUiState.todaySessionCount,
                onNavigate = ::navigateBottomNav,
                onStartFocus = {
                    navController.navigate(Screen.MissionHome.route)
                },
                onSeeAllSessions = {
                    navController.navigate(Screen.SessionHistory.route)
                },
                onOpenStreakCalendar = {
                    navController.navigate(Screen.StreakCalendar.route)
                },
                onOpenGoalTracking = {
                    navController.navigate(Screen.GoalTracking.route)
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.SessionDetails.createRoute(sessionId))
                }
            )
        }

        composable(Screen.CreateMission.route) {
            CreateMissionScreen(
                onNavigateBack = {
                    navController.navigate(Screen.MissionHome.route) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onManageApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onStartMission = { missionName,
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
                                            navController.navigate(Screen.MissionTimer.route) {
                                                popUpTo(Screen.CreateMission.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    )
                },
                onCancel = {
                    navController.navigate(Screen.MissionHome.route) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Focus.route) {
            FocusTimerScreen(
                currentRoute = currentRoute,
                activeSession = focusSessionUiState.activeSession,
                activeMission = missionUiState.activeMission,
                onNavigate = ::navigateBottomNav,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMission = {
                    navController.navigate(Screen.Mission.route)
                },
                onStartFocusSession = { durationMinutes ->
                    focusSessionViewModel.startSession(
                        missionName = "Focus Session",
                        missionGoal = "Normal focus session",
                        missionType = "Focus",
                        durationMinutes = durationMinutes,
                        blockedApps = emptyList()
                    ) {
                        navController.navigate(Screen.Focus.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onCompleteSession = { sessionId, elapsedSeconds ->
                    val activeMissionId = missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }
                    focusSessionViewModel.completeSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = activeMissionId == null
                    )
                    if (activeMissionId != null) {
                        missionViewModel.completeMission(activeMissionId)
                        navController.navigate(Screen.MissionResult.createRoute(sessionId, "completed")) {
                            popUpTo(Screen.MissionHome.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Screen.Focus.route) {
                            popUpTo(Screen.Focus.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onAbandonSession = { sessionId, elapsedSeconds ->
                    val activeMissionId = missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }
                    focusSessionViewModel.abandonSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = activeMissionId == null
                    )
                    if (activeMissionId != null) {
                        missionViewModel.abandonMission(activeMissionId)
                        navController.navigate(Screen.MissionResult.createRoute(sessionId, "abandoned")) {
                            popUpTo(Screen.MissionHome.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Screen.Focus.route) {
                            popUpTo(Screen.Focus.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.MissionHome.route) {
            MissionHomeScreen(
                currentRoute = Screen.MissionHome.route,
                activeMission = missionUiState.activeMission,
                missions = missionUiState.missions,
                currentStreak = streakUiState.currentStreak
                    .takeIf { it > 0 } ?: (userUiState.user?.currentStreak ?: 0),
                bestStreak = streakUiState.bestStreak
                    .takeIf { it > 0 } ?: (userUiState.user?.bestStreak ?: 0),
                disciplineScore = disciplineUiState.score,
                disciplineRank = disciplineUiState.rank,
                onNavigate = ::navigateBottomNav,
                onCreateMission = {
                    navController.navigate(Screen.CreateMission.route)
                },
                onResumeMission = {
                    navController.navigate(Screen.MissionTimer.route)
                },
                onOpenBlockedApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onOpenMissionHistory = {
                    navController.navigate(Screen.MissionHistory.route)
                },
                onOpenStreak = {
                    navController.navigate(Screen.StreakCalendar.route)
                },
                onOpenScore = {
                    navController.navigate(Screen.DisciplineScore.route)
                },
                onOpenMissionDetails = { missionId ->
                    navController.navigate(Screen.MissionDetails.createRoute(missionId))
                }
            )
        }

        composable(Screen.MissionTimer.route) {
            MissionTimerScreen(
                currentRoute = Screen.MissionTimer.route,
                activeMission = missionUiState.activeMission,
                activeSession = focusSessionUiState.activeSession,
                onNavigate = ::navigateBottomNav,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEnterMissionMode = {
                    navController.navigate(Screen.Mission.route)
                },
                onCompleteMission = { sessionId, elapsedSeconds ->
                    focusSessionViewModel.completeSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = missionUiState.activeMission == null
                    )
                    missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }?.let {
                        missionViewModel.completeMission(it)
                    }
                    navController.navigate(Screen.MissionResult.createRoute(sessionId, "completed")) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                    }
                },
                onAbandonMission = { sessionId, elapsedSeconds ->
                    focusSessionViewModel.abandonSession(
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSeconds,
                        applyDisciplineScore = missionUiState.activeMission == null
                    )
                    missionUiState.activeMission?.missionId?.takeIf { it.isNotBlank() }?.let {
                        missionViewModel.abandonMission(it)
                    }
                    navController.navigate(Screen.MissionResult.createRoute(sessionId, "abandoned")) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
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
                    navController.navigate(Screen.MissionResult.createRoute(sessionId, "abandoned")) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                    }
                },
                onMissionComplete = {
                    val sessionId = focusSessionUiState.activeSession?.sessionId ?: ""
                    if (sessionId.isNotBlank()) {
                        navController.navigate(Screen.MissionResult.createRoute(sessionId, "completed")) {
                            popUpTo(Screen.MissionHome.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Screen.MissionHome.route) {
                            popUpTo(Screen.MissionHome.route) { inclusive = false }
                        }
                    }
                }
            )
        }

        composable(Screen.BlockedAppOverlay.route) {
            val enforcementState = enforcementUiState.activeMission
            val blockedPackage = enforcementState.lastBlockedPackage
            val blockedAppName = resolveAppName(context, blockedPackage)
            BlockedAppOverlayScreen(
                blockedAppName = blockedAppName,
                missionTitle = enforcementState.missionTitle.ifBlank {
                    missionUiState.activeMission?.title ?: "Mission Active"
                },
                remainingText = remainingMissionText(enforcementState.startedAt, enforcementState.durationMinutes),
                blockedAppsCount = enforcementState.blockedApps.size,
                onReturnToMission = {
                    navController.navigate(Screen.Mission.route)
                },
                onViewMissionRules = {
                    navController.navigate(Screen.Mission.route)
                },
                onAbandonMission = {
                    (missionUiState.activeMission?.missionId ?: enforcementState.missionId)
                        .takeIf { it.isNotBlank() }
                        ?.let { missionId ->
                            missionViewModel.abandonMission(missionId)
                        }
                    navController.navigate(Screen.MissionHome.route) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                currentRoute = currentRoute,
                tasks = taskUiState.tasks,
                onNavigate = ::navigateBottomNav,
                onAddTask = {
                    navController.navigate(Screen.AddTask.route)
                },
                onToggleTask = taskViewModel::toggleTask,
                onDeleteTask = taskViewModel::deleteTask
            )
        }

        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isSaving = taskUiState.isSaving,
                errorMessage = taskUiState.errorMessage,
                successMessage = taskUiState.successMessage,
                onCreateTask = taskViewModel::createTask,
                onClearMessages = taskViewModel::clearMessages
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                currentRoute = currentRoute,
                sessions = focusSessionUiState.sessions,
                totalFocusHours = analyticsUiState.summary.totalFocusHours,
                completedSessions = analyticsUiState.summary.completedSessions,
                totalSessions = analyticsUiState.summary.totalSessions,
                successRate = analyticsUiState.summary.successRate,
                weeklyFocusHours = analyticsUiState.weeklyHours.map { it.toFloat() },
                monthlyFocusHours = analyticsUiState.monthlyHours.map { it.toFloat() },
                focusDistribution = analyticsUiState.focusDistribution,
                bestFocusDay = analyticsUiState.bestFocusDay,
                bestFocusHours = bestFocusDayHours(focusSessionUiState.sessions, analyticsUiState.summary.bestFocusDay),
                disciplineScore = analyticsUiState.summary.disciplineScore,
                disciplineRank = disciplineUiState.rank,
                averageDailyDisciplineGain = analyticsUiState.disciplineGrowth,
                mostValuableHabit = mostValuableHabit(disciplineUiState.events),
                goalsCreated = goalUiState.goals.size,
                goalsCompleted = analyticsUiState.summary.completedGoals,
                goalCompletionRate = analyticsUiState.goalCompletionRate,
                averageGoalCompletionTimeHours = averageGoalCompletionTimeHours(goalUiState.completedGoals),
                mostSuccessfulGoalType = mostSuccessfulGoalType(goalUiState.completedGoals),
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
                currentRoute = Screen.Analytics.route,
                sessions = focusSessionUiState.sessions,
                missions = missionUiState.missions,
                totalFocusHours = focusSessionUiState.totalFocusHours,
                completedSessions = focusSessionUiState.completedSessions,
                successRate = analyticsUiState.summary.successRate,
                averageDurationMinutes = averageSessionLengthMinutes(focusSessionUiState.sessions),
                onNavigate = ::navigateBottomNav,
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.SessionDetails.createRoute(sessionId))
                }
            )
        }

        composable(Screen.StreakCalendar.route) {
            StreakCalendarScreen(
                currentRoute = Screen.Analytics.route,
                user = userUiState.user,
                streakRecords = streakUiState.streakRecords,
                currentStreak = streakUiState.currentStreak
                    .takeIf { it > 0 } ?: (userUiState.user?.currentStreak ?: 0),
                bestStreak = streakUiState.bestStreak
                    .takeIf { it > 0 } ?: (userUiState.user?.bestStreak ?: 0),
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
                disciplineScore = disciplineUiState.score,
                disciplineRank = disciplineUiState.rank,
                events = disciplineUiState.events,
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
                currentRoute = Screen.Analytics.route,
                goals = goalUiState.goals,
                dailyGoals = goalUiState.dailyGoals,
                weeklyGoals = goalUiState.weeklyGoals,
                monthlyGoals = goalUiState.monthlyGoals,
                activeGoals = goalUiState.activeGoals,
                completedGoals = goalUiState.completedGoals,
                onCreateGoal = goalViewModel::createGoal,
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
                notificationPreferences = notificationUiState.preferences,
                onNotificationPreferencesChange = notificationViewModel::updatePreferences,
                enforcementUiState = enforcementUiState,
                onEnforcementSettingsChange = enforcementViewModel::updateSettings,
                onOpenAccessibilitySettings = {
                    context.startActivity(permissionManager.accessibilitySettingsIntent())
                },
                onOpenUsageAccessSettings = {
                    context.startActivity(permissionManager.usageAccessSettingsIntent())
                },
                onOpenNotificationSettings = {
                    context.startActivity(permissionManager.appNotificationSettingsIntent())
                },
                onNavigate = ::navigateBottomNav,
                onNavigateToBlockedApps = {
                    navController.navigate(Screen.BlockedApps.route)
                },
                onChangePassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLogout = {
                    authViewModel.logout()
                }
            )
        }

        composable(Screen.BlockedApps.route) {
            BlockedAppsScreen(
                currentRoute = Screen.MissionHome.route,
                onNavigate = ::navigateBottomNav,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Mission Result
        composable(
            route = Screen.MissionResult.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("result") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val result = backStackEntry.arguments?.getString("result") ?: "completed"
            val completed = result == "completed"
            val session = focusSessionUiState.sessions.firstOrNull { it.sessionId == sessionId }
            MissionResultScreen(
                session = session,
                completed = completed,
                onViewSession = {
                    navController.navigate(Screen.SessionDetails.createRoute(sessionId)) {
                        popUpTo(Screen.MissionResult.route) { inclusive = false }
                    }
                },
                onTryAgain = {
                    navController.navigate(Screen.CreateMission.route) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                    }
                },
                onBackToDashboard = {
                    navController.navigate(Screen.MissionHome.route) {
                        popUpTo(Screen.MissionHome.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.MissionHistory.route) {
            MissionHistoryScreen(
                currentRoute = Screen.MissionHome.route,
                missions = missionUiState.missions,
                onNavigate = ::navigateBottomNav,
                onMissionClick = { missionId ->
                    navController.navigate(Screen.MissionDetails.createRoute(missionId))
                }
            )
        }

        composable(
            route = Screen.MissionDetails.route,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId") ?: ""
            MissionDetailsScreen(
                currentRoute = Screen.MissionHome.route,
                mission = missionUiState.missions.firstOrNull { it.missionId == missionId },
                onNavigate = ::navigateBottomNav
            )
        }

        // Session Details
        composable(
            route = Screen.SessionDetails.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val session = focusSessionUiState.sessions.firstOrNull { it.sessionId == sessionId }
            SessionDetailsScreen(
                session = session,
                onBack = { navController.popBackStack() },
                onStartSimilarMission = {
                    navController.navigate(Screen.CreateMission.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
            )
        }
    }
}

private fun averageDailyDisciplineGain(events: List<DisciplineEvent>): Int {
    if (events.isEmpty()) return 0
    val dayCount = events.map { it.createdAt / DAY_MS }.distinct().size.coerceAtLeast(1)
    return events.sumOf { it.points }.coerceAtLeast(0) / dayCount
}

private fun resolveAppName(context: android.content.Context, packageName: String): String {
    if (packageName.isBlank()) return "Blocked App"
    return runCatching {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    }.getOrDefault(packageName)
}

private fun remainingMissionText(startedAt: Long, durationMinutes: Int): String {
    if (startedAt <= 0L || durationMinutes <= 0) return "Return to your mission."
    val remainingMs = (durationMinutes * 60_000L - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0L)
    val minutes = remainingMs / 60_000L
    val seconds = (remainingMs % 60_000L) / 1_000L
    return "%02d:%02d".format(minutes, seconds)
}

private fun mostValuableHabit(events: List<DisciplineEvent>): String {
    return events
        .groupBy { it.eventType }
        .maxByOrNull { entry -> entry.value.sumOf { it.points } }
        ?.key
        ?.let(::eventTypeLabel)
        ?: "No discipline events yet"
}

private fun scoreGrowthTrend(events: List<DisciplineEvent>, currentScore: Int): List<Float> {
    if (events.isEmpty()) return emptyList()

    val dailyTotals = events
        .groupBy { it.createdAt / DAY_MS }
        .toSortedMap()
        .values
        .map { dayEvents -> dayEvents.sumOf { it.points } }
        .takeLast(30)

    var runningScore = (currentScore - dailyTotals.sum()).coerceAtLeast(0)
    return dailyTotals.map { points ->
        runningScore = (runningScore + points).coerceAtLeast(0)
        runningScore.toFloat()
    }
}

private fun averageSessionLengthMinutes(sessions: List<com.teamdobermans.dopamine_lock.model.FocusSession>): Int {
    val completed = sessions.filter { it.completed }
    if (completed.isEmpty()) return 0
    return (completed.sumOf { it.elapsedSeconds } / completed.size / 60L).toInt()
}

private fun bestFocusDayHours(
    sessions: List<com.teamdobermans.dopamine_lock.model.FocusSession>,
    bestDay: String
): Double {
    if (bestDay.isBlank()) return 0.0
    val formatter = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
    return sessions
        .filter { it.completed }
        .filter { formatter.format(it.endedAt.takeIf { endedAt -> endedAt > 0L } ?: it.startedAt) == bestDay }
        .sumOf { it.elapsedSeconds } / 3600.0
}

private fun goalCompletionRate(goals: List<Goal>): Int {
    if (goals.isEmpty()) return 0
    return ((goals.count { it.completed }.toFloat() / goals.size.toFloat()) * 100).toInt()
}

private fun averageGoalCompletionTimeHours(goals: List<Goal>): Int {
    val completedDurations = goals
        .filter { it.completedAt > 0L && it.createdAt > 0L }
        .map { (it.completedAt - it.createdAt).coerceAtLeast(0L) / HOUR_MS }

    if (completedDurations.isEmpty()) return 0
    return completedDurations.average().toInt()
}

private fun mostSuccessfulGoalType(goals: List<Goal>): String {
    return goals
        .groupBy { it.goalType }
        .maxByOrNull { it.value.size }
        ?.key
        ?.name
        ?: "NONE"
}

private fun eventTypeLabel(type: String): String {
    return type.lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private const val DAY_MS = 24 * 60 * 60 * 1000L
private const val HOUR_MS = 60 * 60 * 1000L
