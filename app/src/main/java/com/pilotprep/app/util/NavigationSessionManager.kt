package com.pilotprep.app.util

import androidx.compose.ui.graphics.ImageBitmap
import com.pilotprep.app.viewmodels.NavSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationSessionManager @Inject constructor() {
    fun updatePreview(sessions: List<NavSession>, sessionId: String, bitmap: ImageBitmap): List<NavSession> {
        return sessions.map { if (it.id == sessionId) it.copy(preview = bitmap) else it }
    }

    fun addSession(sessions: List<NavSession>, startRoute: String = "home_graph"): Pair<List<NavSession>, String> {
        val newSession = NavSession(startRoute = startRoute)
        return (sessions + newSession) to newSession.id
    }

    fun closeSession(sessions: List<NavSession>, sessionId: String): List<NavSession> {
        return if (sessions.size <= 1) sessions else sessions.filter { it.id != sessionId }
    }
}
