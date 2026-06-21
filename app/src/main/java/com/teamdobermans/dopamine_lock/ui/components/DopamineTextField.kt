package com.teamdobermans.dopamine_lock.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineSubtle
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun DopamineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(8.dp),
        label = {
            Text(
                text = label.uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = DopamineGrey
            )
        },
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(text = placeholder, color = DopamineSubtle)
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DopamineWhite,
            unfocusedTextColor = DopamineWhite,
            disabledTextColor = DopamineGrey,
            focusedContainerColor = DopamineCard,
            unfocusedContainerColor = DopamineCard,
            disabledContainerColor = DopamineCard,
            focusedBorderColor = DopamineWhite,
            unfocusedBorderColor = DopamineBorder,
            disabledBorderColor = DopamineBorder,
            focusedLabelColor = DopamineWhite,
            unfocusedLabelColor = DopamineGrey,
            focusedLeadingIconColor = DopamineGrey,
            unfocusedLeadingIconColor = DopamineGrey,
            focusedTrailingIconColor = DopamineGrey,
            unfocusedTrailingIconColor = DopamineGrey,
            cursorColor = DopamineWhite
        )
    )
}
