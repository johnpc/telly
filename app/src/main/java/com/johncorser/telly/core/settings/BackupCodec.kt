package com.johncorser.telly.core.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One playlist inside a backup: identity only; channels are re-fetched. */
@Serializable
data class BackupPlaylist(
    val name: String,
    val url: String,
    val epgUrl: String? = null,
)

/** The backup file payload: every setting plus the playlist identities. */
@Serializable
data class BackupPayload(
    val version: Int = BackupCodec.VERSION,
    val settings: Map<String, String>,
    val playlists: List<BackupPlaylist>,
)

/** JSON codec for settings/playlist backup files. */
object BackupCodec {
    const val VERSION = 1

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    fun encode(payload: BackupPayload): String = json.encodeToString(BackupPayload.serializer(), payload)

    /** Returns null when the text is not a telly backup. */
    fun decode(text: String): BackupPayload? =
        runCatching { json.decodeFromString(BackupPayload.serializer(), text) }.getOrNull()
}
