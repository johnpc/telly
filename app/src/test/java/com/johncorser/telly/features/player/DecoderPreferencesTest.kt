package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Test

/** Hardware/Software preference resolution + the software-first re-rank. */
class DecoderPreferencesTest {
    @Test
    fun `the base comes from the global tuning raws`() {
        val prefs = DecoderPreferences.of(PlayerTuning(audioDecoder = "Software", videoDecoder = "Hardware"))
        assertEquals(DecoderMode.SOFTWARE, prefs.audio)
        assertEquals(DecoderMode.HARDWARE, prefs.video)
    }

    @Test
    fun `per-channel overrides win and null falls back to the base`() {
        val prefs = DecoderPreferences.of(PlayerTuning(audioDecoder = "Software", videoDecoder = "Hardware"))

        prefs.overrideWith(audioRaw = "Hardware", videoRaw = "Software")
        assertEquals(DecoderMode.HARDWARE, prefs.audio)
        assertEquals(DecoderMode.SOFTWARE, prefs.video)

        // The next channel without overrides returns to the global base.
        prefs.overrideWith(audioRaw = null, videoRaw = null)
        assertEquals(DecoderMode.SOFTWARE, prefs.audio)
        assertEquals(DecoderMode.HARDWARE, prefs.video)
    }

    @Test
    fun `an unknown raw counts as no override`() {
        val prefs = DecoderPreferences()
        prefs.overrideWith(audioRaw = "Default", videoRaw = "Fast")
        assertEquals(DecoderMode.HARDWARE, prefs.audio)
        assertEquals(DecoderMode.HARDWARE, prefs.video)
    }

    @Test
    fun `codec selection is keyed by the mime type's renderer`() {
        val prefs = DecoderPreferences(baseAudio = DecoderMode.SOFTWARE, baseVideo = DecoderMode.HARDWARE)
        assertEquals(DecoderMode.SOFTWARE, prefs.forMimeType("audio/mp4a-latm"))
        assertEquals(DecoderMode.HARDWARE, prefs.forMimeType("video/avc"))
    }

    @Test
    fun `preferSoftware moves software decoders first but drops nothing`() {
        val ranked = listOf("omx.qcom.avc" to true, "c2.android.avc" to false, "omx.mtk.avc" to true)
        val reordered = SoftwareCodecOrdering.preferSoftware(ranked) { it.second }
        assertEquals(listOf("c2.android.avc", "omx.qcom.avc", "omx.mtk.avc"), reordered.map { it.first })
    }

    @Test
    fun `preferSoftware keeps the platform order when nothing is software`() {
        val ranked = listOf("a" to true, "b" to true)
        assertEquals(ranked, SoftwareCodecOrdering.preferSoftware(ranked) { it.second })
    }
}
