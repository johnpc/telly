package com.johncorser.telly.features.player.tracks

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OffsetRenderersFactoryTest {
    @Test
    fun `builds the full renderer set with the wrapped audio sink installed`() {
        val factory =
            OffsetRenderersFactory(ApplicationProvider.getApplicationContext<Context>(), AudioOffsetHolder())

        val renderers =
            factory.createRenderers(
                Handler(Looper.getMainLooper()),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
            )

        assertTrue(renderers.any { it.trackType == C.TRACK_TYPE_VIDEO })
        assertTrue(renderers.any { it.trackType == C.TRACK_TYPE_AUDIO })
        assertTrue(renderers.any { it.trackType == C.TRACK_TYPE_TEXT })
    }
}
