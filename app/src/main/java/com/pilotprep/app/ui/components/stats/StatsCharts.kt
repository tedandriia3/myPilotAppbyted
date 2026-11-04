package com.pilotprep.app.ui.components.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilotprep.app.data.model.SubjectStats
import com.pilotprep.app.data.model.WeeklyProgress
import com.pilotprep.app.ui.theme.*
import kotlin.math.*

/**
 * Composant de graphique en barres pour les scores par matière
 */
@Composable
fun SubjectScoresBarChart(
    subjectStats: List<SubjectStats>,
    modifier: Modifier = Modifier
) {
    if (subjectStats.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune donnée disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Scores par Matière",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                drawBarChart(
                    subjectStats = subjectStats,
                    barColor = primaryColor,
                    textColor = onSurfaceColor
                )
            }
        }
    }
}

/**
 * Dessine le graphique en barres
 */
private fun DrawScope.drawBarChart(
    subjectStats: List<SubjectStats>,
    barColor: Color,
    textColor: Color
) {
    val maxScore = subjectStats.maxOfOrNull { it.averageScore } ?: 100f
    val barWidth = size.width / (subjectStats.size * 1.5f)
    val chartHeight = size.height * 0.8f
    val startY = size.height * 0.1f
    
    subjectStats.forEachIndexed { index, stats ->
        val barHeight = (stats.averageScore / maxScore) * chartHeight
        val x = (index * barWidth * 1.5f) + (barWidth * 0.25f)
        val y = startY + chartHeight - barHeight
        
        // Dessiner la barre
        drawRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
        
        // Dessiner le score au-dessus de la barre
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = textColor.toArgb()
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(
                "${stats.averageScore.toInt()}%",
                x + barWidth / 2,
                y - 8.dp.toPx(),
                textPaint
            )
        }
    }
}

/**
 * Composant de graphique linéaire pour la progression hebdomadaire
 */
@Composable
fun WeeklyProgressLineChart(
    weeklyProgress: List<WeeklyProgress>,
    modifier: Modifier = Modifier
) {
    if (weeklyProgress.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune donnée de progression",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Progression Hebdomadaire",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                drawLineChart(
                    weeklyProgress = weeklyProgress,
                    lineColor = primaryColor
                )
            }
        }
    }
}

/**
 * Dessine le graphique linéaire
 */
private fun DrawScope.drawLineChart(
    weeklyProgress: List<WeeklyProgress>,
    lineColor: Color
) {
    val maxTime = weeklyProgress.maxOfOrNull { it.studyTimeMinutes }?.toFloat() ?: 1f
    val chartWidth = size.width * 0.8f
    val chartHeight = size.height * 0.8f
    val startX = size.width * 0.1f
    val startY = size.height * 0.1f
    
    val points = weeklyProgress.mapIndexed { index, progress ->
        val stepSize = chartWidth / (weeklyProgress.size - 1).toFloat()
        val x = startX + (index.toFloat() * stepSize)
        val progressRatio = progress.studyTimeMinutes.toFloat() / maxTime
        val y = startY + chartHeight - (progressRatio * chartHeight)
        Offset(x, y)
    }
    
    // Dessiner la ligne
    if (points.size > 1) {
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
    
    // Dessiner les points
    points.forEach { point ->
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = point
        )
    }
}

/**
 * Composant de graphique circulaire pour la répartition du temps d'étude
 */
@Composable
fun StudyTimePieChart(
    subjectStats: List<SubjectStats>,
    modifier: Modifier = Modifier
) {
    if (subjectStats.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune donnée de temps d'étude",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Temps d'Étude par Matière",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val errorColor = MaterialTheme.colorScheme.error
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .weight(1f)
                ) {
                    drawPieChart(
                        subjectStats = subjectStats,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        tertiaryColor = tertiaryColor,
                        errorColor = errorColor
                    )
                }
                
                // Légende
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectStats.forEachIndexed { index, stats ->
                        val color = getSubjectColor(index, primaryColor, secondaryColor, tertiaryColor, errorColor)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stats.subject.take(15) + if (stats.subject.length > 15) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dessine le graphique circulaire
 */
private fun DrawScope.drawPieChart(
    subjectStats: List<SubjectStats>,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    errorColor: Color
) {
    val totalTime = subjectStats.sumOf { it.totalTimeMinutes }.toFloat()
    if (totalTime == 0f) return
    
    val center = Offset(size.width / 2, size.height / 2)
    val radius = minOf(size.width, size.height) / 2 * 0.8f
    
    var startAngle = -90f // Commencer en haut
    
    subjectStats.forEachIndexed { index, stats ->
        val sweepAngle = (stats.totalTimeMinutes / totalTime) * 360f
        val color = getSubjectColor(index, primaryColor, secondaryColor, tertiaryColor, errorColor)
        
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        
        startAngle += sweepAngle
    }
}

/**
 * Obtient une couleur pour une matière
 */
private fun getSubjectColor(index: Int, primaryColor: Color, secondaryColor: Color, tertiaryColor: Color, errorColor: Color): Color {
    val colors = listOf(
        primaryColor,
        tertiaryColor,
        errorColor,
        secondaryColor,
        Color(0xFFFF9800), // Orange - garde sa couleur pour la distinction
        Color(0xFF00BCD4), // Cyan - garde sa couleur pour la distinction
        Color(0xFF795548), // Marron - garde sa couleur pour la distinction
        Color(0xFF607D8B)  // Bleu gris - garde sa couleur pour la distinction
    )
    return colors[index % colors.size]
}

/**
 * Composant de carte de statistique simple
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composant de barre de progression
 */
@Composable
fun ProgressBar(
    progress: Float, // 0.0 à 1.0
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}
