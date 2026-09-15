package com.johncorser.telly.features.player.tracks

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Media3-backed [TrackFacade]: mirrors the player's `currentTracks` into a
 * [TrackSnapshot] and applies picks as `TrackSelectionOverride`s (Auto
 * clears the video override; CC "Off" disables the text type). The audio
 * offset lands in the shared [AudioOffsetHolder] the engine's audio sink
 * reads — an engine built without the offset renderers keeps the picker
 * state but the shift never reaches an audio clock.
 */
class ExoTrackFacade(
    private val player: Player,
    private val offsets: AudioOffsetHolder = AudioOffsetHolder(),
) : TrackFacade,
    Player.Listener {
    private val mutableSnapshot = MutableStateFlow(TrackSnapshot())
    private val mutableOffset = MutableStateFlow(0L)
    private var handles: Map<String, TrackHandle> = emptyMap()
    private var videoOverrideId: String? = null
    private var videoOverride: TrackHandle? = null

    override val snapshot: StateFlow<TrackSnapshot> = mutableSnapshot.asStateFlow()
    override val audioOffsetMs: StateFlow<Long> = mutableOffset.asStateFlow()

    init {
        player.addListener(this)
    }

    override fun onTracksChanged(tracks: Tracks) {
        val mapped = ExoTrackMapper.map(tracks)
        handles = mapped.handles
        // A zap replaces the groups (ids like "0:0" recur across streams, so
        // the overridden group itself is compared): a stale override must not
        // stay "checked" while the new stream really plays adaptively.
        if (videoOverrideId != null && handles[videoOverrideId] != videoOverride) selectNoVideoOverride()
        mutableSnapshot.value = mapped.snapshot.copy(videoOverrideId = videoOverrideId)
    }

    override fun selectVideo(id: String?) {
        videoOverrideId = id?.takeIf { it in handles }
        videoOverride = videoOverrideId?.let(handles::get)
        apply { withOverride(C.TRACK_TYPE_VIDEO, videoOverrideId) }
        mutableSnapshot.update { it.copy(videoOverrideId = videoOverrideId) }
    }

    private fun selectNoVideoOverride() {
        videoOverrideId = null
        videoOverride = null
    }

    override fun selectAudio(id: String) {
        if (id !in handles) return
        apply { withOverride(C.TRACK_TYPE_AUDIO, id) }
        mutableSnapshot.update { it.copy(selectedAudioId = id) }
    }

    override fun selectText(id: String?) {
        val valid = id?.takeIf { it in handles }
        apply { setTrackTypeDisabled(C.TRACK_TYPE_TEXT, valid == null).withOverride(C.TRACK_TYPE_TEXT, valid) }
        mutableSnapshot.update { it.copy(selectedTextId = valid) }
    }

    override fun setAudioOffsetMs(ms: Long) {
        offsets.offsetMs = ms
        mutableOffset.value = ms
    }

    private fun apply(edit: TrackSelectionParameters.Builder.() -> TrackSelectionParameters.Builder) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().edit().build()
    }

    private fun TrackSelectionParameters.Builder.withOverride(
        type: Int,
        id: String?,
    ): TrackSelectionParameters.Builder {
        val handle = id?.let(handles::get) ?: return clearOverridesOfType(type)
        return setOverrideForType(TrackSelectionOverride(handle.group, handle.index))
    }
}

/** Captions default Off, like the reference; the CC picker re-enables them. */
fun Player.disableCaptionsByDefault() {
    trackSelectionParameters =
        trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
}
