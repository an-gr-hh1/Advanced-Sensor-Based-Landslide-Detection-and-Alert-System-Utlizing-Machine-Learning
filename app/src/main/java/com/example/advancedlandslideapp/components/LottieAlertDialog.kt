package com.example.advancedlandslideapp.components

import androidx.compose.foundation.layout.*
import android.media.MediaPlayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.advancedlandslideapp.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

@Composable
fun LottieAlertDialog(
    message: String
) {
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) return

    val context = LocalContext.current
    // Dynamically obtain the raw resource ID for "danger_animation.json" located in res/raw.
    val rawId = context.resources.getIdentifier("danger_animation", "raw", context.packageName)

    // Load the Lottie composition using the obtained resource ID.
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(rawId)
    )
    val composition = compositionResult.value

    // Animate the composition indefinitely.
    val animationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // Create and start MediaPlayer using DisposableEffect
    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.alert_sound).apply {
            start()
        }
        onDispose {
            mediaPlayer.release()
        }
    }

    // Control confirm button state: locked for 5 seconds
    var isButtonEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(5000)
        isButtonEnabled = true
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Landslide Alert!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { animationState.progress },
                        modifier = Modifier.size(100.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier.size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text="$message Please take immediate actions.",
                    style = MaterialTheme.typography.bodyMedium
                        .copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                    textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = {showDialog = false},
                enabled = isButtonEnabled
            ) {
                Text("OK")
            }
        },
        dismissButton = {}
    )
}