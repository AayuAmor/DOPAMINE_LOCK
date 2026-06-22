package com.teamdobermans.dopamine_lock.ui.focus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class TimerState { Idle, Running, Paused }

@Composable
fun FocusTimerScreen(
    currentRoute: String = Screen.Focus.route,
    activeSession: FocusSession?,
    activeMission: Mission? = null,
    onNavigate: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToMission: () -> Unit,
    onStartFocusSession: (durationMinutes: Int) -> Unit,
    onCompleteSession: (sessionId: String, elapsedSeconds: Long) -> Unit,
    onAbandonSession: (sessionId: String, elapsedSeconds: Long) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (activeSession == null) {
        Scaffold(
            containerColor = Color.Black,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(horizontal = 24.dp)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 32.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "FOCUS",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineGrey,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Normal focus sessions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Pomodoro, break length, custom focus sessions, and focus preferences are coming soon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineGrey
                )
                Spacer(modifier = Modifier.height(28.dp))
                DopamineButton(
                    text = "START 25 MIN FOCUS",
                    onClick = { onStartFocusSession(25) },
                    leadingIcon = Icons.Filled.PlayArrow
                )
                Spacer(modifier = Modifier.height(12.dp))
                DopamineButton(
                    text = "FOCUS PREFERENCES",
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Focus preferences coming soon")
                        }
                    },
                    variant = ButtonVariant.Secondary
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        return
    }

    val totalSeconds = activeSession.durationMinutes * 60
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var timerState by remember { mutableStateOf(TimerState.Idle) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    val elapsedSeconds = (totalSeconds - remainingSeconds).coerceAtLeast(0)

    LaunchedEffect(activeSession.sessionId, totalSeconds) {
        remainingSeconds = totalSeconds
        timerState = TimerState.Idle
    }

    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "timer_progress"
    )

    LaunchedEffect(timerState) {
        if (timerState == TimerState.Running) {
            while (remainingSeconds > 0 && timerState == TimerState.Running) {
                delay(1000L)
                if (timerState == TimerState.Running) remainingSeconds--
            }
            if (remainingSeconds == 0) {
                timerState = TimerState.Idle
                activeSession.sessionId.takeIf { it.isNotBlank() }?.let {
                    onCompleteSession(it, totalSeconds.toLong())
                }
            }
        }
    }

    // Back press while running → confirmation dialog; otherwise normal back
    BackHandler(enabled = timerState == TimerState.Running) {
        showAbandonDialog = true
    }

    if (showAbandonDialog) {
        AbandonConfirmationDialog(
            onStayFocused = { showAbandonDialog = false },
            onAbandon = {
                showAbandonDialog = false
                timerState = TimerState.Idle
                activeSession.sessionId.takeIf { it.isNotBlank() }?.let {
                    onAbandonSession(it, elapsedSeconds.toLong())
                }
            }
        )
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Scaffold(
        containerColor = Color.Black,
        bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (timerState == TimerState.Running) showAbandonDialog = true
                        else onNavigateBack()
                    },
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
                    text = "FOCUS SESSION",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineGrey,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .background(color = DopamineSurface, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (timerState) {
                        TimerState.Idle -> "READY"
                        TimerState.Running -> "IN PROGRESS"
                        TimerState.Paused -> "PAUSED"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (timerState) {
                        TimerState.Idle -> DopamineGrey
                        TimerState.Running -> DopamineWhite
                        TimerState.Paused -> DopamineGrey
                    },
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CircularTimerDisplay(
                progress = animatedProgress,
                minutes = minutes,
                seconds = seconds,
                timerState = timerState
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = (activeSession.missionType.ifBlank { "DEEP WORK" }).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = DopamineGrey,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${activeSession.durationMinutes}-minute focus block",
                style = MaterialTheme.typography.bodySmall,
                color = DopamineGrey.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (timerState) {
                    TimerState.Idle -> {
                        DopamineButton(
                            text = "Start Session",
                            onClick = { timerState = TimerState.Running },
                            variant = ButtonVariant.Primary,
                            leadingIcon = Icons.Filled.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TimerState.Running -> {
                        DopamineButton(
                            text = "Pause",
                            onClick = { timerState = TimerState.Paused },
                            variant = ButtonVariant.Secondary,
                            leadingIcon = Icons.Filled.Pause,
                            modifier = Modifier.weight(1f)
                        )
                        DopamineButton(
                            text = "End",
                            onClick = { showAbandonDialog = true },
                            variant = ButtonVariant.Danger,
                            leadingIcon = Icons.Filled.Stop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TimerState.Paused -> {
                        DopamineButton(
                            text = "Resume",
                            onClick = { timerState = TimerState.Running },
                            variant = ButtonVariant.Primary,
                            leadingIcon = Icons.Filled.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                        DopamineButton(
                            text = "End",
                            onClick = { showAbandonDialog = true },
                            variant = ButtonVariant.Danger,
                            leadingIcon = Icons.Filled.Stop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enter Mission Mode only available when an active mission exists
            if (activeMission != null) {
                DopamineButton(
                    text = "Enter Mission Mode",
                    onClick = onNavigateToMission,
                    variant = ButtonVariant.Secondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }
}

@Composable
private fun AbandonConfirmationDialog(
    onStayFocused: () -> Unit,
    onAbandon: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStayFocused,
        containerColor = DopamineCard,
        titleContentColor = DopamineWhite,
        textContentColor = DopamineGrey,
        title = {
            Text(
                text = "Abandon Mission?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Leaving now will mark this mission as abandoned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineGrey
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DopamineError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, DopamineError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("−15 Discipline XP", style = MaterialTheme.typography.labelSmall, color = DopamineError, fontWeight = FontWeight.Bold)
                        Text("Streak may be affected", style = MaterialTheme.typography.labelSmall, color = DopamineError.copy(alpha = 0.8f))
                        Text("Session will be saved as failed", style = MaterialTheme.typography.labelSmall, color = DopamineError.copy(alpha = 0.8f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAbandon) {
                Text("Abandon Mission", color = DopamineError, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onStayFocused) {
                Text("Stay Focused", color = DopamineWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun CircularTimerDisplay(
    progress: Float,
    minutes: Int,
    seconds: Int,
    timerState: TimerState
) {
    val trackColor = DopamineBorder
    val progressColor = DopamineWhite

    Box(
        modifier = Modifier
            .size(260.dp)
            .drawBehind {
                val strokeWidth = 8.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(
                    x = (size.width - diameter) / 2,
                    y = (size.height - diameter) / 2
                )
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = DopamineWhite,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (timerState) {
                    TimerState.Idle -> "ready"
                    TimerState.Running -> "running"
                    TimerState.Paused -> "paused"
                },
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 2.sp
            )
        }
    }
}
