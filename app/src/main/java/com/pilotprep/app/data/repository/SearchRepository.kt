package com.pilotprep.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.model.Lesson
import com.pilotprep.app.data.model.Podcast
import com.pilotprep.app.ui.screens.search.*
import com.pilotprep.app.util.ErrorHandler
import com.pilotprep.app.util.ErrorSeverity
import com.pilotprep.app.util.NetworkRetry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val errorHandler: ErrorHandler
) {
    suspend fun searchInAllCollections(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = query.lowercase().trim()
        val searchTerms = normalizedQuery.split(" ").filter { it.isNotBlank() }

        return try {
            coroutineScope {
                // Recherche parallèle dans les trois collections
                val lessonsDeferred = async { searchInLessons(normalizedQuery, searchTerms) }
                val podcastsDeferred = async { searchInPodcasts(normalizedQuery, searchTerms) }
                val aircraftDeferred = async { searchInAircraft(normalizedQuery, searchTerms) }

                // Attendre tous les résultats
                val allResults = awaitAll(lessonsDeferred, podcastsDeferred, aircraftDeferred)
                    .flatten()
                    .sortedByDescending { it.relevanceScore }

                allResults
            }
        } catch (e: Exception) {
            errorHandler.logError(e, "SearchRepository#searchInAllCollections", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    private suspend fun searchInLessons(query: String, searchTerms: List<String>): List<SearchResult> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("lessons").get().await())
            }.getOrThrow()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val lesson = doc.toObject<Lesson>()?.copy(id = doc.id) ?: return@mapNotNull null
                    
                    // Rechercher dans les champs texte
                    val titleMatch = lesson.title.lowercase().contains(query)
                    val subjectMatch = lesson.subject.lowercase().contains(query)
                    val categoryMatch = lesson.category.lowercase().contains(query)
                    
                    // Rechercher dans le contenu si c'est une String
                    val contentMatch = when (lesson.content) {
                        is String -> lesson.content.lowercase().contains(query)
                        is List<*> -> lesson.content.toString().lowercase().contains(query)
                        is Map<*, *> -> lesson.content.toString().lowercase().contains(query)
                        else -> false
                    }

                    // Calculer le score de pertinence
                    val relevanceScore = calculateRelevanceScore(
                        titleMatch, subjectMatch, categoryMatch, contentMatch,
                        lesson.title, lesson.subject, searchTerms
                    )

                    // Créer le résultat seulement si une correspondance est trouvée
                    if (titleMatch || subjectMatch || categoryMatch || contentMatch) {
                        val excerpt = when {
                            lesson.content is String -> {
                                val content = lesson.content as String
                                extractExcerpt(content, query, 150)
                            }
                            else -> "Leçon sur ${lesson.subject}"
                        }

                        SearchResult(
                            id = lesson.id,
                            title = lesson.title,
                            excerpt = excerpt,
                            content = lesson.content?.toString() ?: "",
                            category = mapSubjectToSearchCategory(lesson.subject),
                            difficultyLevel = mapCategoryToDifficulty(lesson.category),
                            mediaType = MediaType.TEXT,
                            lastUpdated = "Récemment",
                            relevanceScore = relevanceScore,
                            isFavorite = false,
                            tags = listOf(lesson.subject, lesson.category),
                            source = "Théorie du Vol"
                        )
                    } else null
                } catch (e: Exception) {
                    errorHandler.logError(e, "SearchRepository#searchInLessons", ErrorSeverity.LOW)
                    null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            errorHandler.logError(e, "SearchRepository#searchInLessons", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    private suspend fun searchInPodcasts(query: String, searchTerms: List<String>): List<SearchResult> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("podcasts").get().await())
            }.getOrThrow()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val podcast = doc.toObject(Podcast::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                    
                    val titleMatch = podcast.title.lowercase().contains(query)
                    val descriptionMatch = podcast.description.lowercase().contains(query)

                    if (titleMatch || descriptionMatch) {
                        val relevanceScore = calculateRelevanceScore(
                            titleMatch, descriptionMatch, false, false,
                            podcast.title, podcast.description, searchTerms
                        )

                        SearchResult(
                            id = podcast.id,
                            title = podcast.title,
                            excerpt = extractExcerpt(podcast.description, query, 150),
                            content = podcast.description,
                            category = SearchCategory.VOICES,
                            difficultyLevel = DifficultyLevel.INTERMEDIATE,
                            mediaType = MediaType.AUDIO,
                            lastUpdated = "Récemment",
                            relevanceScore = relevanceScore,
                            isFavorite = false,
                            tags = emptyList(),
                            source = "Voix du Ciel"
                        )
                    } else null
                } catch (e: Exception) {
                    errorHandler.logError(e, "SearchRepository#searchInPodcasts", ErrorSeverity.LOW)
                    null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            errorHandler.logError(e, "SearchRepository#searchInPodcasts", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    private suspend fun searchInAircraft(query: String, searchTerms: List<String>): List<SearchResult> {
        return try {
            val snapshot = NetworkRetry.retryWithBackoff(maxRetries = 3, initialDelay = 800L) {
                Result.success(db.collection("aircraft").get().await())
            }.getOrThrow()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val aircraft = doc.toObject(Aircraft::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                    
                    val nameMatch = aircraft.name.lowercase().contains(query)
                    val descriptionMatch = aircraft.description.lowercase().contains(query)
                    val categoryMatch = aircraft.category.lowercase().contains(query)
                    val yearMatch = aircraft.year.contains(query)

                    if (nameMatch || descriptionMatch || categoryMatch || yearMatch) {
                        val relevanceScore = calculateRelevanceScore(
                            nameMatch, descriptionMatch, categoryMatch, false,
                            aircraft.name, aircraft.description, searchTerms
                        )

                        SearchResult(
                            id = aircraft.id,
                            title = aircraft.name,
                            excerpt = extractExcerpt(aircraft.description, query, 150),
                            content = aircraft.description,
                            category = SearchCategory.HERITAGE,
                            difficultyLevel = DifficultyLevel.BEGINNER,
                            mediaType = MediaType.TEXT,
                            lastUpdated = "Récemment",
                            relevanceScore = relevanceScore,
                            isFavorite = false,
                            tags = listOf(aircraft.category, aircraft.year),
                            source = "Patrimoine du Ciel"
                        )
                    } else null
                } catch (e: Exception) {
                    errorHandler.logError(e, "SearchRepository#searchInAircraft", ErrorSeverity.LOW)
                    null
                }
            }.filterNotNull()
        } catch (e: Exception) {
            errorHandler.logError(e, "SearchRepository#searchInAircraft", ErrorSeverity.MEDIUM)
            emptyList()
        }
    }

    private fun calculateRelevanceScore(
        titleMatch: Boolean,
        field1Match: Boolean,
        field2Match: Boolean,
        contentMatch: Boolean,
        title: String,
        field1: String,
        searchTerms: List<String>
    ): Float {
        var score = 0f

        // Score basé sur les correspondances
        if (titleMatch) score += 0.5f
        if (field1Match) score += 0.3f
        if (field2Match) score += 0.2f
        if (contentMatch) score += 0.1f

        // Bonus si le titre commence par le terme de recherche
        if (title.lowercase().startsWith(searchTerms.firstOrNull() ?: "")) {
            score += 0.2f
        }

        // Bonus pour correspondance exacte
        if (title.lowercase() == searchTerms.firstOrNull()?.lowercase()) {
            score += 0.3f
        }

        // Normaliser le score entre 0 et 1
        return score.coerceIn(0f, 1f)
    }

    private fun extractExcerpt(text: String, query: String, maxLength: Int): String {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        val index = lowerText.indexOf(lowerQuery)

        return if (index >= 0) {
            val start = (index - 30).coerceAtLeast(0)
            val end = (index + query.length + maxLength).coerceAtMost(text.length)
            val excerpt = text.substring(start, end)
            if (start > 0) "..." + excerpt else excerpt + if (end < text.length) "..." else ""
        } else {
            text.take(maxLength) + if (text.length > maxLength) "..." else ""
        }
    }

    private fun mapSubjectToSearchCategory(subject: String): SearchCategory {
        return when (subject.lowercase()) {
            "météorologie", "météo" -> SearchCategory.METEOROLOGY
            "navigation" -> SearchCategory.NAVIGATION
            "réglementation", "règles de l'air", "atc" -> SearchCategory.REGULATION
            "procédures", "approche", "atterrissage" -> SearchCategory.PROCEDURES
            "systèmes", "cellule", "moteur" -> SearchCategory.AIRCRAFT_SYSTEMS
            "facteurs humains" -> SearchCategory.HUMAN_FACTORS
            "aérodynamique", "théorie du vol" -> SearchCategory.AERODYNAMICS
            "urgence", "panne" -> SearchCategory.EMERGENCY
            "examen", "quiz" -> SearchCategory.EXAM
            else -> SearchCategory.THEORY
        }
    }

    private fun mapCategoryToDifficulty(category: String): DifficultyLevel {
        return when (category.lowercase()) {
            "ppl", "élève", "débutant" -> DifficultyLevel.BEGINNER
            "cpl", "licence", "intermédiaire" -> DifficultyLevel.INTERMEDIATE
            "atpl", "instructeur", "avancé" -> DifficultyLevel.ADVANCED
            else -> DifficultyLevel.INTERMEDIATE
        }
    }
}







