package com.teamdobermans.dopamine_lock.enforcement

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.teamdobermans.dopamine_lock.model.AppBlockItem

class InstalledAppsProvider(
    private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    fun getInstalledApps(): List<AppBlockItem> {
        return filterLaunchableApps()
            .filterNot { it.activityInfo.packageName in excludedPackages }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(packageManager).toString()
                AppBlockItem(
                    id = packageName,
                    name = appName,
                    category = inferCategory(packageName, appName)
                )
            }
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase() }
    }

    fun filterLaunchableApps(): List<android.content.pm.ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
    }

    fun searchApps(query: String, apps: List<AppBlockItem> = getInstalledApps()): List<AppBlockItem> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return apps

        return apps.filter { app ->
            app.name.contains(trimmedQuery, ignoreCase = true) ||
                app.id.contains(trimmedQuery, ignoreCase = true) ||
                app.category.contains(trimmedQuery, ignoreCase = true)
        }
    }

    private fun inferCategory(packageName: String, appName: String): String {
        val value = "$packageName $appName".lowercase()
        return when {
            socialKeywords.any { it in value } -> "Social"
            videoKeywords.any { it in value } -> "Video"
            chatKeywords.any { it in value } -> "Chat"
            entertainmentKeywords.any { it in value } -> "Entertainment"
            else -> "App"
        }
    }

    private val excludedPackages: Set<String> = buildSet {
        add(context.packageName)
        add("com.android.settings")
        add("com.google.android.dialer")
        add("com.android.dialer")
        add("com.google.android.apps.messaging")
        add("com.android.mms")
        add("com.android.emergency")
        add("com.google.android.apps.safetycenter")
        addAll(resolveLauncherPackages())
    }

    private fun resolveLauncherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .toSet()
    }

    private companion object {
        val socialKeywords = listOf("instagram", "facebook", "twitter", "reddit", "snapchat", "tiktok", "musically")
        val videoKeywords = listOf("youtube", "video", "shorts")
        val chatKeywords = listOf("discord", "telegram", "whatsapp", "messenger")
        val entertainmentKeywords = listOf("netflix", "primevideo", "hulu", "spotify")
    }
}
