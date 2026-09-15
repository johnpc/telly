package com.johncorser.telly.features.player.tracks

/** One selectable video rendition; [bitrate] <= 0 when the container omits it. */
data class VideoTrack(
    val id: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
)

/** One selectable audio track; [language] is null when undeclared. */
data class AudioTrack(
    val id: String,
    val language: String?,
    val channels: Int,
)

/** One selectable text/CC track; [language] is null when undeclared. */
data class TextTrack(
    val id: String,
    val language: String?,
)

/**
 * What the tuned stream offers and what is picked right now, feeding the
 * quick-bar picker dialogs. [videoOverrideId] null = adaptive "Auto";
 * [selectedTextId] null = captions off (the engine's default).
 */
data class TrackSnapshot(
    val videos: List<VideoTrack> = emptyList(),
    val audios: List<AudioTrack> = emptyList(),
    val texts: List<TextTrack> = emptyList(),
    val videoOverrideId: String? = null,
    val selectedAudioId: String? = null,
    val selectedTextId: String? = null,
)
