package com.teamdobermans.dopamine_lock.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailsScreen(
    session: FocusSession?,
    onBack: () -> Unit,
    onStartSimilarMission: () -> Unit
) {
    if (session == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DopamineBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Session not found", style = MaterialTheme.typography.titleMedium, color = DopamineWhite)
                Spacer(modifier = Modifier.height(16.dp))
                DopamineButton(text = "Go Back", onClick = onBack, variant = ButtonVariant.Secondary)
            }
        }
        return
    }

    val elapsedMinutes = (session.elapsedSeconds / 60L).coerceAtLeast(0L)
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    val startedLabel = if (session.startedAt > 0L) fmt.format(Date(session.startedAt)) else "Unknown"
    val endedLabel = if (session.endedAt > 0L) fmt.format(Date(session.endedAt)) else "Ongoing"
    val statusLabel = when {
        session.completed -> "Completed"
        session.abandoned -> "Abandoned"
        else -> "Failed"
    }
    val statusColor = when {
        session.completed -> DopamineWhite
        else -> DopamineError
    }
    val xp = session.disciplineXp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DopamineBlack),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 24.dp, bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            // Back button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(DopamineCard, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DopamineWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "SESSION DETAILS",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.missionName.ifBlank { session.missionType.ifBlank { "Focus Session" } },
                style = MaterialTheme.typography.headlineSmall,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            DetailsCard {
                DetailRow("GOAL", session.missionGoal.ifBlank { "No goal set" })
                DetailsCardDivider()
                DetailRow("SESSION TYPE", session.missionType.ifBlank { "Focus" }.uppercase())
                DetailsCardDivider()
                DetailRow("STATUS", statusLabel, valueColor = statusColor)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            DetailsCard {
                DetailRow("STARTED", startedLabel)
                DetailsCardDivider()
                DetailRow("ENDED", endedLabel)
                DetailsCardDivider()
                DetailRow("DURATION", "$elapsedMinutes min")
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            DetailsCard {
                DetailRow(
                    label = if (xp >= 0) "XP EARNED" else "XP PENALTY",
                    value = if (xp >= 0) "+$xp XP" else "$xp XP",
                    valueColor = if (xp >= 0) DopamineWhite else DopamineError
                )
                DetailsCardDivider()
                DetailRow("APPS BLOCKED", session.blockedApps.size.toString())
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (session.blockedApps.isNotEmpty()) {
            item {
                DetailsCard {
                    Text(
                        text = "BLOCKED APPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineGrey,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    session.blockedApps.forEach { pkg ->
                        Text(
                            text = "• ${pkg.substringAfterLast(".")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DopamineWhite,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        } else {
            item { Spacer(modifier = Modifier.height(14.dp)) }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DopamineButton(
                    text = "START SIMILAR MISSION",
                    onClick = onStartSimilarMission,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
                DopamineButton(
                    text = "BACK",
                    onClick = onBack,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineCard, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

@Composable
private fun DetailsCardDivider() {
    HorizontalDivider(color = DopamineBorder, thickness = 1.dp)
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = DopamineWhite
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
