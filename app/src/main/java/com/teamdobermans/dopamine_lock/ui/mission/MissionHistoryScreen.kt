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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun MissionHistoryScreen(
    currentRoute: String = Screen.MissionHome.route,
    missions: List<Mission>,
    onNavigate: (String) -> Unit,
    onMissionClick: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "MISSION HISTORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Review completed and abandoned missions",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val sortedMissions = missions.sortedByDescending { it.createdAt }
            if (sortedMissions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No mission history yet.", color = DopamineGrey)
                    }
                }
            } else {
                items(sortedMissions.size) { index ->
                    HistoryMissionRow(
                        mission = sortedMissions[index],
                        onClick = { onMissionClick(sortedMissions[index].missionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryMissionRow(mission: Mission, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineSurface, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = DopamineWhite)
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
    }
}
