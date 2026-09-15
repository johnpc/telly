package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playlist.db.ChannelEntity

/** How a provider exposes already-aired programmes (community M3U values). */
enum class CatchupType {
    DEFAULT,
    APPEND,
    SHIFT,
    FLUSSONIC,
    XC,
    ;

    /** Shift/flussonic/xc rewrite the live URL, so they need no template. */
    val rewritesLiveUrl: Boolean get() = this != DEFAULT && this != APPEND

    companion object {
        fun of(raw: String?): CatchupType? =
            when (raw?.trim()?.lowercase()) {
                "default" -> DEFAULT
                "append" -> APPEND
                "shift" -> SHIFT
                "flussonic" -> FLUSSONIC
                "xc" -> XC
                else -> null
            }
    }
}

/** A channel's usable catch-up capability, resolved from its raw columns. */
data class CatchupAttributes(
    val type: CatchupType,
    val source: String?,
    val days: Int,
) {
    companion object {
        /** Horizon when `catchup-days` is absent (matches the EPG past-days default). */
        const val DEFAULT_DAYS = 7
    }
}

/**
 * Null when the channel cannot play catch-up: no recognised type (a bare
 * `catchup-source` implies "default"), or a template type without its
 * `catchup-source` template.
 */
fun ChannelEntity.catchupAttributes(): CatchupAttributes? {
    val source = catchup.catchupSource?.takeIf { it.isNotBlank() }
    val type = CatchupType.of(catchup.catchupType) ?: source?.let { CatchupType.DEFAULT }
    return type
        ?.takeIf { source != null || it.rewritesLiveUrl }
        ?.let { CatchupAttributes(it, source, catchup.catchupDays ?: CatchupAttributes.DEFAULT_DAYS) }
}
