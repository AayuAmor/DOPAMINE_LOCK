package com.teamdobermans.dopamine_lock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.teamdobermans.dopamine_lock.MainActivity
import com.teamdobermans.dopamine_lock.repo.EnforcementRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DopamineAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }
        enforcePackage(applicationContext, packageName)
    }

    override fun onInterrupt() = Unit

    companion object {
        private val ignoredPackagePrefixes = listOf(
            "android",
            "com.android.systemui",
            "com.google.android.permissioncontroller",
            "com.google.android.packageinstaller"
        )
        private val emergencyPackages = setOf(
            "com.android.settings",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.android.emergency"
        )
        @Volatile private var lastBlockedPackage: String? = null
        @Volatile private var lastBlockedAt: Long = 0L

        fun enforcePackage(context: Context, packageName: String) {
            val appContext = context.applicationContext
            if (shouldIgnorePackage(appContext, packageName)) return
            if (isDuplicateBlock(packageName)) return

            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                val repository = EnforcementRepositoryImpl(appContext)
                val settings = repository.observeSettings().first()
                val state = repository.observeEnforcementState().first()
                if (!state.active || !settings.blockingEnabled) return@launch
                if (packageName !in state.blockedApps) return@launch

                lastBlockedPackage = packageName
                lastBlockedAt = SystemClock.elapsedRealtime()
                repository.recordBlockedAttempt(packageName)
                launchBlockedOverlay(appContext, packageName)
            }
        }

        private fun shouldIgnorePackage(context: Context, packageName: String): Boolean {
            return packageName == context.packageName ||
                packageName in emergencyPackages ||
                ignoredPackagePrefixes.any { packageName == it || packageName.startsWith("$it.") }
        }

        private fun isDuplicateBlock(packageName: String): Boolean {
            val now = SystemClock.elapsedRealtime()
            return lastBlockedPackage == packageName && now - lastBlockedAt < BLOCK_DEBOUNCE_MS
        }

        private fun launchBlockedOverlay(context: Context, packageName: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
                putExtra(MissionEnforcementService.EXTRA_ENFORCEMENT_DESTINATION, "blocked_app_overlay")
            }
            context.startActivity(intent)
        }

        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        private const val BLOCK_DEBOUNCE_MS = 1_500L
    }
}
