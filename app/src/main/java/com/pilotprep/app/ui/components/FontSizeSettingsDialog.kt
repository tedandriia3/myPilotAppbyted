package com.pilotprep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeSettingsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentFontScale: Float,
    onFontScaleChanged: (Float) -> Unit,
    onReset: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.TextIncrease,
                        contentDescription = "Options d'affichage",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Options d'Affichage",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Prévisualisation du texte
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Prévisualisation",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ceci est un exemple de texte qui s'adapte à la taille sélectionnée.",
                                fontSize = (14 * currentFontScale).sp,
                                lineHeight = (20 * currentFontScale).sp
                            )
                        }
                    }
                    
                    // Contrôles de taille
                    Column {
                        Text(
                            "Taille de la police",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // SeekBar avec boutons + et -
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { 
                                    val newScale = (currentFontScale - 0.1f).coerceAtLeast(0.7f)
                                    onFontScaleChanged(newScale)
                                },
                                enabled = currentFontScale > 0.7f
                            ) {
                                Icon(
                                    Icons.Default.TextDecrease,
                                    contentDescription = "Diminuer",
                                    tint = if (currentFontScale > 0.7f) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                            
                            Slider(
                                value = currentFontScale,
                                onValueChange = onFontScaleChanged,
                                valueRange = 0.7f..1.5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                            
                            IconButton(
                                onClick = { 
                                    val newScale = (currentFontScale + 0.1f).coerceAtMost(1.5f)
                                    onFontScaleChanged(newScale)
                                },
                                enabled = currentFontScale < 1.5f
                            ) {
                                Icon(
                                    Icons.Default.TextIncrease,
                                    contentDescription = "Augmenter",
                                    tint = if (currentFontScale < 1.5f) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                        
                        // Indicateur de taille actuelle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Petite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "${(currentFontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Grande",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onReset) {
                    Text("Réinitialiser")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        )
    }
}

