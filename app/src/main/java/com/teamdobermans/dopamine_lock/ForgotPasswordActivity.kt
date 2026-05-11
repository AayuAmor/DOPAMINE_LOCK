package com.teamdobermans.dopamine_lock

import android.R
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
import com.teamdobermans.dopamine_lock.ui.theme.DOPAMINE_LOCKTheme
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ForgotPasswordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ForgotPasswordBody()
        }
    }
}

@Composable
fun ForgotPasswordBody() {

    var email by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(100.dp))

        Text(
            text = "Forgot Password?",
            style = TextStyle(
                color = Color.White,
                fontSize = 34.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Enter your email to receive reset link",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        CustomTextField(value = email, onValueChange = { email = it }, label = "Email")

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {

                Toast.makeText(
                    context,
                    "Reset Link Sent",
                    Toast.LENGTH_LONG
                ).show()
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )

        ) {

            Text("Send Reset Link", style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.W700
            ))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgetPasswordPreview() {
    ForgotPasswordBody()
}