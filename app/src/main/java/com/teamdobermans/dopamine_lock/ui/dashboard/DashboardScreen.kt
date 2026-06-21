package com.teamdobermans.dopamine_lock.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DashboardStatCard
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.FocusProgressCard
import com.teamdobermans.dopamine_lock.ui.components.SectionHeader
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDivider
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

private data class RecentSession(
    val title: String,
    val duration: String,
    val time: String,
    val completed: Boolean
)

private val recentSessions = listOf(
    RecentSession("Deep Work — Code Review", "52 min", "Today, 09:14", true),
    RecentSession("Reading Session", "30 min", "Today, 07:30", true),
    RecentSession("Design Sprint", "45 min", "Yesterday, 15:00", true),
    RecentSession("Morning Focus Block", "60 min", "Yesterday, 08:00", false),
    RecentSession("Documentation", "25 min", "2d ago, 14:20", true)
)

@Composable
fun DashboardScreen(
    currentRoute: String = Screen.Dashboard.route,
    onNavigate: (String) -> Unit,
    onStartFocus: () -> Unit = { onNavigate(Screen.Focus.route) },
    onSeeAllSessions: () -> Unit = { onNavigate(Screen.Analytics.route) },
    onOpenStreakCalendar: () -> Unit = { onNavigate(Screen.Analytics.route) },
    onOpenGoalTracking: () -> Unit = { onNavigate(Screen.Analytics.route) }
) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 0.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                DashboardHeader(onNotificationsClick = {})
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Today's Stats")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        value = "4.2",
                        label = "Focus Hrs",
                        unit = "h",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = "6",
                        label = "Sessions",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = "12",
                        label = "Day Streak",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenStreakCalendar
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader(title = "Today's Goal")
                Spacer(modifier = Modifier.height(12.dp))
                FocusProgressCard(
                    title = "Daily Focus Goal",
                    current = 4.2f,
                    total = 6f,
                    unit = "hours",
                    onClick = onOpenGoalTracking
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader(title = "Quick Actions")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DopamineButton(
                        text = "Start Focus",
                        onClick = onStartFocus,
                        variant = ButtonVariant.Primary,
                        leadingIcon = Icons.Filled.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    DopamineButton(
                        text = "My Tasks",
                        onClick = { onNavigate(Screen.Tasks.route) },
                        variant = ButtonVariant.Secondary,
                        leadingIcon = Icons.Filled.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader(
                    title = "Recent Sessions",
                    action = "See All",
                    onActionClick = onSeeAllSessions
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(recentSessions.size) { index ->
                val session = recentSessions[index]
                SessionListItem(session = session)
                if (index < recentSessions.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DopamineDivider)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "GOOD MORNING",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Aayush",
                style = MaterialTheme.typography.headlineMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = DopamineCard, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = DopamineWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = DopamineCard, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SessionListItem(session: RecentSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (session.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (session.completed) DopamineWhite else DopamineGrey,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (session.completed) DopamineWhite else DopamineGrey,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = session.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }

            Box(
                modifier = Modifier
                    .background(color = DopamineSurface, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = session.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
