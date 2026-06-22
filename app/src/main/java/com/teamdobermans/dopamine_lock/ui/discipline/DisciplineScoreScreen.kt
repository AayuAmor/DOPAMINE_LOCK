package com.teamdobermans.dopamine_lock.ui.discipline

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.DisciplineEventType
import com.teamdobermans.dopamine_lock.model.DisciplineRank
import com.teamdobermans.dopamine_lock.model.User
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
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
import com.teamdobermans.dopamine_lock.util.DisciplineRankCalculator

private data class ScoreBreakdown(val label: String, val points: Int)

private val rankLadder = DisciplineRank.entries.map { it.name }

@Composable
fun DisciplineScoreScreen(
    currentRoute: String = Screen.Analytics.route,
    user: User? = null,
    disciplineScore: Int = user?.disciplineScore ?: 0,
    disciplineRank: DisciplineRank = DisciplineRankCalculator.calculateRank(disciplineScore),
    events: List<DisciplineEvent> = emptyList(),
    onNavigate: (String) -> Unit,
    onViewStreakCalendar: () -> Unit,
    onStartMission: () -> Unit
) {
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
                .background(DopamineBlack),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { DisciplineScoreHeader() }
            item { DisciplineHeroCard(score = disciplineScore, rank = disciplineRank) }
            item { RankProgressSection(score = disciplineScore, rank = disciplineRank) }
            item { ScoreBreakdownCard(items = eventBreakdown(events)) }
            item { DisciplineTrendCard(values = scoreTrend(events, disciplineScore)) }
            item { AchievementBadgesSection(achievements = achievementBadges(events)) }
            item { IdentityShiftCard() }
            item {
                DisciplineScoreActions(
                    onViewStreakCalendar = onViewStreakCalendar,
                    onStartMission = onStartMission
                )
            }
        }
    }
}

@Composable
private fun DisciplineScoreHeader() {
    Column {
        SectionLabel("DISCIPLINE SCORE")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your Discipline Rank",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Discipline is measured by what you complete, not what you plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun DisciplineHeroCard(score: Int, rank: DisciplineRank) {
    val nextRankTarget = nextRankTarget(score)
    val rankFloor = DisciplineRankCalculator.currentRankFloor(score)
    val progress = ((score - rankFloor).toFloat() / (nextRankTarget - rankFloor).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    DopamineCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${rank.name} RANK",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (score > 0) "Top 12% consistency" else "Build consistency to rank up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineGrey
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "$score / $nextRankTarget TO NEXT RANK",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineDim,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
            ScoreArc(progress = progress)
        }
    }
}

@Composable
private fun RankProgressSection(score: Int, rank: DisciplineRank) {
    val nextRank = DisciplineRankCalculator.nextRank(score)?.name ?: "MAX"
    val needed = (nextRankTarget(score) - score).coerceAtLeast(0)

    DopamineCard {
        SectionLabel("RANK SYSTEM")
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RankMetric("CURRENT", rank.name)
            RankMetric("NEXT", nextRank)
            RankMetric("NEEDED", "$needed XP")
        }
        Spacer(modifier = Modifier.height(18.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rankLadder) { rank ->
                RankBadge(rank = rank, selected = rank == DisciplineRankCalculator.calculateRank(score).name)
            }
        }
    }
}

