package com.johncorser.telly.features.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceSearchTest {
    private val notices = mutableListOf<String>()

    @Test
    fun `start launches the recognizer and stays quiet on success`() {
        var launched = 0
        val voice =
            VoiceSearch(
                launch = {
                    launched += 1
                    true
                },
                notify = notices::add,
            )

        voice.start()

        assertEquals(1, launched)
        assertEquals(emptyList<String>(), notices)
    }

    @Test
    fun `a device without a recognizer degrades to the honest toast`() {
        val voice = VoiceSearch(launch = { false }, notify = notices::add)

        voice.start()

        assertEquals(listOf(VoiceSearch.UNAVAILABLE_MESSAGE), notices)
    }

    @Test
    fun `the default hook has no recognizer`() {
        VoiceSearch(notify = notices::add).start()

        assertEquals(listOf(VoiceSearch.UNAVAILABLE_MESSAGE), notices)
    }

    @Test
    fun `a transcript is consumed exactly once and blanks are dropped`() {
        val voice = VoiceSearch()

        voice.onResult("  ")
        voice.onResult(null)
        assertNull(voice.transcripts.value)

        voice.onResult("morning news")
        assertEquals("morning news", voice.transcripts.value)

        assertEquals("morning news", voice.consume())
        assertNull(voice.transcripts.value)
        assertNull(voice.consume())
    }
}
