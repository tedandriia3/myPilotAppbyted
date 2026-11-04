package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.airbnb.lottie.compose.*
import com.pilotprep.app.R
import com.pilotprep.app.util.NavigationStateManager
import android.util.Log

@Composable
fun SmartAppBottomNavigationBar(navController: NavController, onFabClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val navigationManager = NavigationStateManager.rememberNavigationStateManager()

    // Gestion du double-clic avec des états
    var lastHomeClickTime by remember { mutableStateOf(0L) }
    var lastMediathequeClickTime by remember { mutableStateOf(0L) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        SmartLottieNavigationItem(
            label = "Accueil",
            lottieResId = R.raw.lottie_home,
            route = "home",
            currentRoute = currentRoute,
            navController = navController,
            navigationManager = navigationManager,
            context = context,
            onClick = { clickTime ->
                val currentTime = System.currentTimeMillis()
                val isDoubleClick = currentTime - lastHomeClickTime < 500

                if (isDoubleClick) {
                    // Double-clic - retour à la racine
                    Log.d("SmartNav", "Double clic Accueil - retour à la racine")
                    try {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        Log.e("SmartNav", "Erreur navigation double-clic Accueil: ${e.message}")
                        // Fallback: navigation simple
                        navController.navigate("home") {
                            launchSingleTop = true
                        }
                    }
                } else {
                    // Simple clic - navigation normale avec restauration
                    Log.d("SmartNav", "Simple clic Accueil - navigation normale")
                    try {
                        navigationManager.saveNavigationState(context, "home", navController)
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } catch (e: Exception) {
                        Log.e("SmartNav", "Erreur navigation simple Accueil: ${e.message}")
                        // Fallback: navigation simple
                        navController.navigate("home") {
                            launchSingleTop = true
                        }
                    }
                }
                lastHomeClickTime = currentTime
            }
        )

        SmartLottieNavigationItem(
            label = "Médiathèque",
            lottieResId = R.raw.lottie_books,
            route = "mediatheque",
            currentRoute = currentRoute,
            navController = navController,
            navigationManager = navigationManager,
            context = context,
            onClick = { clickTime ->
                val currentTime = System.currentTimeMillis()
                val isDoubleClick = currentTime - lastMediathequeClickTime < 500

                if (isDoubleClick) {
                    // Double-clic - retour à la racine
                    Log.d("SmartNav", "Double clic Médiathèque - retour à la racine")
                    try {
                        navController.navigate("mediatheque") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        Log.e("SmartNav", "Erreur navigation double-clic Médiathèque: ${e.message}")
                        // Fallback: navigation simple
                        navController.navigate("mediatheque") {
                            launchSingleTop = true
                        }
                    }
                } else {
                    // Simple clic - navigation normale avec restauration
                    Log.d("SmartNav", "Simple clic Médiathèque - navigation normale")
                    try {
                        navigationManager.saveNavigationState(context, "mediatheque", navController)
                        navController.navigate("mediatheque") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } catch (e: Exception) {
                        Log.e("SmartNav", "Erreur navigation simple Médiathèque: ${e.message}")
                        // Fallback: navigation simple
                        navController.navigate("mediatheque") {
                            launchSingleTop = true
                        }
                    }
                }
                lastMediathequeClickTime = currentTime
            }
        )

        FabLottieNavigationItem(
            lottieResId = R.raw.lottie_add,
            onClick = onFabClick
        )
    }
}

@Composable
private fun RowScope.SmartLottieNavigationItem(
    label: String,
    lottieResId: Int,
    route: String,
    currentRoute: String?,
    navController: NavController,
    navigationManager: NavigationStateManager,
    context: android.content.Context,
    onClick: (Long) -> Unit
) {
    val isSelected = currentRoute == route ||
            (route == "home" && (currentRoute?.startsWith("theory_") == true || currentRoute?.startsWith("need_to_know") == true)) ||
            (route == "mediatheque" && currentRoute?.startsWith("heritage") == true)

    NavigationBarItem(
        label = { Text(label) },
        selected = isSelected,
        onClick = {
            onClick(System.currentTimeMillis())
        },
        icon = {
            SmartLottieIcon(lottieResId = lottieResId, isSelected = isSelected)
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
                SmartLottieIcon(lottieResId = lottieResId, isSelected = true)
            }
        }
    )
}

@Composable
fun SmartLottieIcon(lottieResId: Int, isSelected: Boolean) {
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