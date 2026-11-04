package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.airbnb.lottie.compose.*
import com.pilotprep.app.R

@Composable
fun AppBottomNavigationBar(navController: NavController, onFabClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        LottieNavigationItem(
            label = "Accueil",
            lottieResId = R.raw.lottie_home,
            route = "home",
            currentRoute = currentRoute,
            navController = navController
        )
        LottieNavigationItem(
            label = "Médiathèque",
            lottieResId = R.raw.lottie_books,
            route = "mediatheque",
            currentRoute = currentRoute,
            navController = navController
        )
        FabLottieNavigationItem(
            lottieResId = R.raw.lottie_add,
            onClick = onFabClick
        )
    }
}

@Composable
private fun RowScope.LottieNavigationItem(
    label: String,
    lottieResId: Int,
    route: String,
    currentRoute: String?,
    navController: NavController
) {
    val isSelected = currentRoute == route

    NavigationBarItem(
        label = { Text(label) },
        selected = isSelected,
        onClick = {
            navController.navigate(route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Evite de lancer plusieurs copies de la même destination
                launchSingleTop = true
                // Restaure l'état quand on re-sélectionne un item
                restoreState = true
            }
        },
        icon = {
            LottieIcon(lottieResId = lottieResId, isSelected = isSelected)
        }
    )
}

@Composable
private fun RowScope.FabLottieNavigationItem(lottieResId: Int, onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                LottieIcon(lottieResId = lottieResId, isSelected = true)
            }
        }
    )
}

@Composable
fun LottieIcon(lottieResId: Int, isSelected: Boolean) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isSelected,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition = composition,
        progress = { if (isSelected) progress else 0f },
        modifier = Modifier.size(40.dp)
    )
}