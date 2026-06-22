package com.teamdobermans.dopamine_lock.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.R
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun AuthProviderButtons(
    isLoading: Boolean,
    loadingProvider: String?,
    onGoogleClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OAuthProviderButton(
            text = if (loadingProvider == AuthProvider.Google) "CONNECTING..." else "CONTINUE WITH GOOGLE",
            logoRes = R.drawable.google_logo,
            onClick = onGoogleClick,
            enabled = !isLoading,
            isLoading = loadingProvider == AuthProvider.Google
        )
        Spacer(modifier = Modifier.height(12.dp))
        OAuthProviderButton(
            text = if (loadingProvider == AuthProvider.GitHub) "CONNECTING..." else "CONTINUE WITH GITHUB",
            logoRes = R.drawable.github_logo,
            onClick = onGitHubClick,
            enabled = !isLoading,
            isLoading = loadingProvider == AuthProvider.GitHub
        )
    }
}

@Composable
private fun OAuthProviderButton(
    text: String,
    logoRes: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) DopamineBorder else DopamineBorder.copy(alpha = 0.4f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DopamineWhite,
            disabledContentColor = DopamineGrey
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = DopamineWhite,
                strokeWidth = 2.dp
            )
        } else {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
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
