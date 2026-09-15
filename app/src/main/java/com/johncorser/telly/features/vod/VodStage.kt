package com.johncorser.telly.features.vod

/** Where VOD playback is: loading the item, the resume prompt, or playing. */
sealed interface VodStage {
    data object Loading : VodStage

    /** A stored position inside the resume band: offer Resume/Start over. */
    data class ResumePrompt(
        val positionMs: Long,
    ) : VodStage

    data object Playing : VodStage
}

/** The transport's clock sample: current position and known duration. */
data class VodProgress(
    val positionMs: Long,
    val durationMs: Long,
) {
    val permille: Int get() = if (durationMs <= 0) 0 else (positionMs * 1000 / durationMs).toInt().coerceIn(0, 1000)
}
