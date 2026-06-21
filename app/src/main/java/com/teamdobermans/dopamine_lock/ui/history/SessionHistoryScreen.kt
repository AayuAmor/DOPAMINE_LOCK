package com.teamdobermans.dopamine_lock.ui.history

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDim
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

private enum class SessionStatus { Completed, Failed }

private data class HistorySession(
    val id: String,
    val dateLabel: String,
    val title: String,
    val status: SessionStatus,
    val duration: String,
    val timeRange: String,
    val appsBlocked: Int,
    val goal: String,
    val disciplineXp: Int,
    val completion: Float
)

private val filterOptions = listOf("All", "Completed", "Failed", "This Week", "This Month")

private val mockSessions = listOf(
    HistorySession(
        id = "deep-work-today",
        dateLabel = "TODAY",
        title = "Deep Work Sprint",
        status = SessionStatus.Completed,
        duration = "90 min",
        timeRange = "09:00 - 10:30",
        appsBlocked = 3,
        goal = "Finish DSA Notes",
        disciplineXp = 25,
        completion = 1f
    ),
    HistorySession(
        id = "react-learning",
        dateLabel = "TODAY",
        title = "React Learning",
        status = SessionStatus.Failed,
        duration = "25 min",
        timeRange = "13:10 - 13:35",
        appsBlocked = 4,
        goal = "Complete component cleanup",
        disciplineXp = -10,
        completion = 0.42f
    ),
    HistorySession(
        id = "reading-yesterday",
        dateLabel = "YESTERDAY",
        title = "Reading Session",
        status = SessionStatus.Completed,
        duration = "45 min",
        timeRange = "20:00 - 20:45",
        appsBlocked = 2,
        goal = "Read product design chapter",
        disciplineXp = 25,
        completion = 1f
    ),
    HistorySession(
        id = "coding-june-18",
        dateLabel = "JUNE 18",
        title = "Coding Mission",
        status = SessionStatus.Completed,
        duration = "120 min",
        timeRange = "07:30 - 09:30",
        appsBlocked = 5,
        goal = "Implement mission planning UI",
        disciplineXp = 25,
        completion = 1f
    )
)

@Composable
fun SessionHistoryScreen(
    currentRoute: String = Screen.Dashboard.route,
    onNavigate: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val visibleSessions = remember(selectedFilter, searchQuery) {
        filterSessions(
            sessions = mockSessions,
            selectedFilter = selectedFilter,
            query = searchQuery
        )
    }

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
            item { SessionHistoryHeader() }
            item { SessionHistorySummary() }
            item {
                SessionHistoryFilters(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            item {
                SessionHistorySearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            if (visibleSessions.isEmpty()) {
                item { SessionHistoryEmptyState() }
            } else {
                val groups = visibleSessions.groupBy { it.dateLabel }
                groups.forEach { (dateLabel, sessions) ->
                    item {
                        SessionDateGroup(
                            dateLabel = dateLabel,
                            sessions = sessions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryHeader() {
    Column {
        Text(
            text = "SESSION HISTORY",
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your Focus Archive",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Review completed missions and discipline patterns.",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun SessionHistorySummary() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(value = "29.2h", label = "Total Focus", modifier = Modifier.weight(1f))
        SummaryCard(value = "38", label = "Sessions", modifier = Modifier.weight(1f))
        SummaryCard(value = "87%", label = "Success Rate", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SessionHistoryFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filterOptions) { filter ->
            FilterChip(
                text = filter,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun SessionHistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    DopamineTextField(
        value = query,
        onValueChange = onQueryChange,
        label = "Search sessions",
        placeholder = "Search sessions",
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        imeAction = ImeAction.Search
    )
}

@Composable
private fun SessionDateGroup(
    dateLabel: String,
    sessions: List<HistorySession>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        sessions.forEach { session ->
            SessionHistoryCard(session = session)
        }
    }
}

@Composable
private fun SessionHistoryCard(session: HistorySession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineCard, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.duration} • ${session.timeRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }
            SessionStatusBadge(status = session.status)
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "${session.appsBlocked} apps blocked",
            style = MaterialTheme.typography.bodySmall,
            color = DopamineGrey
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Goal: ${session.goal}",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineWhite
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SessionProgressIndicator(
                progress = session.completion,
                failed = session.status == SessionStatus.Failed,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = if (session.disciplineXp >= 0) "+${session.disciplineXp} XP" else "${session.disciplineXp} XP",
                style = MaterialTheme.typography.labelMedium,
                color = if (session.disciplineXp >= 0) DopamineWhite else DopamineError,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SessionHistoryEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(DopamineCard, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No sessions found",
                style = MaterialTheme.typography.titleMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Start a mission to build your history.",
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey
            )
        }
    }
}

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DopamineCard, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) DopamineWhite else DopamineCard,
                shape = RoundedCornerShape(99.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) DopamineWhite else DopamineBorder,
                shape = RoundedCornerShape(99.dp)
            )
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
private fun SessionStatusBadge(status: SessionStatus) {
    val failed = status == SessionStatus.Failed
    Box(
        modifier = Modifier
            .background(
                color = if (failed) DopamineError.copy(alpha = 0.14f) else DopamineSurface,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = if (failed) DopamineError.copy(alpha = 0.55f) else DopamineBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (failed) DopamineError else DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SessionProgressIndicator(
    progress: Float,
    failed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(7.dp)
            .background(DopamineSurface, RoundedCornerShape(99.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(7.dp)
                .background(
                    color = if (failed) DopamineError else DopamineWhite,
                    shape = RoundedCornerShape(99.dp)
                )
        )
    }
}

private fun filterSessions(
    sessions: List<HistorySession>,
    selectedFilter: String,
    query: String
): List<HistorySession> {
    val byFilter = when (selectedFilter) {
        "Completed" -> sessions.filter { it.status == SessionStatus.Completed }
        "Failed" -> sessions.filter { it.status == SessionStatus.Failed }
        "This Week" -> sessions
        "This Month" -> sessions
        else -> sessions
    }

    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return byFilter

    return byFilter.filter { session ->
        session.title.contains(trimmedQuery, ignoreCase = true) ||
            session.goal.contains(trimmedQuery, ignoreCase = true) ||
            session.dateLabel.contains(trimmedQuery, ignoreCase = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SessionHistoryScreenPreview() {
    DOPAMINE_LOCKTheme {
        SessionHistoryScreen(onNavigate = {})
    }
}
