package com.johncorser.telly.features.catchup

import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withCatchup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatchupAttributesTest {
    @Test
    fun `all five community type values parse case-insensitively`() {
        assertEquals(CatchupType.DEFAULT, CatchupType.of("default"))
        assertEquals(CatchupType.APPEND, CatchupType.of("APPEND"))
        assertEquals(CatchupType.SHIFT, CatchupType.of(" shift "))
        assertEquals(CatchupType.FLUSSONIC, CatchupType.of("flussonic"))
        assertEquals(CatchupType.XC, CatchupType.of("xc"))
        assertNull(CatchupType.of("timeshift"))
        assertNull(CatchupType.of(null))
    }

    @Test
    fun `a channel without catch-up attributes has no capability`() {
        assertNull(testChannel(1, 1, "News One").catchupAttributes())
    }

    @Test
    fun `a bare catchup-source implies the default type`() {
        val channel = testChannel(1, 1, "News One").withCatchup(type = null, source = "http://s/a?utc={utc}")

        val attributes = channel.catchupAttributes()

        assertEquals(CatchupType.DEFAULT, attributes?.type)
        assertEquals("http://s/a?utc={utc}", attributes?.source)
    }

    @Test
    fun `default and append types without a template are not usable`() {
        assertNull(testChannel(1, 1, "A").withCatchup(type = "default", source = null).catchupAttributes())
        assertNull(testChannel(1, 1, "A").withCatchup(type = "append", source = " ").catchupAttributes())
    }

    @Test
    fun `shift, flussonic and xc derive from the stream URL without a template`() {
        listOf("shift", "flussonic", "xc").forEach { type ->
            val channel = testChannel(1, 1, "A").withCatchup(type = type, source = null)
            assertEquals(CatchupType.of(type), channel.catchupAttributes()?.type)
        }
    }

    @Test
    fun `catchup-days defaults to seven and honors the declared value`() {
        val declared = testChannel(1, 1, "A").withCatchup(days = 3)
        val absent = testChannel(1, 1, "A").withCatchup(days = null)

        assertEquals(3, declared.catchupAttributes()?.days)
        assertEquals(CatchupAttributes.DEFAULT_DAYS, absent.catchupAttributes()?.days)
    }

    @Test
    fun `an unknown type with no template is not catch-up capable`() {
        assertNull(testChannel(1, 1, "A").withCatchup(type = "vod", source = null).catchupAttributes())
    }
}
