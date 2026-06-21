package com.teamdobermans.dopamine_lock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.dopamine_lock.ui.theme.DopamineBorder
import com.teamdobermans.dopamine_lock.ui.theme.DopamineCard
import com.teamdobermans.dopamine_lock.ui.theme.DopamineDivider
import com.teamdobermans.dopamine_lock.ui.theme.DopamineGrey
import com.teamdobermans.dopamine_lock.ui.theme.DopamineWhite

@Composable
fun FocusProgressCard(
    title: String,
    current: Float,
    total: Float,
    unit: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val progress = if (total > 0f) (current / total).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DopamineCard),
        border = BorderStroke(1.dp, DopamineBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = DopamineGrey,
                    modifier = Modifier.weight(1f),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleSmall,
                    color = DopamineWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = DopamineWhite,
                trackColor = DopamineDivider
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${current.toInt()} / ${total.toInt()} $unit",
                style = MaterialTheme.typography.bodySmall,
                color = DopamineGrey
            )
        }
    }
}
