package com.teamdobermans.dopamine_lock.ui.dashboard

import android.os.Bundle
import com.teamdobermans.dopamine_lock.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    Scaffold(
        bottomBar = { CustomBottomNavBar(currentScreen = "home") },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Good evening, User",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_notifications_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardCard("4.2", "Focus Hours", Modifier.weight(1f))
                    DashboardCard("3", "Sessions", Modifier.weight(1f))
                    DashboardCard("7", "Streak", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(25.dp))

                GoalCard(
                    title = "Today's Goal",
                    description = "Complete 4 Focus Sessions",
                    progress = 0.75f
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Quick Actions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton("Start Focus", R.drawable.baseline_play_arrow_24, Modifier.weight(1f))
                    QuickActionButton("My Tasks", R.drawable.baseline_list_24, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Recent Sessions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(15.dp))
                RecentSessionItem("Deep Work Session", "2h 15m")
                RecentSessionItem("Pomodoro Session", "25m")

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DashboardCard(value: String, title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.Gray, fontSize = 12.sp)
            Text(text = "Today", color = Color.DarkGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun GoalCard(title: String, description: String, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "${(progress * 100).toInt()}%", color = Color.Gray)
            }
            Text(text = description, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun QuickActionButton(label: String, iconRes: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(56.dp).clickable { },
        color = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentSessionItem(name: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF111111), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_history_24),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = Color.White, fontSize = 16.sp)
        }
        Text(text = time, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun CustomBottomNavBar(currentScreen: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Home", R.drawable.baseline_home_24, currentScreen == "home")
            NavItem("Sessions", R.drawable.baseline_timer_24, currentScreen == "sessions")
            NavItem("Tasks", R.drawable.baseline_list_alt_24, currentScreen == "tasks")
            NavItem("Profile", R.drawable.baseline_person_24, currentScreen == "profile")
        }
    }
}

@Composable
fun NavItem(label: String, iconRes: Int, active: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (active) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (active) Color.White else Color.Gray,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardBody()
}