package com.teamdobermans.dopamine_lock.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.domain.model.User
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDivider
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun SettingsScreen(
    currentRoute: String = Screen.Settings.route,
    user: User? = null,
    onNavigate: (String) -> Unit,
    onNavigateToBlockedApps: () -> Unit = {},
    onLogout: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var missionModeReminder by remember { mutableStateOf(false) }
    var dailyGoalReminder by remember { mutableStateOf(true) }
    var strictModeEnabled by remember { mutableStateOf(false) }

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
                start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = DopamineGrey,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item { ProfileCard(user = user) }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                SettingsSectionLabel("ACCOUNT")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroup {
                    SettingsNavigationRow(
                        icon = Icons.Filled.Person,
                        label = "Edit Profile",
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Filled.Key,
                        label = "Change Password",
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Filled.Lock,
                        label = "Privacy & Security",
                        onClick = {}
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionLabel("FOCUS")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroup {
                    SettingsToggleRow(
                        icon = Icons.Filled.Timer,
                        label = "Strict Focus Mode",
                        subtitle = "Prevent early session exit",
                        checked = strictModeEnabled,
                        onCheckedChange = { strictModeEnabled = it }
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Filled.Timer,
                        label = "Default Session Length",
                        trailing = "25 min",
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Filled.Timer,
                        label = "Break Duration",
                        trailing = "5 min",
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Filled.Lock,
                        label = "Blocked Apps",
                        trailing = "Configure",
                        onClick = onNavigateToBlockedApps
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionLabel("NOTIFICATIONS")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroup {
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        label = "Push Notifications",
                        subtitle = "Enable all app notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        label = "Daily Goal Reminder",
                        subtitle = "Remind me to hit my daily goal",
                        checked = dailyGoalReminder,
                        onCheckedChange = { dailyGoalReminder = it }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        label = "Mission Mode Alerts",
                        subtitle = "Notify before entering mission",
                        checked = missionModeReminder,
                        onCheckedChange = { missionModeReminder = it }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionLabel("APPEARANCE")
                Spacer(modifier = Modifier.height(8.dp))
                SettingsGroup {
                    SettingsNavigationRow(
                        icon = Icons.Filled.Palette,
                        label = "Theme",
                        trailing = "Dark",
                        onClick = {}
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                DopamineButton(
                    text = "Log Out",
                    onClick = onLogout,
                    variant = ButtonVariant.Danger
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DOPAMINE LOCK v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(user: User?) {
    val displayName = user?.name?.takeIf { it.isNotBlank() } ?: "Focus Warrior"
    val email = user?.email?.takeIf { it.isNotBlank() } ?: "No email available"
    val streak = user?.currentStreak ?: 0
    val disciplineScore = user?.disciplineScore ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color = DopamineSurface, shape = CircleShape)
                .border(2.dp, DopamineBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "F",
                style = MaterialTheme.typography.headlineSmall,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = DopamineGrey
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(color = DopamineSurface, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, DopamineBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$streak-DAY STREAK • $disciplineScore XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineWhite,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = DopamineGrey,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = DopamineGrey,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCard, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = DopamineDivider,
        thickness = 1.dp
    )
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = DopamineSurface, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DopamineGrey,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineWhite,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = DopamineGrey
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = DopamineGrey,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = DopamineSurface, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DopamineGrey,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineWhite
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DopamineGrey
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = DopamineWhite,
                uncheckedThumbColor = DopamineGrey,
                uncheckedTrackColor = DopamineSurface,
                uncheckedBorderColor = DopamineBorder
            )
        )
    }
}
