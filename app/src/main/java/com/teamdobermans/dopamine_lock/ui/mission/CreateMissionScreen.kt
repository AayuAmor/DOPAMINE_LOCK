package com.teamdobermans.dopamine_lock.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard as DopamineCardColor
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDim
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

private val missionTypes = listOf("Deep Work", "Study", "Coding", "Reading", "Writing", "Custom")
private val sessionDurations = listOf(25, 45, 60, 90, 120)
private val selectedAppsPreview = listOf("Instagram", "TikTok", "YouTube")
private val missionRules = listOf(
    "No social media",
    "Stay focused",
    "Do not abandon session",
    "Respect the timer",
    "Commit fully"
)

@Composable
fun CreateMissionScreen(
    onNavigateBack: () -> Unit,
    onManageApps: () -> Unit,
    onStartMission: () -> Unit,
    onCancel: () -> Unit
) {
    var missionName by remember { mutableStateOf("Deep Work Sprint") }
    var selectedMissionType by remember { mutableStateOf("Deep Work") }
    var selectedDuration by remember { mutableIntStateOf(90) }
    var missionGoal by remember { mutableStateOf("Finish DSA Notes") }

    CreateMissionContent(
        missionName = missionName,
        selectedMissionType = selectedMissionType,
        selectedDuration = selectedDuration,
        missionGoal = missionGoal,
        onMissionNameChange = { missionName = it },
        onMissionTypeSelect = { selectedMissionType = it },
        onDurationSelect = { selectedDuration = it },
        onMissionGoalChange = { missionGoal = it },
        onNavigateBack = onNavigateBack,
        onManageApps = onManageApps,
        onStartMission = onStartMission,
        onCancel = onCancel
    )
}

@Composable
private fun CreateMissionContent(
    missionName: String,
    selectedMissionType: String,
    selectedDuration: Int,
    missionGoal: String,
    onMissionNameChange: (String) -> Unit,
    onMissionTypeSelect: (String) -> Unit,
    onDurationSelect: (Int) -> Unit,
    onMissionGoalChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onManageApps: () -> Unit,
    onStartMission: () -> Unit,
    onCancel: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DopamineBlack)
            .systemBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            CreateMissionHeader(onNavigateBack = onNavigateBack)
        }

        item {
            MissionNameSection(
                missionName = missionName,
                onMissionNameChange = onMissionNameChange
            )
        }

        item {
            MissionTypeSection(
                selectedType = selectedMissionType,
                onTypeSelect = onMissionTypeSelect
            )
        }

        item {
            SessionDurationSection(
                selectedDuration = selectedDuration,
                onDurationSelect = onDurationSelect
            )
        }

        item {
            DistractionBlockingSection(
                selectedApps = selectedAppsPreview,
                onManageApps = onManageApps
            )
        }

        item {
            MissionGoalSection(
                goal = missionGoal,
                onGoalChange = onMissionGoalChange
            )
        }

        item {
            MissionRulesCard()
        }

        item {
            MissionPreviewCard(
                mission = missionName.ifBlank { selectedMissionType },
                duration = selectedDuration,
                appsBlocked = selectedAppsPreview.size,
                goal = missionGoal.ifBlank { "No goal set" }
            )
        }

        item {
            CreateMissionActions(
                onStartMission = onStartMission,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun CreateMissionHeader(
    onNavigateBack: () -> Unit
) {
    Column {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .background(color = DopamineCardColor, shape = CircleShape)
                .border(1.dp, DopamineBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DopamineWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "CREATE MISSION",
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Define Your Focus",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Set your objective before entering Mission Mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun MissionNameSection(
    missionName: String,
    onMissionNameChange: (String) -> Unit
) {
    DopamineCard {
        SectionLabel("MISSION NAME")
        Spacer(modifier = Modifier.height(12.dp))
        DopamineTextField(
            value = missionName,
            onValueChange = onMissionNameChange,
            label = "Mission Name",
            placeholder = "Finish DSA Notes",
            imeAction = ImeAction.Next
        )
    }
}

@Composable
private fun MissionTypeSection(
    selectedType: String,
    onTypeSelect: (String) -> Unit
) {
    Column {
        SectionLabel("MISSION TYPE")
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(missionTypes) { type ->
                SelectableMissionChip(
                    text = type,
                    selected = type == selectedType,
                    onClick = { onTypeSelect(type) }
                )
            }
        }
    }
}

@Composable
private fun SessionDurationSection(
    selectedDuration: Int,
    onDurationSelect: (Int) -> Unit
) {
    Column {
        SectionLabel("SESSION DURATION")
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sessionDurations) { duration ->
                SelectableMissionChip(
                    text = "$duration MIN",
                    selected = duration == selectedDuration,
                    onClick = { onDurationSelect(duration) },
                    minWidth = 86.dp
                )
            }
        }
    }
}

@Composable
private fun DistractionBlockingSection(
    selectedApps: List<String>,
    onManageApps: () -> Unit
) {
    Column {
        SectionLabel("DISTRACTION BLOCKING")
        Spacer(modifier = Modifier.height(10.dp))
        DopamineCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Apps Selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = DopamineWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    selectedApps.forEach { app ->
                        Text(
                            text = app,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DopamineGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                SmallOutlinedAction(
                    text = "MANAGE APPS",
                    onClick = onManageApps
                )
            }
        }
    }
}

@Composable
private fun MissionGoalSection(
    goal: String,
    onGoalChange: (String) -> Unit
) {
    Column {
        SectionLabel("MISSION GOAL")
        Spacer(modifier = Modifier.height(10.dp))
        DopamineTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = "Mission Goal",
            placeholder = "Complete chapter 4",
            singleLine = false,
            maxLines = 4,
            imeAction = ImeAction.Default,
            modifier = Modifier.height(132.dp)
        )
    }
}

