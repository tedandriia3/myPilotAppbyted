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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.pilotprep.app.ui.theme.PilotPrepAppTheme
import kotlinx.coroutines.delay
import com.pilotprep.app.ui.theme.AviationDarkBlue

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            PilotPrepAppTheme {
                VideoSplashScreen()
            }
        }
    }
} // <- Cette accolade fermait la classe, elle doit être à la fin du fichier

// Définir notre police personnalisée
val orbitronFontFamily = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

@Composable
fun AnimatedWelcomeText() {
    // Gérer l'état de la phrase affichée
    var currentPhraseIndex by remember { mutableStateOf(0) }
    val phrases = listOf(
        "Bienvenue à  bord...",
        "...Commandant."
    )

    // Gérer l'animation de fondu (alpha = transparence)
    var alpha by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 1500), // Durée du fondu
        label = "textAlpha"
    )

    // Logique pour changer les phrases et déclencher les animations
    LaunchedEffect(Unit) {
        // Phrase 1
        alpha = 1f // Fade In
        delay(2500)
        alpha = 0f // Fade Out
        delay(1500)

        // Phrase 2
        currentPhraseIndex = 1
        alpha = 1f // Fade In
        delay(2500)
        alpha = 0f // Fade Out
    }

    // Le composant Text qui sera affiché
    Text(
        text = phrases[currentPhraseIndex],
        modifier = Modifier
            .alpha(animatedAlpha) // Appliquer l'animation de transparence
            .shadow(elevation = 10.dp, spotColor = Color.White, ambientColor = Color.White), // Effet de lueur
        color = Color.White,
        fontSize = 28.sp,
        fontFamily = orbitronFontFamily,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            // Effet de lueur subtil
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

    // Cette partie ne change pas
    LaunchedEffect(Unit) {
        delay(8000L)
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    // Remplacer Box par Column pour une disposition verticale
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Appliquer la couleur de fond directement à la Column
            .background(AviationDarkBlue),
        horizontalAlignment = Alignment.CenterHorizontally, // Centrer les éléments horizontalement
        verticalArrangement = Arrangement.Center // Centrer le groupe verticalement
    ) {

        // Premier élément de la colonne : Le texte animé (inchangé)
        AnimatedWelcomeText()

        // Ajouter un espace vertical entre le texte et la vidéo
        Spacer(modifier = Modifier.height(64.dp))

        // Deuxième élément de la colonne : La vidéo
        AndroidView(
            modifier = Modifier
                .fillMaxWidth() // La vidéo prendra toute la largeur
                .padding(horizontal = 32.dp) // Avec des marges sur les côtés
                .aspectRatio(16f / 9f) // Garder un format 16:9
                .clip(RoundedCornerShape(16.dp)), // (Optionnel) Ajoute de jolis coins arrondis
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