package com.pilotprep.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pilotprep.app.ui.components.SmartAppBottomNavigationBar
import com.pilotprep.app.ui.components.AppTopBar
import com.pilotprep.app.ui.navigation.AppNavigation
import com.pilotprep.app.ui.sessions.SessionGrid
import com.pilotprep.app.ui.print.PrintableContentRegistry
import com.pilotprep.app.ui.print.printHtml
import com.pilotprep.app.ui.print.PrintOptions
import com.pilotprep.app.util.NavigationStateManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.pilotprep.app.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

@Composable
fun MainScreen() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Gestionnaire de navigation intelligente
    val navigationManager = NavigationStateManager.rememberNavigationStateManager()
    
    // Restauration de l'état au démarrage
    val lastActiveSection = remember {
        NavigationStateManager.getActiveSection(context)
    }

    // Gestion améliorée des NavControllers
    val navControllers = remember { mutableStateMapOf<String, NavHostController>() }

    // Créer les NavControllers pour les sessions existantes
    mainUiState.sessions.forEach { session ->
        if (!navControllers.containsKey(session.id)) {
            navControllers[session.id] = rememberNavController()
        }
    }

    // Nettoyer les NavControllers des sessions supprimées
    LaunchedEffect(mainUiState.sessions) {
        val sessionIds = mainUiState.sessions.map { it.id }.toSet()
        val controllersToRemove = navControllers.keys.filter { it !in sessionIds }
        controllersToRemove.forEach { sessionId ->
            navControllers.remove(sessionId)
        }
    }

    val activeSessionId = mainUiState.activeSessionId
    val showTabSwitcher = mainUiState.showSessionGrid || activeSessionId == null

    Crossfade(targetState = showTabSwitcher, label = "screen_switcher") { isShowingSwitcher ->
        if (isShowingSwitcher) {
            SessionGrid(
                sessions = mainUiState.sessions,
                onSelect = { sessionId -> mainViewModel.onSwitchSession(sessionId) },
                onClose = { sessionId -> mainViewModel.onCloseSession(sessionId) },
                onAddNew = { mainViewModel.onAddNewSessionInGrid() }
            )
        } else {
            if (activeSessionId != null) {
                val activeNavController = navControllers[activeSessionId]
                val activeSession = mainUiState.sessions.find { it.id == activeSessionId }

                if (activeNavController != null && activeSession != null) {
                    // Utiliser une clé stable pour éviter les recompositions
                    key(activeSessionId) {
                        SessionContent(
                            navController = activeNavController,
                            mainViewModel = mainViewModel,
                            activeSessionId = activeSessionId,
                            startRoute = activeSession.startRoute
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionContent(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    activeSessionId: String,
    startRoute: String
) {
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val view = LocalView.current
    val activity = LocalContext.current as ComponentActivity
    val context = LocalContext.current
    
    // Gestionnaire de navigation intelligente
    val navigationManager = NavigationStateManager.rememberNavigationStateManager()
    
    // Sauvegarde automatique de l'état de navigation
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            when {
                route == "home" || route.startsWith("theory_") || route.startsWith("need_to_know") -> {
                    navigationManager.saveNavigationState(context, "home", navController)
                }
                route == "mediatheque" || route.startsWith("heritage") || route.startsWith("audio") || route.startsWith("atc_talk") -> {
                    navigationManager.saveNavigationState(context, "mediatheque", navController)
                }
            }
        }
    }

    // Snackbar de hint (une seule fois)
    val snackbarHostState = remember { SnackbarHostState() }
    val prefs = remember { activity.getSharedPreferences("app_main_prefs", android.content.Context.MODE_PRIVATE) }
    val immersiveHintShownKey = remember { "immersive_hint_shown" }

    // Mode immersif sauvegardé (rotation, process death)
    var isImmersive by rememberSaveable { mutableStateOf(false) }

    // Contrôle des barres système selon l'état immersif
    LaunchedEffect(isImmersive) {
        val controller = WindowInsetsControllerCompat(activity.window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (isImmersive) {
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        }
    }

    val title = when (currentRoute) {
        "home" -> "Accueil"
        "mediatheque" -> "Médiathèque"
        "need_to_know_list" -> "Need to Know"
        "need_to_know_detail" -> "Need to Know"
        else -> "Pilot Prep"
    }

    // Détermine si la route courante est imprimable
    val isPrintable = remember(currentRoute) {
        when {
            // Écrans de leçon / détail 
            currentRoute?.startsWith("lesson_detail") == true -> true
            // Fiche patrimoine détail
            currentRoute?.startsWith("aircraft_detail") == true -> true
            else -> false
        }
    }

    // Impression via PrintHelper (AndroidX Print)
    var showPrintDialog by remember { mutableStateOf(false) }
    val KEY_PRINT_LANDSCAPE = remember { "print_a4_landscape" }
    val KEY_PRINT_MARGIN = remember { "print_margin_mm" }
    val KEY_PRINT_DARK = remember { "print_theme_dark" }
    var a4Landscape by remember { mutableStateOf(prefs.getBoolean(KEY_PRINT_LANDSCAPE, false)) }
    var marginMm by remember { mutableStateOf(prefs.getInt(KEY_PRINT_MARGIN, 18).coerceIn(5, 25)) }
    var darkThemeForPrint by remember { mutableStateOf(prefs.getBoolean(KEY_PRINT_DARK, false)) }
    var headerColorHex by remember { mutableStateOf(prefs.getString("print_header_color", null) ?: "") }
    var headerBorderPx by remember { mutableStateOf(prefs.getInt("print_header_border_px", 1).coerceIn(0, 8)) }
    var footerText by remember { mutableStateOf(prefs.getString("print_footer_text", "") ?: "") }

    fun parseHexToRgb(hex: String): Triple<Int, Int, Int>? {
        val clean = hex.trim().removePrefix("#")
        if (clean.length != 6) return null
        return try {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            Triple(r, g, b)
        } catch (_: Throwable) { null }
    }
    val initialRgb = parseHexToRgb(headerColorHex)
        ?: if (darkThemeForPrint) Triple(0x33, 0x33, 0x33) else Triple(0xDD, 0xDD, 0xDD)
    var headerR by remember { mutableStateOf(initialRgb.first) }
    var headerG by remember { mutableStateOf(initialRgb.second) }
    var headerB by remember { mutableStateOf(initialRgb.third) }
    fun updateHeaderHexFromRGB() {
        headerColorHex = String.format("#%02X%02X%02X", headerR.coerceIn(0,255), headerG.coerceIn(0,255), headerB.coerceIn(0,255))
    }

    fun launchPrintWithOptions() {
        val payload = PrintableContentRegistry.getPayload()
        if (payload?.html != null) {
            printHtml(
                activity,
                payload.html,
                payload.jobName,
                PrintOptions(
                    a4Landscape = a4Landscape,
                    marginMm = marginMm.coerceIn(5, 25),
                    themeDark = darkThemeForPrint,
                    headerTitle = title,
                    headerLogoUrl = "https://firebasestorage.googleapis.com/v0/b/pilot-prep-app.firebasestorage.app/o/app_logo.png?alt=media&token=f573156f-c8d9-4f11-8f8c-31de5b36d41a",
                    headerColorHex = headerColorHex.ifBlank { null },
                    headerBorderPx = headerBorderPx,
                    footerText = footerText.ifBlank { null }
                )
            )
        } else {
            val bitmap = captureView(view)
            androidx.print.PrintHelper(activity).apply {
                scaleMode = androidx.print.PrintHelper.SCALE_MODE_FIT
            }.printBitmap("PilotPrep-${title}", bitmap)
        }
    }

    fun printCurrentView() {
        try {
            val payload = PrintableContentRegistry.getPayload()
            showPrintDialog = payload?.html != null
            if (!showPrintDialog) {
                launchPrintWithOptions()
            }
        } catch (e: Throwable) {
            // Optionnel: afficher une Snackbar d'erreur
            scope.launch {
                snackbarHostState.showSnackbar("Impossible d'imprimer ce contenu")
            }
        }
    }

    val fabOnClick: () -> Unit = {
        scope.launch {
            try {
                // Capture d'écran native Android
                val bitmap = captureView(view)
                // Conversion de Bitmap vers ImageBitmap
                val imageBitmap = bitmap.asImageBitmap()
                mainViewModel.updateSessionPreview(activeSessionId, imageBitmap)
            } catch (error: Throwable) {
                println("Erreur de capture d'écran: ${error.message}")
            }
            mainViewModel.onPlusClicked()
        }
    }

    // CORRECTION : Utiliser une route valide par défaut
    val safeStartRoute = if (startRoute == "home_graph") "home" else startRoute

    // Affiche l'astuce au premier lancement uniquement
    LaunchedEffect(Unit) {
        val hasShown = prefs.getBoolean(immersiveHintShownKey, false)
        if (!hasShown) {
            snackbarHostState.showSnackbar("Astuce: double‑tapez pour plein écran")
            prefs.edit().putBoolean(immersiveHintShownKey, true).apply()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                AppTopBar(
                    title = title,
                    navController = navController,
                    showPrint = isPrintable,
                    onPrintClick = if (isPrintable) ({ printCurrentView() }) else null
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SmartAppBottomNavigationBar(navController = navController, onFabClick = fabOnClick)
            }
        }
    ) { innerPadding ->
        if (showPrintDialog) {
            AlertDialog(
                onDismissRequest = { showPrintDialog = false },
                title = { Text("Options d'impression") },
                text = {
                    Column {
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Checkbox(checked = a4Landscape, onCheckedChange = { a4Landscape = it })
                            Text("A4 paysage")
                        }
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Checkbox(checked = darkThemeForPrint, onCheckedChange = { darkThemeForPrint = it })
                            Text("Thème sombre pour impression")
                        }
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text("Marges (mm): $marginMm")
                        }
                        androidx.compose.material3.Slider(
                            value = marginMm.toFloat(),
                            onValueChange = { marginMm = it.toInt() },
                            valueRange = 5f..25f
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Couleur de l'en-tête:")
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(modifier = Modifier.size(24.dp).background(Color(headerR/255f, headerG/255f, headerB/255f, 1f), RoundedCornerShape(4.dp)))
                            Spacer(Modifier.size(8.dp))
                            Text(headerColorHex.ifBlank { "auto" })
                        }
                        Text("Rouge: $headerR")
                        androidx.compose.material3.Slider(
                            value = headerR.toFloat(),
                            onValueChange = { headerR = it.toInt().coerceIn(0,255); updateHeaderHexFromRGB() },
                            valueRange = 0f..255f
                        )
                        Text("Vert: $headerG")
                        androidx.compose.material3.Slider(
                            value = headerG.toFloat(),
                            onValueChange = { headerG = it.toInt().coerceIn(0,255); updateHeaderHexFromRGB() },
                            valueRange = 0f..255f
                        )
                        Text("Bleu: $headerB")
                        androidx.compose.material3.Slider(
                            value = headerB.toFloat(),
                            onValueChange = { headerB = it.toInt().coerceIn(0,255); updateHeaderHexFromRGB() },
                            valueRange = 0f..255f
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Épaisseur bordure en-tête (px): $headerBorderPx")
                        androidx.compose.material3.Slider(
                            value = headerBorderPx.toFloat(),
                            onValueChange = { headerBorderPx = it.toInt().coerceIn(0, 8) },
                            valueRange = 0f..8f
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Texte de pied de page:")
                        androidx.compose.material3.OutlinedTextField(
                            value = footerText,
                            onValueChange = { footerText = it },
                            singleLine = false,
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showPrintDialog = false
                        prefs.edit()
                            .putBoolean(KEY_PRINT_LANDSCAPE, a4Landscape)
                            .putInt(KEY_PRINT_MARGIN, marginMm.coerceIn(5, 25))
                            .putBoolean(KEY_PRINT_DARK, darkThemeForPrint)
                            .putString("print_header_color", headerColorHex.ifBlank { null })
                            .putInt("print_header_border_px", headerBorderPx)
                            .putString("print_footer_text", footerText.ifBlank { null })
                            .apply()
                        launchPrintWithOptions()
                    }) { Text("Imprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { showPrintDialog = false }) { Text("Annuler") }
                }
            )
        }
        
        // Ajouter une clé stable pour éviter les conflits de ViewModel
        key(activeSessionId) {
            AppNavigation(
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .pointerInput(isImmersive) {
                        detectTapGestures(
                            onDoubleTap = {
                                isImmersive = !isImmersive
                            }
                        )
                    },
                startDestination = safeStartRoute
            )
        }
        
    }
}

// Fonction de capture d'écran native
private fun captureView(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}