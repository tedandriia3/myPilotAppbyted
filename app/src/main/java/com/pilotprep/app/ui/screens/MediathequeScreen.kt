package com.pilotprep.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilotprep.app.R

// La classe de données ne change pas
data class MenuItem(
    val title: String,
    val route: String,
    val imageResId: Int
)

// La fonction principale de l'écran ne change pas
@Composable
fun MediathequeScreen(navController: NavController) {
    val menuItems = listOf(
        MenuItem("Théorie du vol", "theory_selection", R.drawable.theorie_du_vol),
        MenuItem("Mise en Pratique", "license_selection/practice", R.drawable.mise_en_pratique),
        MenuItem("Simulation d'Examen", "license_selection/exam", R.drawable.simulation_examen),
        MenuItem("Patrimoine du Ciel", "heritage", R.drawable.patrimoine_ciel),
        MenuItem("Voix du Ciel", "audio", R.drawable.voix_du_ciel)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { item ->
            GridCard(
                title = item.title,
                imageResId = item.imageResId,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}


// --- MODIFICATION PRINCIPALE ICI ---
@Composable
fun GridCard(title: String, imageResId: Int, onClick: () -> Unit) {
    // 1. Le composant racine est maintenant une Column. C'est elle qui gère le clic.
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally // Centrer la carte et le texte
    ) {
        // 2. La Card contient UNIQUEMENT l'image. Elle sert pour le visuel (ombre, coins arrondis).
        Card(
            modifier = Modifier
                .fillMaxWidth() // Prend toute la largeur de la cellule de la grille
                .aspectRatio(1f), // Reste carrée
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(), // L'image remplit la Card
                contentScale = ContentScale.Crop
            )
        }

        // 3. On ajoute un petit espace entre l'image et le texte
        Spacer(modifier = Modifier.height(8.dp))

        // 4. Le Text est maintenant un enfant de la Column, à l'extérieur de la Card.
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp) // Un peu de padding pour éviter que le texte ne touche les bords s'il est long
        )
    }
}