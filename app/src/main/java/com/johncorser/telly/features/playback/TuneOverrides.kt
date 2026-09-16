package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.player.external.ExternalPlayer
import com.johncorser.telly.features.player.external.ExternalPlayerSetting
import com.johncorser.telly.features.playlist.db.ChannelEntity

// The per-channel Channel-options overrides a tune applies (kept out of
// TuneController for its file gate).

/** The channel's decoder picks win for this tune; null = the global base. */
internal fun PlayerEngine.applyDecoderOverridesOf(channel: ChannelEntity) =
    decoders.overrideWith(channel.overrides.audioDecoder, channel.overrides.videoDecoder)

/** External hand-off: the channel's toggle wins over the global setting. */
internal fun ExternalPlayer.handsOff(channel: ChannelEntity): Boolean =
    maybeLaunch(
        channel.source.streamUrl,
        ExternalPlayerSetting.overrideOf(channel.overrides.externalPlayer),
    )
