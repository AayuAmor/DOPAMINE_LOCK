package com.teamdobermans.dopamine_lock.ui.goals

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.GoalType
import com.teamdobermans.dopamine_lock.model.GoalUnit
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
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

private data class GoalItem(
    val id: String,
    val title: String,
    val category: String,
    val type: String,
    val target: String,
    val progressPercent: Int,
    val status: String,
    val deadline: String,
    val currentProgress: String,
    val remaining: String,
    val bestDay: String,
    val suggestedAction: String,
    val completed: Boolean = false
)

private val goalFilters = listOf("All", "Daily", "Weekly", "Monthly", "Completed")

@Composable
fun GoalTrackingScreen(
    currentRoute: String = Screen.Dashboard.route,
    goals: List<Goal> = emptyList(),
    dailyGoals: List<Goal> = emptyList(),
    weeklyGoals: List<Goal> = emptyList(),
    monthlyGoals: List<Goal> = emptyList(),
    activeGoals: List<Goal> = emptyList(),
    completedGoals: List<Goal> = emptyList(),
    onCreateGoal: (String, String, GoalType, Int, GoalUnit) -> Unit = { _, _, _, _, _ -> },
    onNavigate: (String) -> Unit,
    onStartMission: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedGoalId by remember { mutableStateOf("") }
    var showCreateGoal by remember { mutableStateOf(false) }

    val goalItems = remember(goals) { goals.map { it.toGoalItem() } }
    val visibleGoals = remember(selectedFilter, goalItems) {
        when (selectedFilter) {
            "Completed" -> goalItems.filter { it.completed }
            "All" -> goalItems
            else -> goalItems.filter { it.type == selectedFilter }
        }
    }
    val selectedGoal = goalItems.firstOrNull { it.id == selectedGoalId } ?: goalItems.firstOrNull()

    Scaffold(
        containerColor = DopamineBlack,
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
                .background(DopamineBlack)
                .systemBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { GoalTrackingHeader() }
            item {
                GoalSummaryCards(
                    dailyGoals = dailyGoals,
                    weeklyGoals = weeklyGoals,
                    monthlyGoals = monthlyGoals
                )
            }
            item {
                GoalFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            item {
                ActiveGoalsSection(
                    goals = visibleGoals,
                    selectedGoalId = selectedGoalId,
                    onViewDetails = { selectedGoalId = it }
                )
            }
            selectedGoal?.let { goal ->
                item { SelectedGoalPreviewCard(goal = goal) }
                item { GoalMilestoneCard(goal = goal) }
            }
            item {
                GoalTrackingActions(
                    onCreateGoal = { showCreateGoal = true },
                    onStartMission = onStartMission
                )
            }
        }
    }

    if (showCreateGoal) {
        CreateGoalBottomSheet(
            onSave = { title, description, goalType, targetValue, unit ->
                onCreateGoal(title, description, goalType, targetValue, unit)
                showCreateGoal = false
            },
            onCancel = { showCreateGoal = false }
        )
    }
}

@Composable
private fun GoalTrackingHeader() {
    Column {
        SectionLabel("GOAL TRACKING")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Mission Objectives",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Track the targets that build your discipline.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun GoalSummaryCards(
    dailyGoals: List<Goal>,
    weeklyGoals: List<Goal>,
    monthlyGoals: List<Goal>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoalSummaryCard(goalSummary(dailyGoals), "Daily Goal", Modifier.weight(1f))
        GoalSummaryCard(goalSummary(weeklyGoals), "Weekly Goal", Modifier.weight(1f))
        GoalSummaryCard(goalSummary(monthlyGoals), "Monthly Goal", Modifier.weight(1f))
    }
}

