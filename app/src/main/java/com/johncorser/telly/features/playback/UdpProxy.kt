package com.johncorser.telly.features.playback

/**
 * TiviMate's "UDP proxy (address:port)" stream rewrite, udpxy convention:
 * `udp://@239.1.2.3:1234` becomes `http://proxy/udp/239.1.2.3:1234` (and
 * `rtp://` maps to `/rtp/`). Anything else — including a blank proxy, the
 * captured default — passes through untouched, preserving today's behavior.
 */
object UdpProxy {
    private val multicast = Regex("^(udp|rtp)://@?(.+)$", RegexOption.IGNORE_CASE)

    fun resolve(
        proxy: String,
        streamUrl: String,
    ): String {
        val target = proxy.trim().removeSuffix("/")
        val match = if (target.isEmpty()) null else multicast.matchEntire(streamUrl.trim())
        return match?.destructured?.let { (scheme, address) ->
            "http://$target/${scheme.lowercase()}/$address"
        } ?: streamUrl
    }
}
