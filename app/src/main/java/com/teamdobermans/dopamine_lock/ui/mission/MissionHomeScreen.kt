package com.teamdobermans.dopamine_lock.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
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
import com.teamdobermans.dopamine_lock.model.DisciplineRank
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.components.SectionHeader
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun MissionHomeScreen(
    currentRoute: String = Screen.MissionHome.route,
    activeMission: Mission?,
    missions: List<Mission>,
    currentStreak: Int,
    bestStreak: Int,
    disciplineScore: Int,
    disciplineRank: DisciplineRank,
    onNavigate: (String) -> Unit,
    onCreateMission: () -> Unit,
    onResumeMission: () -> Unit,
    onOpenBlockedApps: () -> Unit,
    onOpenMissionHistory: () -> Unit,
    onOpenStreak: () -> Unit,
    onOpenScore: () -> Unit,
    onOpenMissionDetails: (String) -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                MissionHeader()
            }

            item {
                ActiveMissionCard(
                    activeMission = activeMission,
                    onCreateMission = onCreateMission,
                    onResumeMission = onResumeMission
                )
            }

            item {
                QuickActions(
                    onCreateMission = onCreateMission,
                    onOpenBlockedApps = onOpenBlockedApps,
                    onOpenMissionHistory = onOpenMissionHistory
                )
            }

            item {
                StreakAndScoreCards(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    disciplineScore = disciplineScore,
                    disciplineRank = disciplineRank,
                    onOpenStreak = onOpenStreak,
                    onOpenScore = onOpenScore
                )
            }

            item {
                SectionHeader(title = "Recent Missions")
            }

            val recentMissions = missions.sortedByDescending { it.createdAt }.take(5)
            if (recentMissions.isEmpty()) {
                item {
                    EmptyRecentMissions()
                }
            } else {
                items(recentMissions.size) { index ->
                    MissionRow(
                        mission = recentMissions[index],
                        onClick = { onOpenMissionDetails(recentMissions[index].missionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionHeader() {
    Column {
        Text(
            text = "MISSION CONTROL",
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Discipline enforcement hub",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActiveMissionCard(
    activeMission: Mission?,
    onCreateMission: () -> Unit,
    onResumeMission: () -> Unit
) {
    DopamineCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = DopamineWhite)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ACTIVE MISSION",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        if (activeMission == null) {
            Text(
                text = "No active mission",
                style = MaterialTheme.typography.titleLarge,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create a mission to start enforcing your next focused block.",
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey
            )
            Spacer(modifier = Modifier.height(16.dp))
            DopamineButton(text = "CREATE MISSION", onClick = onCreateMission)
        } else {
            Text(
                text = activeMission.title.ifBlank { "Mission" },
                style = MaterialTheme.typography.titleLarge,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activeMission.goal.ifBlank { "No goal set" },
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey
            )
            Spacer(modifier = Modifier.height(14.dp))
            InfoRow("Time remaining", missionRemainingText(activeMission))
            InfoRow("Status", activeMission.status.name)
            Spacer(modifier = Modifier.height(16.dp))
            DopamineButton(
                text = "RESUME MISSION",
                onClick = onResumeMission,
                leadingIcon = Icons.Filled.PlayArrow
            )
        }
    }
}

@Composable
private fun QuickActions(
    onCreateMission: () -> Unit,
    onOpenBlockedApps: () -> Unit,
    onOpenMissionHistory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Quick Actions")
        DopamineButton(text = "CREATE MISSION", onClick = onCreateMission, variant = ButtonVariant.Primary)
        DopamineButton(text = "BLOCKED APPS", onClick = onOpenBlockedApps, variant = ButtonVariant.Secondary)
        DopamineButton(text = "MISSION HISTORY", onClick = onOpenMissionHistory, variant = ButtonVariant.Secondary)
    }
}

@Composable
private fun StreakAndScoreCards(
    currentStreak: Int,
    bestStreak: Int,
    disciplineScore: Int,
    disciplineRank: DisciplineRank,
    onOpenStreak: () -> Unit,
    onOpenScore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DopamineCard {
            SectionHeader(title = "Mission Streak")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Current streak", "$currentStreak days")
            InfoRow("Best streak", "$bestStreak days")
            Spacer(modifier = Modifier.height(12.dp))
            DopamineButton(text = "VIEW STREAK", onClick = onOpenStreak, variant = ButtonVariant.Secondary)
        }
        DopamineCard {
            SectionHeader(title = "Discipline Score")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Score", disciplineScore.toString())
            InfoRow("Rank", disciplineRank.name)
            Spacer(modifier = Modifier.height(12.dp))
            DopamineButton(text = "VIEW SCORE", onClick = onOpenScore, variant = ButtonVariant.Secondary)
        }
    }
}

@Composable
private fun EmptyRecentMissions() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No missions yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun MissionRow(mission: Mission, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineSurface, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = Icons.Filled.Shield, contentDescription = null, tint = DopamineWhite)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.title.ifBlank { "Mission" },
                style = MaterialTheme.typography.bodyLarge,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${mission.durationMinutes} min · ${mission.status.name}",
                style = MaterialTheme.typography.bodySmall,
                color = DopamineGrey
            )
        }
        Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = DopamineGrey)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = DopamineWhite, fontWeight = FontWeight.Bold)
    }
}

private fun missionRemainingText(mission: Mission): String {
    if (mission.startedAt <= 0L) return "${mission.durationMinutes} min"
    val remainingMs = (mission.durationMinutes * 60_000L - (System.currentTimeMillis() - mission.startedAt)).coerceAtLeast(0L)
    val minutes = remainingMs / 60_000L
    val seconds = (remainingMs % 60_000L) / 1_000L
    return "%02d:%02d".format(minutes, seconds)
}
