package com.johncorser.telly.features.catchup

/**
 * The heart of the slice: channel attributes + programme window + "now" →
 * a playable catch-up URL. One tiny builder per catch-up type; null when
 * the type cannot produce a URL for this channel.
 */
object CatchupUrlBuilder {
    fun build(
        streamUrl: String,
        attributes: CatchupAttributes,
        startMs: Long,
        endMs: Long,
        nowMs: Long,
    ): String? =
        when (attributes.type) {
            CatchupType.DEFAULT -> attributes.source?.let { CatchupTemplate.expand(it, startMs, endMs, nowMs) }
            CatchupType.APPEND ->
                attributes.source?.let { streamUrl + CatchupTemplate.expand(it, startMs, endMs, nowMs) }
            CatchupType.SHIFT -> ShiftUrl.build(streamUrl, startMs, nowMs)
            CatchupType.FLUSSONIC -> FlussonicUrl.build(streamUrl, startMs, endMs)
            CatchupType.XC -> XtreamUrl.build(streamUrl, startMs, endMs)
        }
}
