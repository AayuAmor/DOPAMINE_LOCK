package com.teamdobermans.dopamine_lock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Focus.route, "Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    BottomNavItem(Screen.MissionHome.route, "Mission", Icons.Filled.Lock, Icons.Outlined.Lock),
    BottomNavItem(Screen.Tasks.route, "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    BottomNavItem(Screen.Analytics.route, "Stats", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

private val missionTabRoutes = setOf(
    Screen.MissionHome.route,
    Screen.CreateMission.route,
    Screen.MissionTimer.route,
    Screen.Mission.route,
    Screen.MissionHistory.route,
    Screen.MissionDetails.route,
    Screen.MissionResult.route,
    Screen.BlockedApps.route
)

private val statsTabRoutes = setOf(
    Screen.Analytics.route,
    Screen.SessionHistory.route,
    Screen.SessionDetails.route,
    Screen.StreakCalendar.route,
    Screen.DisciplineScore.route,
    Screen.GoalTracking.route
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = DopamineBorder,
            thickness = 1.dp
        )

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            containerColor = DopamineSurface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0.dp)
        ) {
            bottomNavItems.forEach { item ->
                val selected = isRouteSelected(item.route, currentRoute)

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DopamineWhite,
                        selectedTextColor = DopamineWhite,
                        unselectedIconColor = DopamineGrey,
                        unselectedTextColor = DopamineGrey,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

private fun isRouteSelected(itemRoute: String, currentRoute: String): Boolean {
    if (currentRoute == itemRoute) return true

    return when (itemRoute) {
        Screen.MissionHome.route -> currentRoute in missionTabRoutes ||
                currentRoute.startsWith("mission_details/") ||
                currentRoute.startsWith("mission_result/") ||
                currentRoute.startsWith("mission_details") ||
                currentRoute.startsWith("mission_result")

        Screen.Analytics.route -> currentRoute in statsTabRoutes ||
                currentRoute.startsWith("session_details/") ||
                currentRoute.startsWith("session_details")

        else -> false
    }
}
