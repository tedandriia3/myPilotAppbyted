package com.pilotprep.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

@Composable
fun MarshallerLoadingAnimation(modifier: Modifier = Modifier) {

    // --- Configuration de l'animation en boucle infinie ---
    val infiniteTransition = rememberInfiniteTransition(label = "marshaller-transition")

    // 1. Animation de l'angle du bras droit
    // On anime une rotation de -135 degrés (position 7h) à 45 degrés (position 1h)
    val armAngle by infiniteTransition.animateFloat(
        initialValue = -135f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // L'animation fera l'aller-retour
        ),
        label = "armAngle"
    )

    // 2. Animation de la lueur pulsante du bâton
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // --- Définition des couleurs et du style ---
    val marshallerColor = Color.Gray.copy(alpha = 0.8f)
    val stickColor = Color(0xFFFFA500) // Orange vif
    val stickGlowColor = Color(0x99FF5722) // Lueur orange/rouge semi-transparente

    // --- Dessin de l'animation avec Canvas ---
    Canvas(modifier = modifier.size(150.dp)) {
        val stickWidth = 12f
        val stickLength = 80f
        val headRadius = 20f
        val shoulderOffsetY = headRadius + 10f

        // Dessiner la tête
        drawCircle(
            color = marshallerColor,
            radius = headRadius,
            center = Offset(center.x, center.y - 50f)
        )

        // Dessiner le corps
        drawRoundRect(
            color = marshallerColor,
            topLeft = Offset(center.x - 25f, center.y - 30f),
            size = androidx.compose.ui.geometry.Size(50f, 100f),
            cornerRadius = CornerRadius(15f, 15f)
        )

        // Bâton statique (bras gauche)
        drawRoundRect(
            color = stickColor,
            topLeft = Offset(center.x - 60f, center.y - 60f),
            size = androidx.compose.ui.geometry.Size(stickWidth, stickLength),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // Bâton mobile (bras droit) avec son animation
        withTransform({
            // On applique la rotation autour du point de l'épaule droite
            rotate(degrees = armAngle, pivot = Offset(center.x + 20f, center.y - shoulderOffsetY))
        }) {
            // Lueur pulsante (un cercle plus grand et flou derrière le bâton)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(stickGlowColor, Color.Transparent),
                    radius = (stickWidth * 2.5f) * glowScale // L'animation de pulsation change le rayon
                ),
                radius = (stickWidth * 2.5f) * glowScale,
                center = Offset(center.x + 20f, center.y + 10f)
            )

            // Le bâton mobile lui-même
            drawRoundRect(
                color = stickColor,
                topLeft = Offset(center.x + 14f, center.y - 30f),
                size = androidx.compose.ui.geometry.Size(stickWidth, stickLength),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Effet de traînée de lumière (simulé avec une ligne dégradée)
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(stickColor.copy(alpha = 0.5f), Color.Transparent)
                ),
                start = Offset(center.x + 20f, center.y + 50f),
                end = Offset(center.x + 20f, center.y + 100f),
                strokeWidth = stickWidth,
                cap = StrokeCap.Round
            )
        }
    }
}