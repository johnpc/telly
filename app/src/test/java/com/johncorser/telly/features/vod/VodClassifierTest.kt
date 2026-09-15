package com.johncorser.telly.features.vod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VodClassifierTest {
    @Test
    fun `video-file extensions classify as VOD, case-insensitively`() {
        assertTrue(VodClassifier.isVod("http://s/movie.mp4"))
        assertTrue(VodClassifier.isVod("http://s/movie.MKV"))
        assertTrue(VodClassifier.isVod("http://s/movie.avi"))
        assertTrue(VodClassifier.isVod("http://s/movie.MoV"))
    }

    @Test
    fun `query strings do not hide the extension`() {
        assertTrue(VodClassifier.isVod("http://s/movie.mp4?token=a.ts"))
    }

    @Test
    fun `streams and extension-less urls stay live channels`() {
        assertFalse(VodClassifier.isVod("http://s/live.ts"))
        assertFalse(VodClassifier.isVod("http://s/live.m3u8"))
        assertFalse(VodClassifier.isVod("http://s/live/stream"))
        assertFalse(VodClassifier.isVod("http://s/live.mp4.ts"))
    }

    @Test
    fun `item keys pair the stream url with the title`() {
        assertEquals("http://s/m.mp4|Big Buck Bunny", VodClassifier.itemKey("http://s/m.mp4", "Big Buck Bunny"))
    }
}
