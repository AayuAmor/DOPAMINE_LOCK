package com.teamdobermans.dopamine_lock.ui.focus

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDim
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MissionResultScreen(
    session: FocusSession?,
    completed: Boolean,
    onViewSession: () -> Unit,
    onTryAgain: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val elapsedMinutes = ((session?.elapsedSeconds ?: 0L) / 60L).coerceAtLeast(0L)
    val xp = session?.disciplineXp ?: 0
    val missionName = session?.missionName?.ifBlank { session.missionType.ifBlank { "Focus Session" } } ?: "Focus Session"
    val missionGoal = session?.missionGoal?.ifBlank { "No goal set" } ?: "No goal set"
    val appsBlocked = session?.blockedApps?.size ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DopamineBlack)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // Status icon
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    color = if (completed) DopamineWhite else DopamineError.copy(alpha = 0.12f),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (completed) DopamineWhite else DopamineError,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (completed) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (completed) DopamineBlack else DopamineError,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (completed) "MISSION COMPLETE" else "MISSION ABANDONED",
            style = MaterialTheme.typography.labelLarge,
            color = if (completed) DopamineWhite else DopamineError,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = missionName,
            style = MaterialTheme.typography.headlineSmall,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DopamineCard, RoundedCornerShape(14.dp))
                .border(1.dp, DopamineBorder, RoundedCornerShape(14.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ResultRow("Goal", missionGoal)
            HorizontalDivider(color = DopamineBorder, thickness = 1.dp)
            ResultRow("Duration", "$elapsedMinutes min")
            HorizontalDivider(color = DopamineBorder, thickness = 1.dp)
            ResultRow(
                label = if (xp >= 0) "XP Earned" else "XP Penalty",
                value = if (xp >= 0) "+$xp XP" else "$xp XP",
                valueColor = if (xp >= 0) DopamineWhite else DopamineError
            )
            HorizontalDivider(color = DopamineBorder, thickness = 1.dp)
            ResultRow("Apps Blocked", appsBlocked.toString())
        }

        if (!completed) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DopamineError.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .border(1.dp, DopamineError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "STREAK WARNING",
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineError,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Abandoning missions may affect your daily streak. Stay consistent to protect your chain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DopamineError.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (completed) {
                DopamineButton(
                    text = "VIEW SESSION DETAILS",
                    onClick = onViewSession,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                DopamineButton(
                    text = "TRY AGAIN",
                    onClick = onTryAgain,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            DopamineButton(
                text = "BACK TO DASHBOARD",
                onClick = onBackToDashboard,
                variant = ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = DopamineWhite
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
