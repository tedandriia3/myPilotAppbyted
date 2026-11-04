package com.pilotprep.app.viewmodels

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import com.pilotprep.app.util.NavigationSessionManager

data class NavSession(
    val id: String = UUID.randomUUID().toString(),
    val startRoute: String = "home_graph",
    var preview: ImageBitmap? = null
)

data class MainUiState(
    val sessions: List<NavSession> = listOf(NavSession()),
    val activeSessionId: String? = sessions.firstOrNull()?.id,
    val showSessionGrid: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: NavigationSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun updateSessionPreview(sessionId: String, bitmap: ImageBitmap) {
        _uiState.update { currentState ->
            val updatedSessions = sessionManager.updatePreview(currentState.sessions, sessionId, bitmap)
            currentState.copy(sessions = updatedSessions)
        }
    }

    fun onPlusClicked() {
        if (_uiState.value.sessions.size == 1) {
            _uiState.update { state ->
                val (sessions, activeId) = sessionManager.addSession(state.sessions)
                state.copy(sessions = sessions, activeSessionId = activeId)
            }
        } else {
            _uiState.update { it.copy(showSessionGrid = true) }
        }
    }

    fun onSwitchSession(sessionId: String?) {
        _uiState.update { it.copy(activeSessionId = sessionId, showSessionGrid = false) }
    }

    fun onAddNewSessionInGrid() {
        _uiState.update { state ->
            val (sessions, activeId) = sessionManager.addSession(state.sessions)
            state.copy(sessions = sessions, activeSessionId = activeId, showSessionGrid = false)
        }
    }

    fun onCloseSession(sessionId: String) {
        if (_uiState.value.sessions.size <= 1) return
        val newSessions = sessionManager.closeSession(_uiState.value.sessions, sessionId)
        _uiState.update {
            it.copy(
                sessions = newSessions,
                activeSessionId = if (it.activeSessionId == sessionId) null else it.activeSessionId
            )
        }
    }
}