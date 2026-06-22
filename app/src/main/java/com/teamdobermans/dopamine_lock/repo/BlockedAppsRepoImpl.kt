package com.teamdobermans.dopamine_lock.repo

import android.content.Context

class BlockedAppsRepositoryImpl(context: Context) : BlockedAppsRepository {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getBlockedApps(): Set<String> {
        return preferences.getStringSet(KEY_BLOCKED_APPS, emptySet()).orEmpty()
    }

    override fun saveBlockedApps(appIds: Set<String>) {
        preferences.edit()
            .putStringSet(KEY_BLOCKED_APPS, appIds)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "blocked_apps_preferences"
        const val KEY_BLOCKED_APPS = "blocked_app_ids"
    }
}
