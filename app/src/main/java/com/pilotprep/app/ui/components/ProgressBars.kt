package com.pilotprep.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Barre de progression linéaire personnalisée
 */
@Composable
fun CustomProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) {
            tween(durationMillis = 500, easing = EaseInOutCubic)
        } else {
            snap()
        },
        label = "progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        // Arrière-plan
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRoundRect(
                color = backgroundColor,
                size = this.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
        
        // Progression
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val progressWidth = this.size.width * animatedProgress
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(progressWidth, this.size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
    }
}

/**
 * Barre de progression circulaire
 */
@Composable
fun CircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp,
    size: Dp = 120.dp,
    animated: Boolean = true,
    showPercentage: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) {
            tween(durationMillis = 1000, easing = EaseInOutCubic)
        } else {
            snap()
        },
        label = "circular_progress"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (size.toPx() - strokeWidthPx) / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)
            
            // Arrière-plan
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )
            
            // Progression
            val sweepAngle = 360f * animatedProgress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        if (showPercentage) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Barre de progression avec étapes
 */
@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    currentColor: Color = MaterialTheme.colorScheme.secondary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val stepNumber = step + 1
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cercle de l'étape
                val stepColor = when {
                    stepNumber < currentStep -> completedColor
                    stepNumber == currentStep -> currentColor
                    else -> backgroundColor
                }
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = stepColor,
                            radius = this.size.minDimension / 2
                        )
                    }
                    
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stepNumber <= currentStep) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Ligne de connexion (sauf pour la dernière étape)
                if (stepNumber < totalSteps) {
                    val lineColor = if (stepNumber < currentStep) {
                        completedColor
                    } else {
                        backgroundColor
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = lineColor,
                                size = this.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barre de progression avec gradient
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    ),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 12.dp,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) {
            tween(durationMillis = 800, easing = EaseInOutCubic)
        } else {
            snap()
        },
        label = "gradient_progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        // Arrière-plan
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = backgroundColor,
                size = this.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
        
        // Progression avec gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progressWidth = this.size.width * animatedProgress
            drawRoundRect(
                brush = gradient,
                topLeft = Offset.Zero,
                size = Size(progressWidth, this.size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
    }
}

/**
 * Barre de progression avec animation de pulsation
 */
@Composable
fun PulsingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
    isActive: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "pulsing_progress"
    )
    
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        // Arrière-plan
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = backgroundColor,
                size = this.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
        
        // Progression principale
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progressWidth = this.size.width * animatedProgress
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(progressWidth, this.size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
            )
        }
        
        // Effet de pulsation
        if (isActive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progressWidth = this.size.width * animatedProgress
                drawRoundRect(
                    color = color.copy(alpha = pulseAlpha),
                    topLeft = Offset.Zero,
                    size = Size(progressWidth, this.size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(height.toPx() / 2)
                )
            }
        }
    }
}

/**
 * Composable pour afficher les statistiques de progression
 */
@Composable
fun ProgressStatsCard(
    title: String,
    currentValue: Int,
    targetValue: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    showProgressBar: Boolean = true
) {
    val progress = if (targetValue > 0) currentValue.toFloat() / targetValue else 0f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentValue / $targetValue",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (showProgressBar) {
                CustomProgressBar(
                    progress = progress,
                    color = color,
                    height = 6.dp
                )
            }
        }
    }
}

