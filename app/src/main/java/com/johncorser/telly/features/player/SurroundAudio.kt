package com.johncorser.telly.features.player

import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks

/**
 * "Select surround audio track by default": prefers the supported audio
 * track with the most channels. Returns null when the current selection
 * already has as many channels (so applying the returned override never
 * loops through onTracksChanged) or when no audio track beats it.
 */
object SurroundAudio {
    fun pick(tracks: Tracks): TrackSelectionOverride? {
        val audio = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
        val best = audio.flatMap(::supportedTracks).maxByOrNull { it.channels }
        val selected = audio.flatMap(::selectedChannels).maxOrNull() ?: 0
        return best
            ?.takeIf { it.channels > selected }
            ?.let { TrackSelectionOverride(it.group.mediaTrackGroup, it.index) }
    }

    private data class Candidate(
        val group: Tracks.Group,
        val index: Int,
        val channels: Int,
    )

    private fun supportedTracks(group: Tracks.Group): List<Candidate> =
        (0 until group.length)
            .filter(group::isTrackSupported)
            .map { Candidate(group, it, group.getTrackFormat(it).channelCount) }

    private fun selectedChannels(group: Tracks.Group): List<Int> =
        (0 until group.length)
            .filter(group::isTrackSelected)
            .map { group.getTrackFormat(it).channelCount }
}
