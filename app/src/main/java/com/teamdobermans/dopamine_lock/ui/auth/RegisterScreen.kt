package com.teamdobermans.dopamine_lock.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.components.ButtonVariant
import com.teamdobermans.dopamine_lock.ui.components.DopamineButton
import com.teamdobermans.dopamine_lock.ui.components.DopamineTextField
import com.teamdobermans.dopamine_lock.ui.theme.DopamineError
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite
import com.teamdobermans.dopamine_lock.viewModel.AuthUiState

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    authUiState: AuthUiState,
    onRegister: (String, String, String, String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGitHubSignInClick: () -> Unit,
    onClearMessages: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) onNavigateToDashboard()
    }

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "DOPAMINE LOCK",
                style = MaterialTheme.typography.labelLarge,
                color = DopamineGrey,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = DopamineWhite,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start your discipline journey today",
                style = MaterialTheme.typography.bodyMedium,
                color = DopamineGrey,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            DopamineTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                placeholder = "John Doe",
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = DopamineGrey)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DopamineTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "your@email.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = DopamineGrey)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DopamineTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Create a password",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = DopamineGrey)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = DopamineGrey
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DopamineTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Repeat your password",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = DopamineGrey)
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = DopamineGrey
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            authUiState.errorMessage?.let { message ->
                AuthMessage(text = message, isError = true)
                Spacer(modifier = Modifier.height(12.dp))
            }
            authUiState.successMessage?.let { message ->
                AuthMessage(text = message, isError = false)
                Spacer(modifier = Modifier.height(12.dp))
            }

            DopamineButton(
                text = "Create Account",
                onClick = {
                    onClearMessages()
                    onRegister(fullName, email, password, confirmPassword)
                },
                variant = ButtonVariant.Primary,
                enabled = !authUiState.isLoading,
                isLoading = authUiState.isLoading && authUiState.loadingProvider == null
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthDivider()

            Spacer(modifier = Modifier.height(24.dp))

            AuthProviderButtons(
                isLoading = authUiState.isLoading,
                loadingProvider = authUiState.loadingProvider,
                onGoogleClick = {
                    onClearMessages()
                    onGoogleSignInClick()
                },
                onGitHubClick = {
                    onClearMessages()
                    onGitHubSignInClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DopamineButton(
                text = "Cancel",
                onClick = onNavigateToLogin,
                variant = ButtonVariant.Secondary,
                enabled = !authUiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account? ",
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
        textAlign = TextAlign.Center
    )
}
