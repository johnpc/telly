package com.johncorser.telly.features.player

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Live speed must stay pinned so Media3 never chases the live edge. */
@RunWith(RobolectricTestRunner::class)
class StreamMediaItemsTest {
    @Test
    fun `the stream media item carries the url and pins live playback speed to 1x`() {
        val item = streamMediaItem("http://s/1.m3u8")

        assertEquals("http://s/1.m3u8", item.localConfiguration?.uri?.toString())
        assertEquals(1f, item.liveConfiguration.minPlaybackSpeed, 0f)
        assertEquals(1f, item.liveConfiguration.maxPlaybackSpeed, 0f)
    }
}
