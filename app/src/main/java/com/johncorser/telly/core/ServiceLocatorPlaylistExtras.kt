package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.player.STREAM_USER_AGENT
import com.johncorser.telly.features.playlist.GroupFilteredChannelDao
import com.johncorser.telly.features.playlist.StreamUserAgentResolver
import com.johncorser.telly.features.playlist.UserAgentPrecedence
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.settings.PlaylistRefresher
import com.johncorser.telly.features.settings.PlaylistUpdater
import com.johncorser.telly.features.settings.playlistGroupEnabledSetting
import com.johncorser.telly.features.settings.playlistUpdateIntervalSetting
import com.johncorser.telly.features.settings.playlistUpdateOnStartSetting
import com.johncorser.telly.features.settings.playlistUserAgentSetting

// Playlist-extras wiring over ServiceLocator's app-scoped singletons.

/** Per-playlist > global User-Agent precedence over the settings store. */
private fun ServiceLocator.userAgentPrecedence(context: Context): UserAgentPrecedence =
    UserAgentPrecedence(
        perPlaylist = { settingsRepository(context).get(playlistUserAgentSetting(it)) },
        global = { settingsRepository(context).get(TellySettings.USER_AGENT) },
    )

/** Playlist fetch UA: per-playlist > global > client default (null). */
fun ServiceLocator.playlistFetchUserAgentFor(context: Context): (url: String) -> String? =
    userAgentPrecedence(context)::forPlaylist

/** Stream playback UA: per-playlist > global > the default stream UA. */
fun ServiceLocator.streamUserAgentFor(context: Context): (streamUrl: String) -> String =
    StreamUserAgentResolver(
        playlistUrlFor = { database(context).playlistDao().playlistUrlForStream(it) },
        precedence = userAgentPrecedence(context),
        fallback = STREAM_USER_AGENT,
    )::resolve

/** The Manage-groups-filtered visible-channel feed every slice reads. */
fun ServiceLocator.visibleChannelDao(context: Context): ChannelDao =
    GroupFilteredChannelDao(
        delegate = database(context).channelDao(),
        playlists = database(context).playlistDao().observeAll(),
        settingsChanges = settingsRepository(context).changes,
        groupEnabled = { url, group ->
            settingsRepository(context).get(playlistGroupEnabledSetting(url, group))
        },
    )

/** Automatic playlist updates driven by the per-playlist update options. */
fun ServiceLocator.playlistRefresher(
    context: Context,
    fetchPlaylist: suspend (String) -> String,
): PlaylistRefresher =
    PlaylistRefresher(
        playlistDao = database(context).playlistDao(),
        update = PlaylistUpdater(fetchPlaylist, playlistRepository(context))::update,
        intervalHours = { settingsRepository(context).get(playlistUpdateIntervalSetting(it)) },
        updateOnStart = { settingsRepository(context).get(playlistUpdateOnStartSetting(it)) },
        clock = clock,
    )