@Composable
private fun MissionRulesCard() {
    Column {
        SectionLabel("MISSION RULES")
        Spacer(modifier = Modifier.height(10.dp))
        DopamineCard {
            missionRules.forEachIndexed { index, rule ->
                Text(
                    text = "✓ $rule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Medium
                )
                if (index < missionRules.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MissionPreviewCard(
    mission: String,
    duration: Int,
    appsBlocked: Int,
    goal: String
) {
    DopamineCard {
        SectionLabel("MISSION PREVIEW")
        Spacer(modifier = Modifier.height(14.dp))
        PreviewRow(label = "Mission:", value = mission)
        PreviewRow(label = "Duration:", value = "$duration Minutes")
        PreviewRow(label = "Apps Blocked:", value = appsBlocked.toString())
        PreviewRow(label = "Goal:", value = goal)
    }
}

@Composable
private fun CreateMissionActions(
    onStartMission: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DopamineButton(
            text = "START MISSION",
            onClick = onStartMission,
            variant = ButtonVariant.Primary
        )
        DopamineButton(
            text = "CANCEL",
            onClick = onCancel,
            variant = ButtonVariant.Secondary
        )
    }
}

@Composable
private fun SelectableMissionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    minWidth: androidx.compose.ui.unit.Dp = 112.dp
) {
    Box(
        modifier = Modifier
            .width(minWidth)
            .height(52.dp)
            .background(
                color = if (selected) DopamineWhite else DopamineCardColor,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) DopamineWhite else DopamineBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) DopamineBlack else DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SmallOutlinedAction(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(color = DopamineSurface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineDim,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.SemiBold
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
private fun CreateMissionScreenPreview() {
    DOPAMINE_LOCKTheme {
        CreateMissionContent(
            missionName = "Deep Work Sprint",
            selectedMissionType = "Deep Work",
            selectedDuration = 90,
            missionGoal = "Finish DSA Notes",
            onMissionNameChange = {},
            onMissionTypeSelect = {},
            onDurationSelect = {},
            onMissionGoalChange = {},
            onNavigateBack = {},
            onManageApps = {},
            onStartMission = {},
            onCancel = {}
        )
    }
}
