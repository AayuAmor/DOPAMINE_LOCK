package com.teamdobermans.dopamine_lock.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import com.teamdobermans.dopamine_lock.viewModel.AuthUiState

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    authUiState: AuthUiState,
    onSendReset: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val emailSent = authUiState.successMessage != null
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = DopamineCard, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DopamineWhite
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(color = DopamineCard, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = DopamineWhite,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!emailSent)
                    "Enter your email address and we'll send you a link to reset your password."
                else
                    "A reset link has been sent. Check your inbox and follow the instructions.",
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!emailSent) {
                DopamineTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "your@email.com",
                    keyboardType = KeyboardType.Email,
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = DopamineGrey)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                authUiState.errorMessage?.let { message ->
                    AuthMessage(text = message, isError = true)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                DopamineButton(
                    text = "Send Reset Link",
                    onClick = {
                        onClearMessages()
                        onSendReset(email)
                    },
                    variant = ButtonVariant.Primary,
                    enabled = !authUiState.isLoading,
                    isLoading = authUiState.isLoading
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = DopamineCard, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "CHECK YOUR EMAIL",
                            style = MaterialTheme.typography.labelSmall,
                            color = DopamineGrey,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DopamineWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DopamineButton(
                    text = "Resend Email",
                    onClick = {
                        onClearMessages()
                        onSendReset(email)
                    },
                    variant = ButtonVariant.Secondary,
                    enabled = !authUiState.isLoading,
                    isLoading = authUiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember your password? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineGrey
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AuthMessage(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) DopamineError else DopamineWhite,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
