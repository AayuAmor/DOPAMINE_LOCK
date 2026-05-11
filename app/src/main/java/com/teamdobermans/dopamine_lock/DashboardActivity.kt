package com.teamdobermans.dopamine_lock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DashboardBody()
        }
    }
}

@Composable
fun DashboardBody() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {

        item {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Dashboard",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                DashboardCard("4.2", "Focus Hours")

                DashboardCard("7", "Streak")

                DashboardCard("12", "Sessions")
            }

            Spacer(modifier = Modifier.height(30.dp))

            DashboardLargeCard(
                title = "Today's Goal",
                description = "Complete 4 Focus Sessions"
            )

            Spacer(modifier = Modifier.height(30.dp))

            DashboardLargeCard(
                title = "Mission Mode",
                description = "No distractions today."
            )

            Spacer(modifier = Modifier.height(30.dp))

            DashboardLargeCard(
                title = "Recent Session",
                description = "Deep Work - 2h 15m"
            )
        }
    }
}

@Composable
fun DashboardCard(
    value: String,
    title: String
) {

    Card(
        modifier = Modifier.size(
            width = 110.dp,
            height = 110.dp
        ),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = value,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DashboardLargeCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    DashboardBody()
}