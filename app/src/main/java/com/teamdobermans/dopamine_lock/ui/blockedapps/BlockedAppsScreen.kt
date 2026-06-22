package com.teamdobermans.dopamine_lock.ui.blockedapps

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.model.AppBlockItem
import com.teamdobermans.dopamine_lock.enforcement.InstalledAppsProvider
import com.teamdobermans.dopamine_lock.navigation.Screen
import com.teamdobermans.dopamine_lock.repo.BlockedAppsRepositoryImpl
import com.teamdobermans.dopamine_lock.ui.components.BottomNavigationBar
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineCard
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBlack
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard as DopamineCardColor
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDim
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSurface
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import com.teamdobermans.dopamine_lock.viewModel.BlockedAppsUiState
import com.teamdobermans.dopamine_lock.viewModel.BlockedAppsViewModel
import kotlinx.coroutines.launch

@Composable
fun BlockedAppsScreen(
    currentRoute: String = Screen.BlockedApps.route,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel = remember {
        BlockedAppsViewModel(
            repository = BlockedAppsRepositoryImpl(context),
            installedAppsProvider = InstalledAppsProvider(context)
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    BlockedAppsContent(
        uiState = viewModel.uiState,
        currentRoute = currentRoute,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onNavigateBack = onNavigateBack,
        onSearchChange = viewModel::searchApps,
        onToggleApp = viewModel::toggleApp,
        onSave = {
            viewModel.saveBlockedApps()
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Blocked apps updated",
                    duration = SnackbarDuration.Short
                )
            }
        }
    )
}

@Composable
private fun BlockedAppsContent(
    uiState: BlockedAppsUiState,
    currentRoute: String,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        containerColor = DopamineBlack,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BlockedAppsHeader(onNavigateBack = onNavigateBack)
            }

            item {
                BlockedAppsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchChange
                )
            }

            item {
                BlockedAppsSummaryCard(selectedCount = uiState.selectedCount)
            }

            if (uiState.filteredApps.isEmpty()) {
                item { BlockedAppsEmptyState() }
            } else {
                items(uiState.filteredApps, key = { it.id }) { app ->
                    BlockedAppRow(
                        app = app,
                        onToggle = { onToggleApp(app.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                DopamineButton(
                    text = "SAVE BLOCK LIST",
                    onClick = onSave
                )
            }
        }
    }
}

@Composable
private fun BlockedAppsHeader(
    onNavigateBack: () -> Unit
) {
    Column {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .background(color = DopamineCardColor, shape = CircleShape)
                .border(1.dp, DopamineBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DopamineWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "BLOCKED APPS",
            style = MaterialTheme.typography.headlineMedium,
            color = DopamineWhite,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Choose apps to lock during focus sessions",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Composable
private fun BlockedAppsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    DopamineTextField(
        value = query,
        onValueChange = onQueryChange,
        label = "Search apps",
        placeholder = "Search apps",
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        imeAction = ImeAction.Search,
        keyboardActions = KeyboardActions.Default
    )
}

@Composable
private fun BlockedAppsSummaryCard(
    selectedCount: Int
) {
    DopamineCard {
        Text(
            text = "LOCKED DURING MISSION",
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedCount.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (selectedCount == 1) "APP SELECTED" else "APPS SELECTED",
                style = MaterialTheme.typography.labelMedium,
                color = DopamineDim,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun BlockedAppRow(
    app: AppBlockItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = DopamineCardColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color = DopamineSurface, shape = RoundedCornerShape(10.dp))
                .border(1.dp, DopamineBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.initial,
                style = MaterialTheme.typography.titleMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = DopamineWhite,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = DopamineGrey,
                letterSpacing = 1.2.sp
            )
        }

        Switch(
            checked = app.isBlocked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = DopamineBlack,
                checkedTrackColor = DopamineWhite,
                checkedBorderColor = DopamineWhite,
                uncheckedThumbColor = DopamineGrey,
                uncheckedTrackColor = DopamineSurface,
                uncheckedBorderColor = DopamineBorder
            )
        )
    }
}

@Composable
private fun BlockedAppsEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(color = DopamineCardColor, shape = RoundedCornerShape(12.dp))
            .border(1.dp, DopamineBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No apps found",
            style = MaterialTheme.typography.bodyMedium,
            color = DopamineGrey
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BlockedAppsScreenPreview() {
    DOPAMINE_LOCKTheme {
        BlockedAppsContent(
            uiState = BlockedAppsUiState(
                apps = previewApps,
                filteredApps = previewApps
            ),
            currentRoute = Screen.Settings.route,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigate = {},
            onNavigateBack = {},
            onSearchChange = {},
            onToggleApp = {},
            onSave = {}
        )
    }
}

private val previewApps = listOf(
    AppBlockItem(id = "instagram", name = "Instagram", category = "Social", isBlocked = true),
    AppBlockItem(id = "youtube", name = "YouTube", category = "Video"),
    AppBlockItem(id = "discord", name = "Discord", category = "Chat", isBlocked = true)
)
