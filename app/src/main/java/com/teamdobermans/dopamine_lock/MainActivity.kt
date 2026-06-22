package com.teamdobermans.dopamine_lock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import com.teamdobermans.dopamine_lock.navigation.AppNavigation
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager.Companion.EXTRA_NOTIFICATION_DESTINATION
import com.teamdobermans.dopamine_lock.service.MissionEnforcementService
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme

class MainActivity : ComponentActivity() {
    private var externalDestination by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Notifications are optional. You can enable them later in settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        externalDestination = readExternalDestination(intent)
        enableEdgeToEdge()
        DopamineNotificationManager(this).createChannels()
        requestNotificationPermissionOnce()
        setContent {
            DOPAMINE_LOCKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Black
                ) {
                    AppNavigation(
                        externalDestination,
                        { externalDestination = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalDestination = readExternalDestination(intent)
    }

    private fun readExternalDestination(intent: Intent?): String? {
        return intent?.getStringExtra(MissionEnforcementService.EXTRA_ENFORCEMENT_DESTINATION)
            ?: intent?.getStringExtra(EXTRA_NOTIFICATION_DESTINATION)
    }

    private fun requestNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return

        val preferences = getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
        if (preferences.getBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, false)) return

        preferences.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_ASKED, true).apply()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private companion object {
        const val PERMISSION_PREFS = "notification_permission_preferences"
        const val KEY_NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked"
    }
}
