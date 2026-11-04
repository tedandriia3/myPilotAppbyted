package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composant simple pour le pull-to-refresh avec animation Lottie personnalisée
 * Affiche l'animation Marshall lorsqu'un rafraîchissement est en cours
 */
@Composable
fun PullToRefreshLottie(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Contenu principal
        content()
        
        // Animation de chargement seulement pour le pull-to-refresh (pas le premier chargement)
        // Pour éviter la duplication, on n'affiche l'animation que si on est vraiment en train de rafraîchir
        // L'animation du premier chargement est gérée dans le composant enfant
    }
}