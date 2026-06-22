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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.StreakRecord
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class StreakDayState { Completed, Missed, Today, Neutral }

private data class CalendarDay(
    val day: Int?,
    val state: StreakDayState = StreakDayState.Neutral
)

private val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun StreakCalendarScreen(
    currentRoute: String = Screen.Dashboard.route,
    user: User? = null,
    streakRecords: List<StreakRecord> = emptyList(),
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    onNavigate: (String) -> Unit,
    onStartMission: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }

    val recordByDate = remember(streakRecords) { streakRecords.associateBy { it.date } }

    val calendarDays = remember(displayMonth, recordByDate) {
        buildCalendarDays(displayMonth, recordByDate, today)
    }

    val completionRate = remember(streakRecords) { calcCompletionRate(streakRecords) }
    val weekStats = remember(streakRecords) { calcWeekStats(streakRecords, today) }

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
            item {
                StreakSummaryCards(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    completionRate = completionRate
                )
            }
            item {
                DopamineCard {
                    CalendarMonthSelector(
                        displayMonth = displayMonth,
                        onPrevMonth = { displayMonth = displayMonth.minusMonths(1) },
                        onNextMonth = {
                            if (displayMonth.isBefore(YearMonth.now())) {
                                displayMonth = displayMonth.plusMonths(1)
                            }
                        },
                        canGoForward = displayMonth.isBefore(YearMonth.now())
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StreakCalendarGrid(calendarDays = calendarDays)
                    Spacer(modifier = Modifier.height(16.dp))
                    StreakLegend()
                }
            }
            item { WeeklyConsistencyCard(completed = weekStats.first, total = weekStats.second) }
            item { StreakMilestoneCard(currentStreak = currentStreak) }
            item { StreakCalendarActions(onStartMission = onStartMission) }
        }
    }
}

private fun buildCalendarDays(
    yearMonth: YearMonth,
    recordByDate: Map<String, StreakRecord>,
    today: LocalDate
): List<CalendarDay> {
    val firstDay = yearMonth.atDay(1)
    // ISO: Monday=1 … Sunday=7; offset = days of blank cells before day 1
    val leadingBlanks = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    return buildList {
        repeat(leadingBlanks) { add(CalendarDay(day = null)) }
        for (d in 1..daysInMonth) {
            val date = yearMonth.atDay(d)
            val dateStr = date.toString()
            val record = recordByDate[dateStr]
            val state = when {
                date == today -> StreakDayState.Today
                record?.successful == true -> StreakDayState.Completed
                record != null -> StreakDayState.Missed
                else -> StreakDayState.Neutral
            }
            add(CalendarDay(day = d, state = state))
        }
        // Pad trailing cells so the last row is full
        val filled = leadingBlanks + daysInMonth
        val remainder = filled % 7
        if (remainder != 0) repeat(7 - remainder) { add(CalendarDay(day = null)) }
    }
}

private fun calcCompletionRate(records: List<StreakRecord>): Int {
    if (records.isEmpty()) return 0
    return (records.count { it.successful } * 100 / records.size)
}

private fun calcWeekStats(records: List<StreakRecord>, today: LocalDate): Pair<Int, Int> {
    val weekStart = today.with(DayOfWeek.MONDAY)
    val byDate = records.associateBy { it.date }
    var completed = 0
    var tracked = 0
    for (offset in 0L..6L) {
        val date = weekStart.plusDays(offset)
        if (date.isAfter(today)) break
        tracked++
        if (byDate[date.toString()]?.successful == true) completed++
    }
    return Pair(completed, tracked.coerceAtLeast(1))
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
private fun StreakSummaryCards(currentStreak: Int, bestStreak: Int, completionRate: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(value = currentStreak.toString(), label = "Current Streak", unit = "Days", modifier = Modifier.weight(1f))
        SummaryCard(value = bestStreak.toString(), label = "Best Streak", unit = "Days", modifier = Modifier.weight(1f))
        SummaryCard(value = "$completionRate%", label = "Completion Rate", unit = "", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CalendarMonthSelector(
    displayMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    canGoForward: Boolean
) {
    val label = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)).uppercase()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MonthArrowButton(left = true, enabled = true, onClick = onPrevMonth)
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        MonthArrowButton(left = false, enabled = canGoForward, onClick = onNextMonth)
    }
}

@Composable
private fun StreakCalendarGrid(calendarDays: List<CalendarDay>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekDayLabels.forEach { day ->
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

        calendarDays.chunked(7).forEach { week ->
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
private fun WeeklyConsistencyCard(completed: Int, total: Int) {
    DopamineCard {
        SectionLabel("THIS WEEK")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "$completed / $total days completed",
            style = MaterialTheme.typography.titleLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        ProgressBar(progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f)
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
        ProgressBar(progress = if (nextMilestone > 0) currentStreak.toFloat() / nextMilestone.toFloat() else 0f)
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
private fun MonthArrowButton(left: Boolean, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(36.dp)
            .background(if (enabled) DopamineSurface else DopamineSurface.copy(alpha = 0.4f), CircleShape)
            .border(1.dp, DopamineBorder, CircleShape)
    ) {
        Icon(
            imageVector = if (left) Icons.AutoMirrored.Filled.KeyboardArrowLeft
                          else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) DopamineWhite else DopamineGrey
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
private fun StreakCalendarScreenPreview() {
    DOPAMINE_LOCKTheme {
        StreakCalendarScreen(
            user = User(currentStreak = 12, bestStreak = 38),
            currentStreak = 12,
            bestStreak = 38,
            onNavigate = {},
            onStartMission = {}
        )
    }
}
