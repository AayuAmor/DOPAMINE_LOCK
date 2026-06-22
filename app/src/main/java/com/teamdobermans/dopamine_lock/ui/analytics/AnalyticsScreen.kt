package com.teamdobermans.dopamine_lock.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.DisciplineRank
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.DashboardStatCard
import com.teamdobermans.dopamine_lock.ui.components.SectionHeader
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

private val weeklyData = listOf(
    Pair("Mon", 2.5f),
    Pair("Tue", 4.0f),
    Pair("Wed", 3.2f),
    Pair("Thu", 5.5f),
    Pair("Fri", 6.0f),
    Pair("Sat", 3.8f),
    Pair("Sun", 4.2f)
)

private data class FocusCategory(val name: String, val hours: Float, val percentage: Int)

@Composable
fun AnalyticsScreen(
    currentRoute: String = Screen.Analytics.route,
    sessions: List<FocusSession> = emptyList(),
    totalFocusHours: Double = 0.0,
    totalSessions: Int = sessions.size,
    completedSessions: Int = 0,
    successRate: Int = 0,
    weeklyFocusHours: List<Float> = List(7) { 0f },
    monthlyFocusHours: List<Float> = emptyList(),
    focusDistribution: Map<String, Double> = emptyMap(),
    bestFocusDay: String = "",
    bestFocusHours: Double = 0.0,
    disciplineScore: Int = 0,
    disciplineRank: DisciplineRank = DisciplineRank.D,
    averageDailyDisciplineGain: Int = 0,
    mostValuableHabit: String = "No discipline events yet",
    scoreGrowthTrend: List<Float> = emptyList(),
    goalsCreated: Int = 0,
    goalsCompleted: Int = 0,
    goalCompletionRate: Int = 0,
    averageGoalCompletionTimeHours: Int = 0,
    mostSuccessfulGoalType: String = "NONE",
    onNavigate: (String) -> Unit,
    onOpenStreakCalendar: () -> Unit = {},
    onOpenDisciplineScore: () -> Unit = {}
) {
    val categories = remember(focusDistribution) { focusDistribution.toFocusCategories() }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 0.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "ANALYTICS",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineGrey,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        value = formatHours(totalFocusHours),
                        label = "Total Hrs",
                        unit = "h",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = totalSessions.toString(),
                        label = "Sessions",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = successRate.toString(),
                        label = "Success %",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenStreakCalendar
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                DisciplineScoreEntryCard(
                    score = disciplineScore,
                    rank = disciplineRank,
                    averageDailyGain = averageDailyDisciplineGain,
                    mostValuableHabit = mostValuableHabit,
                    onClick = onOpenDisciplineScore
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Goal Performance")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        value = goalsCreated.toString(),
                        label = "Goals",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = goalsCompleted.toString(),
                        label = "Done",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        value = goalCompletionRate.toString(),
                        label = "Goal %",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Avg completion: ${averageGoalCompletionTimeHours}h · Best type: $mostSuccessfulGoalType",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Weekly Focus Hours")
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyBarChart(data = weeklyData.mapIndexed { index, pair ->
                    pair.first to weeklyFocusHours.getOrElse(index) { 0f }
                })
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Monthly Trend")
                Spacer(modifier = Modifier.height(16.dp))
                MonthlyLineChart(data = monthlyFocusHours.takeIf { it.size > 1 } ?: List(2) { 0f })
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Focus Distribution")
                Spacer(modifier = Modifier.height(16.dp))
                categories.forEachIndexed { index, category ->
                    FocusCategoryRow(category = category)
                    if (index < categories.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Best Performance Day")
                Spacer(modifier = Modifier.height(12.dp))
                BestDayCard(
                    day = bestFocusDay.ifBlank { "No focus day yet" },
                    focusHours = bestFocusHours,
                    completedSessions = completedSessions
                )
            }
        }
    }
}

private fun formatHours(hours: Double): String {
    return if (hours % 1.0 == 0.0) hours.toInt().toString() else "%.1f".format(hours)
}

private fun Map<String, Double>.toFocusCategories(): List<FocusCategory> {
    val total = values.sum().coerceAtLeast(0.0)
    if (total <= 0.0) return listOf(FocusCategory("No focus data", 0f, 0))

    return entries
        .sortedByDescending { it.value }
        .map { (name, hours) ->
            FocusCategory(
                name = name,
                hours = hours.toFloat(),
                percentage = ((hours / total) * 100).toInt()
            )
        }
}

@Composable
private fun WeeklyBarChart(data: List<Pair<String, Float>>) {
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (day, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "%.1fh".format(value),
                            style = MaterialTheme.typography.labelSmall,
                            color = DopamineGrey,
                            fontSize = 9.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val barHeightFraction = value / maxValue
                        val isToday = day == "Fri"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .fillMaxWidth()
                                .height((barHeightFraction * 100).dp)
                                .background(
                                    color = if (isToday) DopamineWhite else DopamineGrey.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) DopamineWhite else DopamineGrey,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyLineChart(data: List<Float>) {
    val maxValue = data.maxOf { it }.coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .drawWithCache {
                val width = size.width
                val height = size.height
                val padding = 24.dp.toPx()

                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                val stepX = chartWidth / (data.size - 1)

                val path = Path()
                data.forEachIndexed { index, value ->
                    val x = padding + index * stepX
                    val y = padding + chartHeight * (1f - value / maxValue)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                val fillPath = Path().apply {
                    addPath(path)
                    val lastX = padding + (data.size - 1) * stepX
                    lineTo(lastX, height)
                    lineTo(padding, height)
                    close()
                }

                onDrawBehind {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
    )
}

@Composable
private fun FocusCategoryRow(category: FocusCategory) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category.hours.toInt()}h",
                        style = MaterialTheme.typography.bodySmall,
                        color = DopamineGrey
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${category.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(color = DopamineBorder, shape = RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = category.percentage / 100f)
                        .height(3.dp)
                        .background(color = DopamineWhite, shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun BestDayCard(
    day: String,
    focusHours: Double,
    completedSessions: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineWhite,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatHours(focusHours)} hours of deep focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedSessions sessions completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "PEAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${formatHours(focusHours)}h",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DopamineWhite
                )
            }
        }
    }
}

@Composable
private fun DisciplineScoreEntryCard(
    score: Int,
    rank: DisciplineRank,
    averageDailyGain: Int,
    mostValuableHabit: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DISCIPLINE SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${rank.name} RANK · $score XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+$averageDailyGain avg/day · $mostValuableHabit",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }
            androidx.compose.material3.TextButton(onClick = onClick) {
                Text(
                    text = "VIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
