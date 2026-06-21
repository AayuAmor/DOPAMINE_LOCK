package com.teamdobermans.dopamine_lock.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.teamdobermans.dopamine_lock.model.AppBlockItem
import com.teamdobermans.dopamine_lock.repo.BlockedAppsRepository

data class BlockedAppsUiState(
    val searchQuery: String = "",
    val apps: List<AppBlockItem> = emptyList(),
    val filteredApps: List<AppBlockItem> = emptyList()
) {
    val selectedCount: Int
        get() = apps.count { it.isBlocked }
}

class BlockedAppsViewModel(
    private val repository: BlockedAppsRepository
) : ViewModel() {

    var uiState by mutableStateOf(BlockedAppsUiState())
        private set

    init {
        val blockedAppIds = repository.getBlockedApps()
        val apps = mockInstalledApps.map { app ->
            app.copy(isBlocked = app.id in blockedAppIds)
        }
        uiState = BlockedAppsUiState(apps = apps, filteredApps = apps)
    }

    fun toggleApp(appId: String) {
        val updatedApps = uiState.apps.map { app ->
            if (app.id == appId) app.copy(isBlocked = !app.isBlocked) else app
        }
        uiState = uiState.copy(
            apps = updatedApps,
            filteredApps = filterApps(updatedApps, uiState.searchQuery)
        )
    }

    fun searchApps(query: String) {
        uiState = uiState.copy(
            searchQuery = query,
            filteredApps = filterApps(uiState.apps, query)
        )
    }

    fun saveBlockedApps() {
        repository.saveBlockedApps(
            uiState.apps
                .filter { it.isBlocked }
                .map { it.id }
                .toSet()
        )
    }

    private fun filterApps(apps: List<AppBlockItem>, query: String): List<AppBlockItem> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return apps

        return apps.filter { app ->
            app.name.contains(trimmedQuery, ignoreCase = true) ||
                app.category.contains(trimmedQuery, ignoreCase = true)
        }
    }

    private companion object {
        val mockInstalledApps = listOf(
            AppBlockItem(id = "com.instagram.android", name = "Instagram", category = "Social"),
            AppBlockItem(id = "com.zhiliaoapp.musically", name = "TikTok", category = "Video"),
            AppBlockItem(id = "com.google.android.youtube", name = "YouTube", category = "Video"),
            AppBlockItem(id = "com.facebook.katana", name = "Facebook", category = "Social"),
            AppBlockItem(id = "com.snapchat.android", name = "Snapchat", category = "Social"),
            AppBlockItem(id = "com.twitter.android", name = "X / Twitter", category = "Social", initial = "X"),
            AppBlockItem(id = "com.reddit.frontpage", name = "Reddit", category = "Social"),
            AppBlockItem(id = "com.netflix.mediaclient", name = "Netflix", category = "Entertainment"),
            AppBlockItem(id = "com.discord", name = "Discord", category = "Chat"),
            AppBlockItem(id = "org.telegram.messenger", name = "Telegram", category = "Chat")
        )
    }
}
