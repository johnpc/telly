package com.johncorser.telly.features.catchup

/**
 * `catchup="flussonic"`: the standard Flussonic/Media-Server archive rewrite
 * of the live URL's last path segment (query string preserved):
 * `…/mono.m3u8` → `…/archive-{start}-{duration}.m3u8`,
 * `…/video.m3u8` → `…/video-{start}-{duration}.m3u8`,
 * `…/mpegts`     → `…/archive-{start}-{duration}.ts`.
 */
object FlussonicUrl {
    fun build(
        streamUrl: String,
        startMs: Long,
        endMs: Long,
    ): String {
        val start = CatchupTemplate.seconds(startMs)
        val duration = CatchupTemplate.seconds(endMs - startMs)
        val base = streamUrl.substringBefore('?')
        val query = streamUrl.removePrefix(base)
        val head = base.substringBeforeLast('/')
        val archive =
            when (base.substringAfterLast('/')) {
                "mpegts" -> "archive-$start-$duration.ts"
                "video.m3u8" -> "video-$start-$duration.m3u8"
                else -> "archive-$start-$duration.m3u8"
            }
        return "$head/$archive$query"
    }
}
