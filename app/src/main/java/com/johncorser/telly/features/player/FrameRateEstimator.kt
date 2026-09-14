package com.johncorser.telly.features.player

/**
 * Derives the stream frame rate from video-frame presentation timestamps —
 * raw TS streams don't carry it in the container format, but TiviMate still
 * shows "25 FPS" (round3 item 7). Pure logic: feed [onFrame] consecutive
 * presentation times, get an estimate once enough consistent deltas exist.
 */
class FrameRateEstimator(
    private val samples: Int = DEFAULT_SAMPLES,
) {
    private val deltasUs = ArrayDeque<Long>()
    private var lastUs: Long? = null

    /** Returns the estimated fps once [samples] deltas agree, else null. */
    fun onFrame(presentationTimeUs: Long): Float? {
        val previous = lastUs
        lastUs = presentationTimeUs
        previous?.let { record(presentationTimeUs - it) }
        return if (deltasUs.size >= samples) MICROS_PER_SECOND / median() else null
    }

    private fun record(deltaUs: Long) {
        if (deltaUs <= 0) {
            deltasUs.clear()
        } else {
            deltasUs.addLast(deltaUs)
            if (deltasUs.size > samples) deltasUs.removeFirst()
        }
    }

    /** The median delta shrugs off single late/early frames. */
    private fun median(): Float {
        val sorted = deltasUs.sorted()
        return sorted[sorted.size / 2].toFloat()
    }

    companion object {
        private const val DEFAULT_SAMPLES = 12
        private const val MICROS_PER_SECOND = 1_000_000f
    }
}