@Composable
private fun ScoreBreakdownCard(items: List<ScoreBreakdown>) {
    DopamineCard {
        SectionLabel("SCORE BREAKDOWN")
        Spacer(modifier = Modifier.height(12.dp))
        items.forEachIndexed { index, item ->
            ScoreBreakdownRow(item)
            if (index < items.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DisciplineTrendCard(values: List<Int>) {
    DopamineCard {
        SectionLabel("7-DAY SCORE TREND")
        Spacer(modifier = Modifier.height(16.dp))
        TrendLineChart(values = values)
    }
}

@Composable
private fun AchievementBadgesSection(achievements: List<String>) {
    Column {
        SectionLabel("ACHIEVEMENT BADGES")
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(achievements) { achievement ->
                AchievementBadge(text = achievement)
            }
        }
    }
}

@Composable
private fun IdentityShiftCard() {
    DopamineCard {
        Text(
            text = "IDENTITY SHIFT",
            style = MaterialTheme.typography.labelMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are becoming the type of person who finishes what they start.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun DisciplineScoreActions(
    onViewStreakCalendar: () -> Unit,
    onStartMission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DopamineButton(
            text = "VIEW STREAK CALENDAR",
            onClick = onViewStreakCalendar,
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
private fun ScoreArc(progress: Float) {
    Box(
        modifier = Modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            drawArc(
                color = DopamineBorder,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White,
                startAngle = 140f,
                sweepAngle = 260f * progress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun nextRankTarget(score: Int): Int {
    return DisciplineRankCalculator.nextRankTarget(score)
}

private fun eventBreakdown(events: List<DisciplineEvent>): List<ScoreBreakdown> {
    if (events.isEmpty()) return listOf(ScoreBreakdown("No discipline events yet", 0))

    return events
        .groupBy { it.eventType }
        .map { (type, groupedEvents) ->
            ScoreBreakdown(label = eventTypeLabel(type), points = groupedEvents.sumOf { it.points })
        }
        .sortedByDescending { it.points }
}

private fun achievementBadges(events: List<DisciplineEvent>): List<String> {
    val milestones = events
        .filter { it.eventType == DisciplineEventType.STREAK_MILESTONE.name }
        .map { it.description.ifBlank { "Streak Milestone" } }

    return (milestones + listOf("Mission Finisher", "Focus Builder")).distinct().take(4)
}

private fun scoreTrend(events: List<DisciplineEvent>, currentScore: Int): List<Int> {
    if (events.isEmpty()) return List(7) { currentScore }

    val dailyTotals = events
        .groupBy { it.createdAt / DAY_MS }
        .toSortedMap()
        .values
        .map { dayEvents -> dayEvents.sumOf { it.points } }
        .takeLast(7)

    var runningScore = (currentScore - dailyTotals.sum()).coerceAtLeast(0)
    return dailyTotals.map { points ->
        runningScore = (runningScore + points).coerceAtLeast(0)
        runningScore
    }.let { values ->
        if (values.size >= 2) values else List(7) { currentScore }
    }
}

private fun eventTypeLabel(type: String): String {
    return type.lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private const val DAY_MS = 24 * 60 * 60 * 1000L

@Composable
private fun TrendLineChart(values: List<Int>) {
    val min = values.minOrNull() ?: 0
    val max = values.maxOrNull() ?: 1
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(DopamineSurface, RoundedCornerShape(10.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        val horizontalPadding = 12.dp.toPx()
        val verticalPadding = 18.dp.toPx()
        val chartWidth = size.width - horizontalPadding * 2
        val chartHeight = size.height - verticalPadding * 2
        val stepX = chartWidth / (values.lastIndex.coerceAtLeast(1))
        val range = (max - min).coerceAtLeast(1)
        val path = Path()

        values.forEachIndexed { index, value ->
            val x = horizontalPadding + stepX * index
            val y = verticalPadding + chartHeight * (1f - (value - min).toFloat() / range)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun RankMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineDim,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
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
private fun RankBadge(rank: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 48.dp)
            .background(if (selected) DopamineWhite else DopamineSurface, RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) DopamineWhite else DopamineBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) DopamineBlack else DopamineGrey,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreBreakdownRow(item: ScoreBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
        Text(
            text = if (item.points >= 0) "+${item.points} XP" else "${item.points} XP",
            style = MaterialTheme.typography.labelMedium,
            color = if (item.points >= 0) DopamineWhite else DopamineError,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AchievementBadge(text: String) {
    Box(
        modifier = Modifier
            .background(DopamineCardColor, RoundedCornerShape(10.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
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
private fun DisciplineScoreScreenPreview() {
    DOPAMINE_LOCKTheme {
        DisciplineScoreScreen(
            user = User(disciplineScore = 862),
            onNavigate = {},
            onViewStreakCalendar = {},
            onStartMission = {}
        )
    }
}
