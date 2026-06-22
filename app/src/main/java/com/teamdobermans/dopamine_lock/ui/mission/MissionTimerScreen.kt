package com.teamdobermans.dopamine_lock.ui.mission

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

private enum class MissionTimerState { Running, Paused }

@Composable
fun MissionTimerScreen(
    currentRoute: String = Screen.MissionTimer.route,
    activeMission: Mission?,
    activeSession: FocusSession?,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onEnterMissionMode: () -> Unit,
    onCompleteMission: (sessionId: String, elapsedSeconds: Long) -> Unit,
    onAbandonMission: (sessionId: String, elapsedSeconds: Long) -> Unit
) {
    val missionTitle = activeMission?.title?.ifBlank { null }
        ?: activeSession?.missionName?.ifBlank { null }
        ?: "Mission Timer"
    val missionGoal = activeMission?.goal?.ifBlank { null }
        ?: activeSession?.missionGoal?.ifBlank { null }
        ?: "Stay focused until the timer ends."
    val durationMinutes = activeMission?.durationMinutes ?: activeSession?.durationMinutes ?: 25
    val blockedAppsCount = activeMission?.blockedApps?.size ?: activeSession?.blockedApps?.size ?: 0
    val sessionId = activeSession?.sessionId.orEmpty()
    val totalSeconds = (durationMinutes * 60).coerceAtLeast(60)

    var remainingSeconds by remember(activeMission?.missionId, activeSession?.sessionId) {
        mutableIntStateOf(totalSeconds)
    }
    var timerState by remember { mutableStateOf(MissionTimerState.Running) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    val elapsedSeconds = (totalSeconds - remainingSeconds).coerceAtLeast(0)

    BackHandler {
        if (remainingSeconds < totalSeconds || sessionId.isNotBlank()) {
            showAbandonDialog = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(timerState, sessionId, totalSeconds) {
        while (timerState == MissionTimerState.Running && remainingSeconds > 0) {
            delay(1000L)
            if (timerState == MissionTimerState.Running) remainingSeconds--
        }
        if (remainingSeconds == 0 && sessionId.isNotBlank()) {
            onCompleteMission(sessionId, totalSeconds.toLong())
        }
    }

    if (showAbandonDialog) {
        MissionTimerAbandonDialog(
            onStayFocused = { showAbandonDialog = false },
            onAbandon = {
                showAbandonDialog = false
                if (sessionId.isNotBlank()) {
                    onAbandonMission(sessionId, elapsedSeconds.toLong())
                } else {
                    onNavigateBack()
                }
            }
        )
    }

    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "mission_timer_progress"
    )

    Scaffold(
        containerColor = Color.Black,
        bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showAbandonDialog = true },
                        modifier = Modifier.size(44.dp).background(DopamineCard, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DopamineWhite)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "MISSION TIMER",
                        style = MaterialTheme.typography.labelLarge,
                        color = DopamineGrey,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(44.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Icon(Icons.Filled.Lock, contentDescription = null, tint = DopamineWhite, modifier = Modifier.size(42.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = missionTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = missionGoal, style = MaterialTheme.typography.bodyMedium, color = DopamineGrey)
                Spacer(modifier = Modifier.weight(1f))

                MissionCountdown(
                    progress = animatedProgress,
                    remainingSeconds = remainingSeconds,
                    timerState = timerState
                )

                Spacer(modifier = Modifier.weight(1f))
                MissionMetaRow(durationMinutes = durationMinutes, blockedAppsCount = blockedAppsCount)
                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DopamineButton(
                        text = if (timerState == MissionTimerState.Running) "Pause" else "Resume",
                        onClick = {
                            timerState = if (timerState == MissionTimerState.Running) {
                                MissionTimerState.Paused
                            } else {
                                MissionTimerState.Running
                            }
                        },
                        variant = ButtonVariant.Secondary,
                        leadingIcon = if (timerState == MissionTimerState.Running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    DopamineButton(
                        text = "End Mission",
                        onClick = { showAbandonDialog = true },
                        variant = ButtonVariant.Danger,
                        leadingIcon = Icons.Filled.Stop,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                DopamineButton(
                    text = "ENTER MISSION MODE",
                    onClick = onEnterMissionMode,
                    variant = ButtonVariant.Primary,
                    leadingIcon = Icons.Filled.Lock
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MissionCountdown(
    progress: Float,
    remainingSeconds: Int,
    timerState: MissionTimerState
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    Box(
        modifier = Modifier
            .size(260.dp)
            .drawBehind {
                val strokeWidth = 8.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                drawArc(
                    color = DopamineBorder,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = DopamineWhite,
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
                color = DopamineWhite
            )
            Text(
                text = if (timerState == MissionTimerState.Running) "running" else "paused",
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun MissionMetaRow(durationMinutes: Int, blockedAppsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DopamineSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$durationMinutes min", style = MaterialTheme.typography.bodyMedium, color = DopamineWhite, fontWeight = FontWeight.Bold)
        Text("$blockedAppsCount blocked apps", style = MaterialTheme.typography.bodyMedium, color = DopamineGrey)
    }
}

@Composable
private fun MissionTimerAbandonDialog(
    onStayFocused: () -> Unit,
    onAbandon: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStayFocused,
        containerColor = DopamineCard,
        titleContentColor = DopamineWhite,
        textContentColor = DopamineGrey,
        title = { Text("Abandon Mission?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Leaving now will mark this mission as abandoned.")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DopamineError.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, DopamineError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("-15 Discipline XP", color = DopamineError, fontWeight = FontWeight.Bold)
                        Text("Streak may be affected", color = DopamineError.copy(alpha = 0.8f))
                        Text("Session will be saved as failed", color = DopamineError.copy(alpha = 0.8f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAbandon) {
                Text("ABANDON MISSION", color = DopamineError, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onStayFocused) {
                Text("STAY FOCUSED", color = DopamineWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}
