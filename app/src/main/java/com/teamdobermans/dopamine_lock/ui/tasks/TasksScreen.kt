package com.teamdobermans.dopamine_lock.ui.tasks

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard as DLCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

private enum class TaskFilter { All, Pending, Completed }

private enum class Priority { High, Medium, Low }

private data class Task(
    val id: Int,
    val title: String,
    val category: String,
    val dueDate: String,
    val priority: Priority,
    val completed: Boolean
)

private val sampleTasks = listOf(
    Task(1, "Design system audit", "Design", "Today", Priority.High, false),
    Task(2, "Write quarterly report", "Work", "Tomorrow", Priority.High, false),
    Task(3, "Code review for PR #47", "Development", "Today", Priority.Medium, true),
    Task(4, "Update dependencies", "Development", "Jun 25", Priority.Low, false),
    Task(5, "Morning meditation routine", "Health", "Daily", Priority.Medium, true),
    Task(6, "Read chapter 5 — Deep Work", "Learning", "Jun 23", Priority.Low, true),
    Task(7, "Architecture proposal doc", "Development", "Jun 28", Priority.High, false),
    Task(8, "Weekly team sync prep", "Work", "Jun 22", Priority.Medium, false)
)

@Composable
fun TasksScreen(
    currentRoute: String = Screen.Tasks.route,
    onNavigate: (String) -> Unit,
    onAddTask: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(TaskFilter.All) }

    val filteredTasks = when (selectedFilter) {
        TaskFilter.All -> sampleTasks
        TaskFilter.Pending -> sampleTasks.filter { !it.completed }
        TaskFilter.Completed -> sampleTasks.filter { it.completed }
    }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = DopamineWhite,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
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
                start = 20.dp, end = 20.dp, top = 0.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MY TASKS",
                            style = MaterialTheme.typography.labelLarge,
                            color = DopamineGrey,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${filteredTasks.size} items",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DopamineWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(color = DLCard, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, DopamineBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${sampleTasks.count { !it.completed }} pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = DopamineWhite
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TaskFilter.values().size) { index ->
                        val filter = TaskFilter.values()[index]
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.name.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 1.sp
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = DopamineGrey,
                                selectedContainerColor = DopamineWhite,
                                selectedLabelColor = Color.Black
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = DopamineBorder,
                                selectedBorderColor = DopamineWhite,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DopamineGrey
                        )
                    }
                }
            } else {
                items(filteredTasks.size) { index ->
                    TaskCard(task = filteredTasks[index])
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task) {
    DopamineCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (task.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.completed) DopamineWhite else DopamineGrey,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (task.completed) DopamineGrey else DopamineWhite,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = task.priority)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(label = task.category)
                    CategoryChip(label = task.dueDate)
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    val color = when (priority) {
        Priority.High -> DopamineWhite
        Priority.Medium -> DopamineGrey
        Priority.Low -> DopamineGrey.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun CategoryChip(label: String) {
    Box(
        modifier = Modifier
            .background(color = DopamineSurface, shape = RoundedCornerShape(4.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            fontSize = 11.sp
        )
    }
}
