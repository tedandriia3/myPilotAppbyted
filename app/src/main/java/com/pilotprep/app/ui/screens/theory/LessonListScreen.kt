package com.pilotprep.app.ui.screens.theory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.pilotprep.app.ui.components.MarshallerLoadingAnimation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilotprep.app.viewmodels.TheoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    navController: NavController,
    license: String,
    subject: String
) {
    val viewModel: TheoryViewModel = hiltViewModel()

    val lessons by viewModel.lessons.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(license, subject) {
        viewModel.loadLessons(license = license, subject = subject, forceRefresh = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject) }
            )
        }
    ) { paddingValues ->
        when {
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Erreur",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error ?: "")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.clearError()
                        viewModel.loadLessons(license, subject, forceRefresh = true)
                    }) {
                        Text("Réessayer")
                    }
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    MarshallerLoadingAnimation()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(lessons) { lesson ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    navController.navigate("lesson_detail/${lesson.id}")
                                }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = (lesson.title ?: "Sans titre"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val previewText = (lesson.content as? String).orEmpty()
                                if (previewText.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    val clipped = if (previewText.length > 120) previewText.take(120) + "…" else previewText
                                    Text(
                                        text = clipped,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    if (lessons.isEmpty() && !isLoading && error == null) {
                        item {
                            Text(
                                text = "Aucune leçon trouvée.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}