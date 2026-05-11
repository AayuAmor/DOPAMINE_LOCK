package com.teamdobermans.dopamine_lock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {

    var startAnimation by remember {
        mutableStateOf(false)
    }

    val alphaAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(2000),
        label = ""
    )

    LaunchedEffect(true) {

        startAnimation = true

        delay(1500)

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alphaAnimation.value)
    ) {

        Image(
            painter = painterResource(
                R.drawable.dopamine_splash
            ),

            contentDescription = null,

            modifier = Modifier.fillMaxSize(),

            contentScale = ContentScale.Crop
        )
    }
}

@Preview
@Composable
fun SplashPreview(){
    SplashBody()
}