package com.teamdobermans.dopamine_lock.repo

interface BlockedAppsRepository {
    fun getBlockedApps(): Set<String>
    fun saveBlockedApps(appIds: Set<String>)
}
