package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository

/**
 * The playlist URL is the identity every per-playlist KV setting is keyed
 * by. Editing the URL therefore moves every stored value from the old-URL
 * key to the new-URL key: the exact-suffix keys (enabled / User-Agent /
 * update interval / update on start) and the group-enabled family, whose
 * keys carry a group name after the URL.
 */
object PlaylistKeyMigration {
    /** One key rename: the stored [value] moves from [from] to [to]. */
    data class Move(
        val from: String,
        val to: String,
        val value: String,
    )

    private val exactPrefixes =
        listOf(
            "playlist_enabled:",
            "playlist_user_agent:",
            "playlist_update_interval:",
            "playlist_update_on_start:",
        )

    private const val GROUP_PREFIX = "playlist_group_enabled:"

    /** The renames a URL edit requires, computed from a raw KV [snapshot]. */
    fun moves(
        snapshot: Map<String, String>,
        oldUrl: String,
        newUrl: String,
    ): List<Move> = snapshot.mapNotNull { (key, value) -> moveOf(key, value, oldUrl, newUrl) }

    /** Applies the renames to [settings]: write the new key, drop the old. */
    fun apply(
        settings: SettingsRepository,
        oldUrl: String,
        newUrl: String,
    ) {
        moves(settings.snapshot(), oldUrl, newUrl).forEach { move ->
            settings.writeRaw(move.to, move.value)
            settings.removeRaw(move.from)
        }
    }

    private fun moveOf(
        key: String,
        value: String,
        oldUrl: String,
        newUrl: String,
    ): Move? {
        val exactPrefix = exactPrefixes.firstOrNull { key == it + oldUrl }
        val oldGroupPrefix = "$GROUP_PREFIX$oldUrl:"
        return when {
            exactPrefix != null -> Move(from = key, to = exactPrefix + newUrl, value = value)
            key.startsWith(oldGroupPrefix) ->
                Move(from = key, to = "$GROUP_PREFIX$newUrl:${key.removePrefix(oldGroupPrefix)}", value = value)
            else -> null
        }
    }
}
