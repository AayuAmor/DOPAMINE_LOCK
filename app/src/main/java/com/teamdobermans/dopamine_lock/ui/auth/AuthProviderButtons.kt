package com.teamdobermans.dopamine_lock.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey

@Composable
fun AuthProviderButtons(
    isLoading: Boolean,
    loadingProvider: String?,
    onGoogleClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DopamineButton(
            text = if (loadingProvider == AuthProvider.Google) "CONNECTING..." else "CONTINUE WITH GOOGLE",
            onClick = onGoogleClick,
            variant = ButtonVariant.Secondary,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        DopamineButton(
            text = if (loadingProvider == AuthProvider.GitHub) "CONNECTING..." else "CONTINUE WITH GITHUB",
            onClick = onGitHubClick,
            variant = ButtonVariant.Secondary,
            enabled = !isLoading
        )
    }
}

@Composable
fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = DopamineBorder)
        Text(
            text = "  OR  ",
            style = MaterialTheme.typography.labelSmall,
            color = DopamineGrey
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = DopamineBorder)
    }
}

object AuthProvider {
    const val Google = "Google"
    const val GitHub = "GitHub"
}
