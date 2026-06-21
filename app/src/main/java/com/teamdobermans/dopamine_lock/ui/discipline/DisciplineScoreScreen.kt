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
import com.teamdobermans.dopamine_lock.domain.model.User
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

private data class ScoreBreakdown(val label: String, val points: Int)

private val rankLadder = listOf("D", "C", "B", "A", "S", "S+")
private val trendValues = listOf(720, 745, 760, 790, 812, 840, 862)
private val achievements = listOf("7-Day Chain", "Deep Work Beast", "Distraction Resister", "Mission Finisher")
private val scoreBreakdown = listOf(
    ScoreBreakdown("Mission Completion", 420),
    ScoreBreakdown("Streak Strength", 210),
    ScoreBreakdown("Deep Work Hours", 160),
    ScoreBreakdown("Distractions Resisted", 90),
    ScoreBreakdown("Failed Missions", -18)
)

@Composable
fun DisciplineScoreScreen(
    currentRoute: String = Screen.Analytics.route,
    user: User? = null,
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
                .background(DopamineBlack)
                .systemBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { DisciplineScoreHeader() }
            item { DisciplineHeroCard(score = user?.disciplineScore ?: 0) }
            item { RankProgressSection(score = user?.disciplineScore ?: 0) }
            item { ScoreBreakdownCard() }
            item { DisciplineTrendCard() }
            item { AchievementBadgesSection() }
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
private fun DisciplineHeroCard(score: Int) {
    val rank = rankForScore(score)
    val nextRankTarget = nextRankTarget(score)
    val progress = (score.toFloat() / nextRankTarget.toFloat()).coerceIn(0f, 1f)

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
                    text = "$rank RANK",
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
private fun RankProgressSection(score: Int) {
    val rank = rankForScore(score)
    val nextRank = nextRankForScore(score)
    val needed = (nextRankTarget(score) - score).coerceAtLeast(0)

    DopamineCard {
        SectionLabel("RANK SYSTEM")
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RankMetric("CURRENT", rank)
            RankMetric("NEXT", nextRank)
            RankMetric("NEEDED", "$needed XP")
        }
        Spacer(modifier = Modifier.height(18.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rankLadder) { rank ->
                RankBadge(rank = rank, selected = rank == rankForScore(score))
            }
        }
    }
}

@Composable
private fun ScoreBreakdownCard() {
    DopamineCard {
        SectionLabel("SCORE BREAKDOWN")
        Spacer(modifier = Modifier.height(12.dp))
        scoreBreakdown.forEachIndexed { index, item ->
            ScoreBreakdownRow(item)
            if (index < scoreBreakdown.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DisciplineTrendCard() {
    DopamineCard {
        SectionLabel("7-DAY SCORE TREND")
        Spacer(modifier = Modifier.height(16.dp))
        TrendLineChart(values = trendValues)
    }
}

@Composable
private fun AchievementBadgesSection() {
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

private fun rankForScore(score: Int): String {
    return when {
        score >= 1200 -> "S+"
        score >= 1000 -> "S"
        score >= 750 -> "A"
        score >= 500 -> "B"
        score >= 250 -> "C"
        else -> "D"
    }
}

private fun nextRankForScore(score: Int): String {
    return when {
        score >= 1200 -> "MAX"
        score >= 1000 -> "S+"
        score >= 750 -> "S"
        score >= 500 -> "A"
        score >= 250 -> "B"
        else -> "C"
    }
}

private fun nextRankTarget(score: Int): Int {
    return when {
        score >= 1200 -> 1200
        score >= 1000 -> 1200
        score >= 750 -> 1000
        score >= 500 -> 750
        score >= 250 -> 500
        else -> 250
    }
}

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
