package com.teamdobermans.dopamine_lock.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
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
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun MissionDetailsScreen(
    currentRoute: String = Screen.MissionHome.route,
    mission: Mission?,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "MISSION DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mission?.title?.ifBlank { "Mission" } ?: "Mission not found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mission == null) {
                item {
                    Text("This mission is not available.", color = DopamineGrey)
                }
            } else {
                item {
                    DopamineCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Flag, contentDescription = null, tint = DopamineWhite)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SUMMARY", color = DopamineGrey, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        DetailRow("Goal", mission.goal.ifBlank { "No goal set" })
                        DetailRow("Type", mission.missionType.ifBlank { "Mission" })
                        DetailRow("Duration", "${mission.durationMinutes} min")
                        DetailRow("Blocked apps", mission.blockedApps.size.toString())
                        DetailRow("Status", mission.status.name)
                        DetailRow("Reward", "+${mission.disciplineReward} XP")
                        DetailRow("Penalty", "-${mission.disciplinePenalty} XP")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
        Text(value, style = MaterialTheme.typography.bodySmall, color = DopamineWhite, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(10.dp))
}
