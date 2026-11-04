package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilotprep.app.data.model.Podcast
import com.pilotprep.app.data.repository.PodcastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcastUiState(
    val isLoading: Boolean = true,
    val podcasts: List<Podcast> = emptyList(),
    val selectedPodcast: Podcast? = null
)

@HiltViewModel
class PodcastViewModel @Inject constructor(
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PodcastUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchPodcasts()
    }

    fun fetchPodcasts(forceRefresh: Boolean = false) {
        if (_uiState.value.podcasts.isNotEmpty() && !forceRefresh) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val podcasts = repository.getAllPodcasts()
            _uiState.update { it.copy(isLoading = false, podcasts = podcasts) }
        }
    }

    fun selectPodcast(podcast: Podcast?) {
        _uiState.update { it.copy(selectedPodcast = podcast) }
    }
}