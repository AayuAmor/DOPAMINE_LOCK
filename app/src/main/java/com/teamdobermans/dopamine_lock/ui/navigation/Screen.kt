package com.teamdobermans.dopamine_lock.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Core mission flow
    object Dashboard : Screen("dashboard")
    object MissionHome : Screen("mission_home")
    object CreateMission : Screen("create_mission")
    object MissionTimer : Screen("mission_timer")
    object MissionHistory : Screen("mission_history")
    object MissionDetails : Screen("mission_details/{missionId}") {
        fun createRoute(missionId: String) = "mission_details/$missionId"
    }
    object Focus : Screen("focus")
    object Mission : Screen("mission")

    // Mission result — args: sessionId, result ("completed"|"abandoned")
    object MissionResult : Screen("mission_result/{sessionId}/{result}") {
        fun createRoute(sessionId: String, result: String) = "mission_result/$sessionId/$result"
    }

    // Tasks
    object Tasks : Screen("tasks")
    object AddTask : Screen("add_task")

    // Analytics / history
    object Analytics : Screen("analytics")
    object SessionHistory : Screen("session_history")

    // Session details — arg: sessionId
    object SessionDetails : Screen("session_details/{sessionId}") {
        fun createRoute(sessionId: String) = "session_details/$sessionId"
    }

    object StreakCalendar : Screen("streak_calendar")
    object DisciplineScore : Screen("discipline_score")
    object GoalTracking : Screen("goal_tracking")

    // Settings
    object Settings : Screen("settings")
    object BlockedApps : Screen("blocked_apps")
    object BlockedAppOverlay : Screen("blocked_app_overlay")
}
