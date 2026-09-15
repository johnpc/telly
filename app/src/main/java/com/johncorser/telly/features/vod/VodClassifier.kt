package com.johncorser.telly.features.vod

/**
 * TiviMate's playlist classification (device-verified, docs/reference):
 * entries whose stream URL carries a video-file extension are VOD "Movies";
 * streams (.ts/.m3u8/no extension) are live channels. One source of truth —
 * the wizard summary and the importer both dispatch on [isVod].
 */
object VodClassifier {
    private val vodExtensions = setOf("mp4", "mkv", "avi", "mov")

    fun isVod(streamUrl: String): Boolean =
        streamUrl
            .substringBefore('?')
            .substringAfterLast('.', missingDelimiterValue = "")
            .lowercase() in vodExtensions

    /** Refresh-stable identity for resume positions (cf. ChannelImporter). */
    fun itemKey(
        streamUrl: String,
        name: String,
    ): String = "$streamUrl|$name"
}
