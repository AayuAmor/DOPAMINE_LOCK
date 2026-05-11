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
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            OnboardingBody()
        }
    }
}

@Composable
fun OnboardingBody() {

//    val context = LocalContext.current
//    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Welcome to\nDopamine Lock",
            style = TextStyle(
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(60.dp))

        FeatureCard("Block Distractions")
        Spacer(modifier = Modifier.height(20.dp))

        FeatureCard("Focus Deeply")
        Spacer(modifier = Modifier.height(20.dp))

        FeatureCard("Track Progress")
        Spacer(modifier = Modifier.height(20.dp))

        FeatureCard("Build Discipline")

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = {

//                val intent = Intent(
//                    context,
//                    LoginActivity::class.java
//                )
//
//                context.startActivity(intent)
//                activity?.finish()
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )


        ) {
            Text("Get Started", style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.W700
            ))
        }
    }
}

@Composable
fun FeatureCard(title: String) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
        )
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(24.dp),
            style = TextStyle(
                color = Color.White,
                fontSize = 20.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingBody()
}