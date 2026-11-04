package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Lesson
import com.pilotprep.app.data.repository.LessonDataCleaner
import com.pilotprep.app.data.repository.TheoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataCleanupViewModel @Inject constructor(
    private val lessonDataCleaner: LessonDataCleaner,
    private val theoryRepository: TheoryRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadLessons()
    }

    fun loadLessons() {
        viewModelScope.launch {
            try {
                _lessons.value = theoryRepository.getAllLessonsForDebugging()
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des leçons: ${e.message}"
            }
        }
    }

    fun cleanData() {
        cleanData(forceUpdate = false)
    }

    fun cleanData(forceUpdate: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _isCompleted.value = false
                
                lessonDataCleaner.cleanLessonData(forceUpdate)
                
                _isCompleted.value = true
                _isLoading.value = false
                
                // Recharger les leçons après nettoyage
                loadLessons()
                
            } catch (e: Exception) {
                _error.value = "Erreur lors du nettoyage: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
