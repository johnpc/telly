package com.johncorser.telly.features.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingSupportTest {
    @Test
    fun `raw TS and progressive URLs are recordable`() {
        assertTrue(RecordingSupport.isRecordable("http://host/streams/news-one.ts"))
        assertTrue(RecordingSupport.isRecordable("https://host/live/12345"))
        assertTrue(RecordingSupport.isRecordable("http://host/video.mp4?token=a.m3u8"))
    }

    @Test
    fun `HLS playlists are not recordable regardless of query or case`() {
        assertFalse(RecordingSupport.isRecordable("http://host/live/master.m3u8"))
        assertFalse(RecordingSupport.isRecordable("http://host/live/master.M3U8?token=abc"))
        assertFalse(RecordingSupport.isRecordable("http://host/live/master.m3u8#frag"))
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
