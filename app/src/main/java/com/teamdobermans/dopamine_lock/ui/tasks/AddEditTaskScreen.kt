package com.teamdobermans.dopamine_lock.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.TaskPriority
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSubtle
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val categories = listOf("Work", "Development", "Design", "Health", "Learning", "Personal")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onNavigateBack: () -> Unit,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onCreateTask: (title: String, description: String, category: String, dueDate: String, priority: TaskPriority) -> Unit = { _, _, _, _, _ -> },
    onClearMessages: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scrollState = rememberScrollState()

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onClearMessages()
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        val pickerColors = DatePickerDefaults.colors(
            containerColor = DopamineCard,
            titleContentColor = DopamineGrey,
            headlineContentColor = DopamineWhite,
            weekdayContentColor = DopamineGrey,
            subheadContentColor = DopamineGrey,
            navigationContentColor = DopamineWhite,
            yearContentColor = DopamineGrey,
            currentYearContentColor = DopamineWhite,
            selectedYearContentColor = DopamineBlack,
            selectedYearContainerColor = DopamineWhite,
            dayContentColor = DopamineGrey,
            selectedDayContentColor = DopamineBlack,
            selectedDayContainerColor = DopamineWhite,
            todayContentColor = DopamineWhite,
            todayDateBorderColor = DopamineWhite,
            dayInSelectionRangeContentColor = DopamineGrey,
            dayInSelectionRangeContainerColor = DopamineCard,
            dividerColor = DopamineBorder
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = DopamineWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = DopamineGrey)
                }
            },
            colors = pickerColors
        ) {
            DatePicker(state = datePickerState, colors = pickerColors)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(color = DopamineCard, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DopamineWhite
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "NEW TASK",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineGrey,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            DopamineTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task Title",
                placeholder = "What needs to be done?",
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Title, contentDescription = null, tint = DopamineGrey)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DopamineTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                placeholder = "Add details or notes...",
                singleLine = false,
                maxLines = 4,
                imeAction = ImeAction.Default
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(3).forEach { category ->
                    SelectableChip(
                        label = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) "" else category },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.drop(3).forEach { category ->
                    SelectableChip(
                        label = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) "" else category },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DueDateField(
                value = dueDate,
                onClick = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PRIORITY",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { priority ->
                    SelectableChip(
                        label = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineError,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            DopamineButton(
                text = if (isSaving) "Saving..." else "Create Task",
                onClick = {
                    onCreateTask(title, description, selectedCategory, dueDate, selectedPriority)
                },
                variant = ButtonVariant.Primary,
                enabled = !isSaving,
                isLoading = isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            DopamineButton(
                text = "Cancel",
                onClick = onNavigateBack,
                variant = ButtonVariant.Secondary,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DueDateField(value: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            label = {
                Text(
                    text = "DUE DATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey
                )
            },
            placeholder = {
                Text(text = "Select a date", color = DopamineSubtle)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = DopamineGrey,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DopamineWhite,
                unfocusedTextColor = DopamineWhite,
                focusedContainerColor = DopamineCard,
                unfocusedContainerColor = DopamineCard,
                focusedBorderColor = DopamineWhite,
                unfocusedBorderColor = DopamineBorder,
                focusedLabelColor = DopamineWhite,
                unfocusedLabelColor = DopamineGrey,
                focusedLeadingIconColor = DopamineGrey,
                unfocusedLeadingIconColor = DopamineGrey,
                cursorColor = DopamineWhite
            )
        )
        // Transparent overlay intercepts taps without blocking the text field's visual state
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) DopamineWhite else DopamineCard,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) DopamineWhite else DopamineBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.Black else DopamineGrey,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
