package com.teamdobermans.dopamine_lock.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.User
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
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

private enum class StreakDayState { Completed, Missed, Today, Neutral }

private data class CalendarDay(
    val day: Int?,
    val state: StreakDayState = StreakDayState.Neutral
)

private val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
private val completedDays = setOf(1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 15, 16)
private val missedDays = setOf(6, 13)
private const val todayDay = 21

private val june2026Days = buildList {
    for (day in 1..30) {
        add(
            CalendarDay(
                day = day,
                state = when (day) {
                    todayDay -> StreakDayState.Today
                    in completedDays -> StreakDayState.Completed
                    in missedDays -> StreakDayState.Missed
                    else -> StreakDayState.Neutral
                }
            )
        )
    }
    repeat(5) { add(CalendarDay(day = null)) }
}

@Composable
fun StreakCalendarScreen(
    currentRoute: String = Screen.Dashboard.route,
    user: User? = null,
    onNavigate: (String) -> Unit,
    onStartMission: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = DopamineBlack,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                .background(DopamineBlack),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { StreakCalendarHeader() }
            item { StreakSummaryCards(user = user) }
            item {
                DopamineCard {
                    CalendarMonthSelector(onMonthChange = {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Month navigation — coming soon") }
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    StreakCalendarGrid()
                    Spacer(modifier = Modifier.height(16.dp))
                    StreakLegend()
                }
            }
            item { WeeklyConsistencyCard() }
            item { StreakMilestoneCard(currentStreak = user?.currentStreak ?: 0) }
            item { StreakCalendarActions(onStartMission = onStartMission) }
        }
    }
}

@Composable
private fun StreakCalendarHeader() {
    Column {
        SectionLabel("STREAK CALENDAR")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Discipline Chain",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Every completed mission strengthens your streak.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun StreakSummaryCards(user: User?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(value = (user?.currentStreak ?: 0).toString(), label = "Current Streak", unit = "Days", modifier = Modifier.weight(1f))
        SummaryCard(value = (user?.bestStreak ?: 0).toString(), label = "Best Streak", unit = "Days", modifier = Modifier.weight(1f))
        SummaryCard(value = "87%", label = "Completion Rate", unit = "", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CalendarMonthSelector(onMonthChange: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonthArrowButton(left = true, onClick = onMonthChange)
        Text(
            text = "JUNE 2026",
            style = MaterialTheme.typography.titleMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        MonthArrowButton(left = false, onClick = onMonthChange)
    }
}

@Composable
private fun StreakCalendarGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekDays.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineDim,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        june2026Days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { calendarDay ->
                    CalendarDayCell(
                        calendarDay = calendarDay,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LegendItem("Completed", StreakDayState.Completed)
        LegendItem("Missed", StreakDayState.Missed)
        LegendItem("Today", StreakDayState.Today)
        LegendItem("No Data", StreakDayState.Neutral)
    }
}

@Composable
private fun WeeklyConsistencyCard() {
    DopamineCard {
        SectionLabel("THIS WEEK")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "5 / 7 days completed",
            style = MaterialTheme.typography.titleLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProgressBar(progress = 5f / 7f)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Stay consistent to protect your chain.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun StreakMilestoneCard(currentStreak: Int) {
    val nextMilestone = when {
        currentStreak < 15 -> 15
        currentStreak < 30 -> 30
        currentStreak < 60 -> 60
        else -> currentStreak + 30
    }

    DopamineCard {
        SectionLabel("NEXT MILESTONE")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "$nextMilestone-Day Streak",
            style = MaterialTheme.typography.titleLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Progress: $currentStreak / $nextMilestone days",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProgressBar(progress = currentStreak.toFloat() / nextMilestone.toFloat())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Reward: Discipline Rank Upgrade",
            style = MaterialTheme.typography.labelMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun StreakCalendarActions(onStartMission: () -> Unit) {
    DopamineButton(
        text = "START TODAY'S MISSION",
        onClick = onStartMission
    )
}

@Composable
private fun CalendarDayCell(calendarDay: CalendarDay, modifier: Modifier = Modifier) {
    val day = calendarDay.day
    val shape = CircleShape
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(DopamineSurface),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            val completed = calendarDay.state == StreakDayState.Completed
            val missed = calendarDay.state == StreakDayState.Missed
            val today = calendarDay.state == StreakDayState.Today
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(if (completed) DopamineWhite else DopamineCardColor, shape)
                    .border(
                        width = if (today || missed) 1.5.dp else 1.dp,
                        color = when {
                            missed -> DopamineError
                            today -> DopamineWhite
                            else -> DopamineBorder
                        },
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        completed -> DopamineBlack
                        missed -> DopamineError
                        else -> DopamineGrey
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, state: StreakDayState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = if (state == StreakDayState.Completed) DopamineWhite else DopamineCardColor,
                    shape = CircleShape
                )
                .border(
                    1.dp,
                    when (state) {
                        StreakDayState.Missed -> DopamineError
                        StreakDayState.Today -> DopamineWhite
                        StreakDayState.Completed -> DopamineWhite
                        StreakDayState.Neutral -> DopamineBorder
                    },
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun MonthArrowButton(left: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(DopamineSurface, CircleShape)
            .border(1.dp, DopamineBorder, CircleShape)
    ) {
        Icon(
            imageVector = if (left) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = DopamineWhite
        )
    }
}

@Composable
private fun SummaryCard(value: String, label: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DopamineCardColor, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = DopamineWhite, fontWeight = FontWeight.Bold)
        if (unit.isNotEmpty()) {
            Text(text = unit.uppercase(), style = MaterialTheme.typography.labelSmall, color = DopamineDim, fontSize = 9.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = DopamineGrey, fontSize = 9.sp)
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
                .fillMaxWidth(progress)
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
private fun StreakCalendarScreenPreview() {
    DOPAMINE_LOCKTheme {
        StreakCalendarScreen(
            user = User(currentStreak = 12, bestStreak = 38),
            onNavigate = {},
            onStartMission = {}
        )
    }
}
