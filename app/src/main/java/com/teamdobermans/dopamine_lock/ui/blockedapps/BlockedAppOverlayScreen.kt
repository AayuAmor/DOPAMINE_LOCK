package com.teamdobermans.dopamine_lock.ui.blockedapps

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard as DopamineCardColor
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDim
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun BlockedAppOverlayScreen(
    blockedAppName: String = "Instagram",
    missionTitle: String = "Deep Work Sprint",
    remainingText: String = "18:42",
    blockedAppsCount: Int = 5,
    onReturnToMission: () -> Unit,
    onViewMissionRules: () -> Unit,
    onAbandonMission: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DopamineBlack)
            .systemBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { BlockedAppOverlayHeader() }
        item { BlockedApplicationCard(blockedAppName = blockedAppName) }
        item {
            MissionStatusCard(
                missionTitle = missionTitle,
                remainingText = remainingText,
                blockedAppsCount = blockedAppsCount
            )
        }
        item { DisciplineWarningCard() }
        item {
            BlockedOverlayActions(
                onReturnToMission = onReturnToMission,
                onViewMissionRules = onViewMissionRules,
                onAbandonMission = onAbandonMission
            )
        }
    }
}

@Composable
private fun BlockedAppOverlayHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(DopamineSurface, CircleShape)
                .border(1.dp, DopamineBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = DopamineWhite,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .background(DopamineWhite, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "MISSION ACTIVE",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineBlack,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "APP BLOCKED",
            style = MaterialTheme.typography.headlineLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Return to your mission. Discipline is built by resistance.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun BlockedApplicationCard(blockedAppName: String) {
    DopamineCard {
        SectionLabel("BLOCKED APPLICATION")
        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DopamineSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, DopamineBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = blockedAppName.firstOrNull()?.uppercaseChar()?.toString() ?: "!",
                    style = MaterialTheme.typography.titleLarge,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column {
                Text(
                    text = blockedAppName,
                    style = MaterialTheme.typography.titleMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Social Media",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "This app is locked until your mission ends.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun MissionStatusCard(
    missionTitle: String,
    remainingText: String,
    blockedAppsCount: Int
) {
    DopamineCard {
        SectionLabel("CURRENT MISSION")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = missionTitle,
            style = MaterialTheme.typography.titleLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusMetric(label = "REMAINING", value = remainingText)
            StatusMetric(label = "APPS BLOCKED", value = blockedAppsCount.toString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        MonochromeProgress(progress = 0.62f)
    }
}

@Composable
private fun DisciplineWarningCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineError.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, DopamineError.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "BREAKING FOCUS COSTS YOU",
            style = MaterialTheme.typography.labelMedium,
            color = DopamineError,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Leaving now may reset your streak and reduce your discipline score.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun BlockedOverlayActions(
    onReturnToMission: () -> Unit,
    onViewMissionRules: () -> Unit,
    onAbandonMission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DopamineButton(
            text = "RETURN TO MISSION",
            onClick = onReturnToMission,
            variant = ButtonVariant.Primary
        )
        DopamineButton(
            text = "VIEW MISSION RULES",
            onClick = onViewMissionRules,
            variant = ButtonVariant.Secondary
        )
        DopamineButton(
            text = "ABANDON MISSION",
            onClick = onAbandonMission,
            variant = ButtonVariant.Danger
        )
    }
}

@Composable
private fun StatusMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineDim,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MonochromeProgress(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(DopamineSurface, RoundedCornerShape(99.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .background(DopamineWhite, RoundedCornerShape(99.dp))
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = DopamineGrey,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BlockedAppOverlayScreenPreview() {
    DOPAMINE_LOCKTheme {
        BlockedAppOverlayScreen(
            onReturnToMission = {},
            onViewMissionRules = {},
            onAbandonMission = {}
        )
    }
}
