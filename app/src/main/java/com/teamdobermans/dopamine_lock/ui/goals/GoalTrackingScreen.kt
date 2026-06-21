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

private val mockGoals = listOf(
    GoalItem(
        id = "deep-work-hours",
        title = "Deep Work Hours",
        category = "Focus",
        type = "Monthly",
        target = "100h monthly",
        progressPercent = 73,
        status = "On Track",
        deadline = "June 30, 2026",
        currentProgress = "73h",
        remaining = "27h",
        bestDay = "Friday",
        suggestedAction = "Complete one 90-minute mission today"
    ),
    GoalItem(
        id = "study-sessions",
        title = "Study Sessions",
        category = "Study",
        type = "Monthly",
        target = "40 sessions monthly",
        progressPercent = 65,
        status = "Needs Push",
        deadline = "June 30, 2026",
        currentProgress = "26 sessions",
        remaining = "14 sessions",
        bestDay = "Tuesday",
        suggestedAction = "Schedule two short study missions"
    ),
    GoalItem(
        id = "no-abandon",
        title = "No-Abandon Missions",
        category = "Discipline",
        type = "Weekly",
        target = "20 missions",
        progressPercent = 85,
        status = "Strong",
        deadline = "June 28, 2026",
        currentProgress = "17 missions",
        remaining = "3 missions",
        bestDay = "Monday",
        suggestedAction = "Protect the next mission from interruption"
    )
)

@Composable
fun GoalTrackingScreen(
    currentRoute: String = Screen.Dashboard.route,
    onNavigate: (String) -> Unit,
    onStartMission: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedGoalId by remember { mutableStateOf(mockGoals.first().id) }
    var showCreateGoal by remember { mutableStateOf(false) }

    val visibleGoals = remember(selectedFilter) {
        when (selectedFilter) {
            "Completed" -> mockGoals.filter { it.completed }
            "All" -> mockGoals
            else -> mockGoals.filter { it.type == selectedFilter }
        }
    }
    val selectedGoal = mockGoals.firstOrNull { it.id == selectedGoalId } ?: mockGoals.first()

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
            item { GoalSummaryCards() }
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
            item { SelectedGoalPreviewCard(goal = selectedGoal) }
            item { GoalMilestoneCard() }
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
            onSave = { showCreateGoal = false },
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
private fun GoalSummaryCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoalSummaryCard("4 / 6h", "Daily Goal", Modifier.weight(1f))
        GoalSummaryCard("21 / 30h", "Weekly Goal", Modifier.weight(1f))
        GoalSummaryCard("73 / 100h", "Monthly Goal", Modifier.weight(1f))
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
private fun GoalMilestoneCard() {
    DopamineCard {
        SectionLabel("NEXT MILESTONE")
        Spacer(modifier = Modifier.height(10.dp))
        Text("80% Monthly Focus Goal", style = MaterialTheme.typography.titleLarge, color = DopamineWhite, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Reward: Discipline XP +50", style = MaterialTheme.typography.bodyMedium, color = DopamineGrey)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progress", style = MaterialTheme.typography.bodySmall, color = DopamineGrey)
            Text("73 / 80", style = MaterialTheme.typography.labelMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(73f / 80f)
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
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

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
                DopamineTextField(value = deadline, onValueChange = { deadline = it }, label = "Deadline", placeholder = "June 30, 2026")
                DopamineTextField(value = category, onValueChange = { category = it }, label = "Category", placeholder = "Focus", imeAction = ImeAction.Done)
                Text("TYPE: DAILY / WEEKLY / MONTHLY", style = MaterialTheme.typography.labelSmall, color = DopamineDim, letterSpacing = 1.sp)
                Text("UNIT: HOURS / SESSIONS / MISSIONS", style = MaterialTheme.typography.labelSmall, color = DopamineDim, letterSpacing = 1.sp)
            }
        },
        confirmButton = {
            DialogAction("SAVE GOAL", onSave)
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
