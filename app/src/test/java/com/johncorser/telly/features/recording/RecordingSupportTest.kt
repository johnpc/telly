package com.johncorser.telly.features.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingSupportTest {
    @Test
    fun `raw TS and progressive URLs are not classified HLS`() {
        assertFalse(RecordingSupport.isHls("http://host/streams/news-one.ts"))
        assertFalse(RecordingSupport.isHls("https://host/live/12345"))
        assertFalse(RecordingSupport.isHls("http://host/video.mp4?token=a.m3u8"))
    }

    @Test
    fun `HLS playlists are classified HLS regardless of query or case`() {
        assertTrue(RecordingSupport.isHls("http://host/live/master.m3u8"))
        assertTrue(RecordingSupport.isHls("http://host/live/master.M3U8?token=abc"))
        assertTrue(RecordingSupport.isHls("http://host/live/master.m3u8#frag"))
    }

    @Test
    fun `the no-EPG fallback duration is three hours`() {
        assertEquals(3 * 60 * 60_000L, RecordingSupport.FALLBACK_DURATION_MS)
    }

    @Test
    fun `statuses parse by name and unknown values read as FAILED`() {
        assertEquals(RecordingStatus.RECORDING, RecordingStatus.of("RECORDING"))
        assertEquals(RecordingStatus.FAILED, RecordingStatus.of("garbage"))
        assertEquals(RecordingStatus.DONE, recordingEntity(status = RecordingStatus.DONE).recordingStatus)
    }
}
