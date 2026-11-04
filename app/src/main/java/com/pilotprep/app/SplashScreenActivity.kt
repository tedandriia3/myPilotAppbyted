package com.pilotprep.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pilotprep.app.ui.theme.DarkBackground
import com.pilotprep.app.ui.theme.PilotPrepAppTheme
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration pour le plein écran
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            // On force le thème sombre pour le splash screen pour une expérience immersive
            PilotPrepAppTheme(darkTheme = true) {
                VideoSplashScreen()
            }
        }
    }
}

// Définition de la police personnalisée
val orbitronFontFamily = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

@Composable
fun AnimatedWelcomeText() {
    var currentPhraseIndex by remember { mutableIntStateOf(0) }
    val phrases = listOf(
        "Bienvenue à bord...",
        "...commandant."
    )

    var alpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 1500),
        label = "textAlpha"
    )

    LaunchedEffect(Unit) {
        // Phrase 1
        alpha = 1f // Fondu entrant
        delay(3500)
        alpha = 0f // Fondu sortant
        delay(1500)

        // Phrase 2
        currentPhraseIndex = 1
        alpha = 1f // Fondu entrant
        delay(3500)
        alpha = 0f // Fondu sortant
    }

    Text(
        text = phrases[currentPhraseIndex],
        modifier = Modifier.alpha(animatedAlpha),
        color = Color.White,
        fontSize = 28.sp,
        fontFamily = orbitronFontFamily,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            shadow = androidx.compose.ui.graphics.Shadow(
                color = Color(0x99FFFFFF),
                blurRadius = 15f
            )
        )
    )
}


@Composable
fun VideoSplashScreen() {
    val context = LocalContext.current

    // Redirige vers MainActivity après 8.5 secondes
    LaunchedEffect(Unit) {
        delay(7500L)
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground), // Utilise la nouvelle couleur de fond
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedWelcomeText()
        Spacer(modifier = Modifier.height(64.dp))
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp)),
            factory = { ctx ->
                VideoView(ctx).apply {
                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_video}")
                    setVideoURI(videoUri)
                    setMediaController(null)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                    }
                    start()
                }
            }
        )
    }
}