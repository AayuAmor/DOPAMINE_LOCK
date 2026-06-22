package com.teamdobermans.dopamine_lock.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.teamdobermans.dopamine_lock.model.AppBlockItem
import com.teamdobermans.dopamine_lock.enforcement.InstalledAppsProvider
import com.teamdobermans.dopamine_lock.repo.BlockedAppsRepository

data class BlockedAppsUiState(
    val searchQuery: String = "",
    val apps: List<AppBlockItem> = emptyList(),
    val filteredApps: List<AppBlockItem> = emptyList(),
    val errorMessage: String? = null
) {
    val selectedCount: Int
        get() = apps.count { it.isBlocked }
}

class BlockedAppsViewModel(
    private val repository: BlockedAppsRepository,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    var uiState by mutableStateOf(BlockedAppsUiState())
        private set

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        runCatching {
            val blockedAppIds = repository.getBlockedApps()
            installedAppsProvider.getInstalledApps().map { app ->
                app.copy(isBlocked = app.id in blockedAppIds)
            }
        }.onSuccess { apps ->
            uiState = BlockedAppsUiState(apps = apps, filteredApps = filterApps(apps, uiState.searchQuery))
        }.onFailure { exception ->
            uiState = uiState.copy(errorMessage = exception.message ?: "Failed to load installed apps.")
        }
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

}
