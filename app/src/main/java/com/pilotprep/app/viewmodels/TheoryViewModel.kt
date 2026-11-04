package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pilotprep.app.data.repository.TheoryRepository
import com.pilotprep.app.data.model.Lesson

@HiltViewModel
class TheoryViewModel @Inject constructor(
    private val repository: TheoryRepository
) : ViewModel() {

    private val _lessons: MutableStateFlow<List<Lesson>> = MutableStateFlow(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _selectedLesson: MutableStateFlow<Lesson?> = MutableStateFlow(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Alias reconnus pour les catégories "théorie"
    private val theoryCategoryAliases = setOf(
        "summary", "resume", "résumé", "resumé", "need_to_know", "need-to-know", "all"
    )

    fun loadLessons(
        license: String,
        subject: String,
        forceRefresh: Boolean = false
    ) {
        if (_lessons.value.isNotEmpty() && !forceRefresh) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val isTheoryCategory = license.lowercase() in theoryCategoryAliases

                val loadedLessons = if (isTheoryCategory) {
                    if (license.lowercase() == "need_to_know") {
                        // Pour Need to Know, on retourne une liste vide car c'est géré par NeedToKnowViewModel
                        println("DEBUG: Need to Know détecté - redirection vers NeedToKnowViewModel")
                        emptyList()
                    } else if (license.lowercase() == "all") {
                        // Pour récupérer toutes les leçons
                        repository.getAllLessonsForDebugging()
                    } else {
                        // Pour les autres catégories de théorie (résumé, etc.)
                        repository.getLessonsForTheoryCategory(license, subject)
                    }
                } else {
                    // Sinon, on considère 'license' comme une véritable licence (ex: "PPL")
                    repository.getLessonsForSubject(license, subject)
                }

                _lessons.value = loadedLessons
                if (_lessons.value.isEmpty() && license.lowercase() != "need_to_know") {
                    _error.value = "Aucune leçon trouvée pour ce sujet"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des leçons: ${e.message}"
                _lessons.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findLessonById(id: String) {
        val localLesson = _lessons.value.find { it.id == id }
        if (localLesson != null) {
            _selectedLesson.value = localLesson
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val remoteLesson = repository.getLessonById(id)
                if (remoteLesson != null) {
                    _selectedLesson.value = remoteLesson
                } else {
                    _error.value = "Leçon introuvable avec l'ID: $id"
                    _selectedLesson.value = null
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement de la leçon: ${e.message}"
                _selectedLesson.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun debugAllLessons() {
        viewModelScope.launch {
            try {
                repository.getAllLessonsForDebugging()
            } catch (e: Exception) {
                println("DEBUG: Erreur lors du débogage: ${e.message}")
            }
        }
    }
}