package com.pilotprep.app.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pilotprep.app.data.model.*
import com.pilotprep.app.ui.components.stats.*
import com.pilotprep.app.viewmodels.ProgressDashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(
    navController: NavController,
    userId: String,
    viewModel: ProgressDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subjectStats by viewModel.subjectStats.collectAsStateWithLifecycle()
    val weeklyProgress by viewModel.weeklyProgress.collectAsStateWithLifecycle()
    val userGoals by viewModel.userGoals.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadUserStats(userId)
    }
    
    // Rafraîchir les données quand l'utilisateur revient sur l'écran
    LaunchedEffect(Unit) {
        viewModel.loadUserStats(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Progrès") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats(userId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                uiState.error != null -> {
                    val errorMessage = uiState.error ?: "Erreur inconnue"
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Erreur",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                uiState.userStats != null -> {
                    // === STATISTIQUES GÉNÉRALES ===
                    item {
                        Text(
                            text = "Vue d'ensemble",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Quiz Complétés",
                                value = uiState.userStats!!.totalQuizzesCompleted.toString(),
                                subtitle = "Total",
                                icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Score Moyen",
                                value = "${uiState.userStats!!.averageScore.toInt()}%",
                                subtitle = "Performance",
                                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Temps d'Étude",
                                value = formatStudyTime(uiState.userStats!!.totalStudyTimeMinutes),
                                subtitle = "Total",
                                icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Série Actuelle",
                                value = "${uiState.userStats!!.currentStreak} jours",
                                subtitle = "Consécutifs",
                                icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // === OBJECTIFS ===
                    if (userGoals.isNotEmpty()) {
                        item {
                            Text(
                                text = "Mes Objectifs",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(userGoals) { goal ->
                            GoalCard(goal = goal)
                        }
                    }
                    
                    // === RÉCOMPENSES ===
                    val unlockedAchievements = achievements.filter { it.isUnlocked }
                    if (unlockedAchievements.isNotEmpty()) {
                        item {
                            Text(
                                text = "Mes Récompenses",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(unlockedAchievements) { achievement ->
                            AchievementCard(achievement = achievement)
                        }
                    }
                    
                    // === GRAPHIQUES ===
                    if (subjectStats.isNotEmpty()) {
                        item {
                            Text(
                                text = "Analyses Détaillées",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            SubjectScoresBarChart(subjectStats = subjectStats)
                        }
                        
                        item {
                            StudyTimePieChart(subjectStats = subjectStats)
                        }
                    }
                    
                    if (weeklyProgress.isNotEmpty()) {
                        item {
                            WeeklyProgressLineChart(weeklyProgress = weeklyProgress)
                        }
                    }
                    
                    // === STATISTIQUES PAR MATIÈRE ===
                    if (subjectStats.isNotEmpty()) {
                        item {
                            Text(
                                text = "Détail par Matière",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(subjectStats) { stats ->
                            SubjectStatsCard(subjectStats = stats)
                        }
                    }
                }

                else -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aucune donnée pour l'instant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lancez un quiz ou un examen pour commencer à suivre votre progression.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Retour à l'accueil")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: UserGoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (goal.isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Complété",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val progress = if (goal.targetValue > 0) {
                goal.currentValue.toFloat() / goal.targetValue
            } else 0f
            
            ProgressBar(
                progress = progress,
                color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${goal.currentValue} / ${goal.targetValue}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementCard(achievement: UserAchievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "Récompense",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (achievement.unlockedAt != null) {
                    Text(
                        text = "Débloqué le ${formatDate(achievement.unlockedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "+${achievement.points} pts",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SubjectStatsCard(subjectStats: SubjectStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subjectStats.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subjectStats.license,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${subjectStats.averageScore.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = subjectStats.quizzesCompleted.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quiz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = subjectStats.examsCompleted.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Examens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatStudyTime(subjectStats.totalTimeMinutes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Temps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatStudyTime(minutes: Long): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return when {
        hours > 0 -> "${hours}h ${remainingMinutes}min"
        else -> "${remainingMinutes}min"
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}
