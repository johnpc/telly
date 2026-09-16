package com.johncorser.telly.features.player

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Media3-backed [PlayerEngine]. HLS, progressive and raw TS streams are all
 * handled by ExoPlayer's default media source factory (media3-exoplayer-hls
 * is on the classpath); audio focus comes from the audio attributes below.
 * The player instance is reused across channel changes, so the last frame
 * stays on the surface while the next stream tunes (round3 P0 item 2).
 */
class Media3PlayerEngine(
    val player: ExoPlayer,
    private val preferSurround: () -> Boolean = { false },
) : PlayerEngine,
    Player.Listener {
    private val mutableState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    private val mutableVideo = MutableStateFlow<VideoDetails?>(null)
    private var frameRates = FrameRateEstimator()

    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val video: StateFlow<VideoDetails?> = mutableVideo.asStateFlow()

    init {
        player.addListener(this)
        player.setVideoFrameMetadataListener { presentationTimeUs, _, _, _ ->
            frameRates.onFrame(presentationTimeUs)?.let(::onFrameRateMeasured)
        }
    }

    override fun load(streamUrl: String) {
        mutableState.value = PlayerState.Buffering
        frameRates = FrameRateEstimator()
        player.setMediaItem(MediaItem.fromUri(streamUrl))
        player.prepare()
        player.play()
    }

    override fun stop() {
        player.stop()
        mutableState.value = PlayerState.Idle
    }

    override fun release() {
        player.release()
    }

    override fun setMuted(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> mutableState.value = PlayerState.Buffering
            Player.STATE_READY -> onReady()
            else -> Unit
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        mutableState.value = PlayerState.Error(error.errorCodeName)
    }

    /**
     * "Select surround audio track by default": once the stream's tracks are
     * known, override onto the audio track with the most channels. The pick
     * is null when the selection is already best, so this never loops.
     */
    override fun onTracksChanged(tracks: Tracks) {
        if (!preferSurround()) return
        SurroundAudio.pick(tracks)?.let { override ->
            player.trackSelectionParameters =
                player.trackSelectionParameters
                    .buildUpon()
                    .setOverrideForType(override)
                    .build()
        }
    }

    private fun onReady() {
        mutableState.value = PlayerState.Playing
        val videoFormat = player.videoFormat
        mutableVideo.value =
            VideoDetails(
                width = videoFormat?.width ?: 0,
                height = videoFormat?.height ?: 0,
                frameRate = (videoFormat?.frameRate ?: 0f).coerceAtLeast(0f),
                audioChannels = player.audioFormat?.channelCount ?: 0,
            )
    }

    /** TS formats report no frame rate; the render-time estimate fills it in. */
    private fun onFrameRateMeasured(fps: Float) {
        mutableVideo.update { details ->
            if (details != null && details.frameRate <= 0f) details.copy(frameRate = fps) else details
        }
    }

    /** The real-device factory lives in PlayerAudioPrefs.kt ([create]). */
    companion object
}
