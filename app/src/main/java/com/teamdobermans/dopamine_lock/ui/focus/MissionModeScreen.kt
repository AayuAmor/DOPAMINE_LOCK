package com.teamdobermans.dopamine_lock.ui.focus

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import kotlinx.coroutines.delay

private data class MissionRule(val icon: ImageVector, val text: String)

private val missionRules = listOf(
    MissionRule(Icons.Filled.PhoneAndroid, "No social media or distracting apps"),
    MissionRule(Icons.Filled.Notifications, "Notifications are silenced"),
    MissionRule(Icons.Filled.Block, "Blocked apps cannot be accessed"),
    MissionRule(Icons.Filled.WifiOff, "Internet restricted to work tools only"),
    MissionRule(Icons.Filled.Lock, "Exiting mission mode is permanent")
)

@Composable
fun MissionModeScreen(
    onAbandonMission: () -> Unit
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(width = 2.dp, color = DopamineBorder, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = DopamineWhite,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(color = DopamineWhite, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "MISSION ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MISSION MODE",
                style = MaterialTheme.typography.headlineMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Maximum focus. Zero distractions.",
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = DopamineCard, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, DopamineBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TIME ELAPSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineGrey,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = DopamineWhite,
                        letterSpacing = (-1).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MISSION RULES",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(missionRules.size) { index ->
            val rule = missionRules[index]
            MissionRuleItem(rule = rule)
            if (index < missionRules.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = DopamineError.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, DopamineError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "WARNING",
                        style = MaterialTheme.typography.labelSmall,
                        color = DopamineError,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Abandoning this mission will end your current session and reset your streak. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DopamineError.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DopamineButton(
                text = "Abandon Mission",
                onClick = onAbandonMission,
                variant = ButtonVariant.Danger
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun MissionRuleItem(rule: MissionRule) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineSurface, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = DopamineCard, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = rule.icon,
                contentDescription = null,
                tint = DopamineGrey,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = rule.text,
            style = MaterialTheme.typography.bodySmall,
            color = DopamineGrey,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = DopamineWhite,
            modifier = Modifier.size(16.dp)
        )
    }
}
