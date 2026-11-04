package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pilotprep.app.R

@Composable
fun MarshallerLoadingAnimation(modifier: Modifier = Modifier) {
    // 1. On charge la composition de notre fichier Lottie de chargement
    // Assurez-vous que votre fichier s'appelle bien "lottie_loading_airplane.json" dans res/raw
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.lottie_loading_airplane)
    )

    // 2. On affiche l'animation
    LottieAnimation(
        composition = composition,
        // On la fait jouer en boucle à l'infini
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(150.dp) // On garde une taille standard
    )
}