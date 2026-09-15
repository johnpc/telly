package com.johncorser.telly.features.player.tracks

import androidx.media3.common.C
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks

/** What a `TrackSelectionOverride` needs to pick one mapped track. */
internal data class TrackHandle(
    val group: TrackGroup,
    val index: Int,
)

/** [TrackSnapshot] for the UI plus the selection handles keyed by track id. */
internal data class MappedTracks(
    val snapshot: TrackSnapshot,
    val handles: Map<String, TrackHandle>,
)

/** Pure `Tracks` → snapshot mapping, kept out of the facade so it JVM-tests flat. */
internal object ExoTrackMapper {
    fun map(tracks: Tracks): MappedTracks {
        val state = MapperState()
        tracks.groups.forEachIndexed { groupIndex, group ->
            for (trackIndex in 0 until group.length) {
                if (group.isTrackSupported(trackIndex)) state.add(group, groupIndex, trackIndex)
            }
        }
        return MappedTracks(state.snapshot(), state.handles)
    }

    private class MapperState {
        val handles = mutableMapOf<String, TrackHandle>()
        private val videos = mutableListOf<VideoTrack>()
        private val audios = mutableListOf<AudioTrack>()
        private val texts = mutableListOf<TextTrack>()
        private var audioId: String? = null
        private var textId: String? = null

        fun add(
            group: Tracks.Group,
            groupIndex: Int,
            trackIndex: Int,
        ) {
            val id = "$groupIndex:$trackIndex"
            val format = group.getTrackFormat(trackIndex)
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> videos += VideoTrack(id, format.width, format.height, format.bitrate)
                C.TRACK_TYPE_AUDIO -> audios += AudioTrack(id, format.language, format.channelCount)
                C.TRACK_TYPE_TEXT -> texts += TextTrack(id, format.language)
                else -> return
            }
            handles[id] = TrackHandle(group.mediaTrackGroup, trackIndex)
            if (group.isTrackSelected(trackIndex)) select(group.type, id)
        }

        private fun select(
            type: Int,
            id: String,
        ) {
            if (type == C.TRACK_TYPE_AUDIO) audioId = id
            if (type == C.TRACK_TYPE_TEXT) textId = id
        }

        fun snapshot(): TrackSnapshot =
            TrackSnapshot(videos, audios, texts, selectedAudioId = audioId, selectedTextId = textId)
    }
}