@Composable
private fun GoalFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(goalFilters) { filter ->
            GoalFilterChip(
                text = filter,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun ActiveGoalsSection(
    goals: List<GoalItem>,
    selectedGoalId: String,
    onViewDetails: (String) -> Unit
) {
    Column {
        SectionLabel("ACTIVE GOALS")
        Spacer(modifier = Modifier.height(10.dp))
        if (goals.isEmpty()) {
            GoalEmptyState()
        } else {
            goals.forEachIndexed { index, goal ->
                GoalProgressCard(
                    goal = goal,
                    selected = goal.id == selectedGoalId,
                    onViewDetails = { onViewDetails(goal.id) }
                )
                if (index < goals.lastIndex) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GoalProgressCard(
    goal: GoalItem,
    selected: Boolean,
    onViewDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineCardColor, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (selected) DopamineWhite else DopamineBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(goal.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = DopamineGrey, letterSpacing = 1.2.sp)
            }
            StatusBadge(goal.status)
        }
        Spacer(modifier = Modifier.height(12.dp))
        GoalInfoRow("TARGET", goal.target)
        GoalInfoRow("DEADLINE", goal.deadline)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progress", style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
            Text("${goal.progressPercent}%", style = MaterialTheme.typography.labelMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(goal.progressPercent / 100f)
        Spacer(modifier = Modifier.height(14.dp))
        SmallAction(text = "VIEW DETAILS", onClick = onViewDetails)
    }
}

@Composable
private fun SelectedGoalPreviewCard(goal: GoalItem) {
    DopamineCard {
        SectionLabel("SELECTED GOAL")
        Spacer(modifier = Modifier.height(12.dp))
        Text(goal.title, style = MaterialTheme.typography.titleLarge, color = DopamineWhite, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        GoalInfoRow("TARGET", goal.target)
        GoalInfoRow("CURRENT", goal.currentProgress)
        GoalInfoRow("REMAINING", goal.remaining)
        GoalInfoRow("DEADLINE", goal.deadline)
        GoalInfoRow("BEST DAY", goal.bestDay)
        Spacer(modifier = Modifier.height(10.dp))
        Text("SUGGESTED ACTION", style = MaterialTheme.typography.labelSmall, color = DopamineDim, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(goal.suggestedAction, style = MaterialTheme.typography.bodyMedium, color = DopamineWhite)
    }
}

@Composable
private fun GoalMilestoneCard(goal: GoalItem) {
    DopamineCard {
        SectionLabel("NEXT MILESTONE")
        Spacer(modifier = Modifier.height(10.dp))
        Text(goal.title, style = MaterialTheme.typography.titleLarge, color = DopamineWhite, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Reward: Discipline XP on completion", style = MaterialTheme.typography.bodyMedium, color = DopamineGrey)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progress", style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
            Text(goal.currentProgress, style = MaterialTheme.typography.labelMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(goal.progressPercent / 100f)
    }
}

@Composable
private fun GoalTrackingActions(
    onCreateGoal: () -> Unit,
    onStartMission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DopamineButton(
            text = "CREATE NEW GOAL",
            onClick = onCreateGoal,
            variant = ButtonVariant.Secondary
        )
        DopamineButton(
            text = "START MISSION",
            onClick = onStartMission,
            variant = ButtonVariant.Primary
        )
    }
}

@Composable
private fun GoalEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(DopamineCardColor, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("No goals found", style = MaterialTheme.typography.bodyMedium, color = DopamineGrey)
    }
}

@Composable
private fun CreateGoalBottomSheet(
    onSave: (String, String, GoalType, Int, GoalUnit) -> Unit,
    onCancel: () -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf("DAILY") }
    var goalUnit by remember { mutableStateOf("HOURS") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = DopamineCardColor,
        titleContentColor = DopamineWhite,
        textContentColor = DopamineGrey,
        title = { Text("CREATE GOAL", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DopamineTextField(value = goalName, onValueChange = { goalName = it }, label = "Goal name", placeholder = "Deep Work Hours")
                DopamineTextField(value = targetAmount, onValueChange = { targetAmount = it }, label = "Target amount", placeholder = "100")
                DopamineTextField(value = goalType, onValueChange = { goalType = it }, label = "Type", placeholder = "DAILY / WEEKLY / MONTHLY")
                DopamineTextField(value = goalUnit, onValueChange = { goalUnit = it }, label = "Unit", placeholder = "HOURS / SESSIONS / MISSIONS")
                DopamineTextField(value = description, onValueChange = { description = it }, label = "Description", placeholder = "Focus", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            DialogAction("SAVE GOAL") {
                onSave(
                    goalName,
                    description,
                    goalType.toGoalType(),
                    targetAmount.toIntOrNull() ?: 0,
                    goalUnit.toGoalUnit()
                )
            }
        },
        dismissButton = {
            DialogAction("CANCEL", onCancel)
        }
    )
}

@Composable
private fun GoalSummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DopamineCardColor, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = DopamineGrey, fontSize = 9.sp)
    }
}

@Composable
private fun GoalFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) DopamineWhite else DopamineCardColor, RoundedCornerShape(99.dp))
            .border(1.dp, if (selected) DopamineWhite else DopamineBorder, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) DopamineBlack else DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    Box(
        modifier = Modifier
            .background(DopamineSurface, RoundedCornerShape(6.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, color = DopamineWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun SmallAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(DopamineSurface, RoundedCornerShape(8.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = DopamineWhite, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun DialogAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = DopamineWhite,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(10.dp)
    )
}

@Composable
private fun GoalInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = DopamineDim, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .background(DopamineSurface, RoundedCornerShape(99.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(7.dp)
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

private fun Goal.toGoalItem(): GoalItem {
    val progressPercent = if (targetValue <= 0) {
        0
    } else {
        ((currentProgress.toFloat() / targetValue.toFloat()) * 100).toInt().coerceIn(0, 100)
    }
    val unitLabel = unit.name.lowercase()
    val remainingValue = (targetValue - currentProgress).coerceAtLeast(0)

    return GoalItem(
        id = goalId,
        title = title.ifBlank { "${goalType.name.lowercase().replaceFirstChar { it.uppercase() }} Goal" },
        category = description.ifBlank { unitLabel },
        type = goalType.name.lowercase().replaceFirstChar { it.uppercase() },
        target = "$targetValue $unitLabel",
        progressPercent = progressPercent,
        status = if (completed) "Completed" else if (progressPercent >= 75) "Strong" else "Active",
        deadline = if (endDate > 0L) "Ends ${endDate.toDateLabel()}" else "Current period",
        currentProgress = "$currentProgress / $targetValue $unitLabel",
        remaining = "$remainingValue $unitLabel",
        bestDay = goalType.name.lowercase().replaceFirstChar { it.uppercase() },
        suggestedAction = "Complete ${unit.name.lowercase()} to move this goal forward",
        completed = completed
    )
}

private fun goalSummary(goals: List<Goal>): String {
    val goal = goals.firstOrNull { !it.completed } ?: goals.firstOrNull() ?: return "0 / 0"
    return "${goal.currentProgress} / ${goal.targetValue}${goal.unit.shortLabel()}"
}

private fun GoalUnit.shortLabel(): String {
    return when (this) {
        GoalUnit.HOURS -> "h"
        GoalUnit.MISSIONS -> "m"
        GoalUnit.SESSIONS -> "s"
    }
}

private fun String.toGoalType(): GoalType {
    return GoalType.entries.firstOrNull { it.name.equals(trim(), ignoreCase = true) } ?: GoalType.DAILY
}

private fun String.toGoalUnit(): GoalUnit {
    return GoalUnit.entries.firstOrNull { it.name.equals(trim(), ignoreCase = true) } ?: GoalUnit.HOURS
}

private fun Long.toDateLabel(): String {
    if (this <= 0L) return "current period"
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = this@toDateLabel }
    val month = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, java.util.Locale.getDefault())
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    return "$month $day"
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GoalTrackingScreenPreview() {
    DOPAMINE_LOCKTheme {
        GoalTrackingScreen(
            onNavigate = {},
            onStartMission = {}
        )
    }
}
