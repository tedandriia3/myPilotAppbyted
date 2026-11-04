package com.pilotprep.app.util

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*
import javax.inject.Inject

class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val mainScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionJob: Job? = null
    data class PlaybackState(
        val isPlaying: Boolean = false,
        val currentAudioId: String? = null,
        val position: Long = 0L,
        val duration: Long = 0L
    )

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private var currentAudioId: String? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { updatePlaybackState() }
            override fun onPlaybackStateChanged(state: Int) { updatePlaybackState() }
        })
        exoPlayer.playWhenReady = false
    }

    private fun updatePlaybackState() {
        _playbackState.value = PlaybackState(
            isPlaying = exoPlayer.isPlaying,
            currentAudioId = currentAudioId,
            position = exoPlayer.currentPosition,
            duration = exoPlayer.duration
        )
    }

    fun prepare(audioUrl: String, audioId: String) {
        if (currentAudioId != audioId) {
            exoPlayer.setMediaItem(
                MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaId(audioId)
                    .build()
            )
            exoPlayer.prepare()
            currentAudioId = audioId
            updatePlaybackState()
        }
    }

    fun play(audioUrl: String, audioId: String, title: String = "Audio") {
        if (currentAudioId != audioId) {
            exoPlayer.setMediaItem(
                MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaId(audioId)
                    .build()
            )
            exoPlayer.prepare()
            currentAudioId = audioId
        }
        exoPlayer.playWhenReady = true
        startPositionUpdates()
    }

    fun playCurrent() {
        exoPlayer.playWhenReady = true
    }

    fun pause() { exoPlayer.pause(); stopPositionUpdates() }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        updatePlaybackState()
    }

    fun stop() { exoPlayer.stop(); stopPositionUpdates() }

    fun release() { stopPositionUpdates(); exoPlayer.release() }

    // No audio focus, no persistence, no media session

    private fun startPositionUpdates() {
        if (positionJob?.isActive == true) return
        positionJob = mainScope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
        updatePlaybackState()
    }
}
