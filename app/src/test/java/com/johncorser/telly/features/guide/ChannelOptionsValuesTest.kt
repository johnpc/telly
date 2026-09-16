package com.johncorser.telly.features.guide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Value formatting + option lists of the Channel-options pane. */
class ChannelOptionsValuesTest {
    @Test
    fun `offset labels render h-min with a leading minus`() {
        assertEquals("0:00", ChannelOptionsValues.offsetLabel(0))
        assertEquals("0:30", ChannelOptionsValues.offsetLabel(30))
        assertEquals("1:30", ChannelOptionsValues.offsetLabel(90))
        assertEquals("12:00", ChannelOptionsValues.offsetLabel(720))
        assertEquals("-0:30", ChannelOptionsValues.offsetLabel(-30))
        assertEquals("-12:00", ChannelOptionsValues.offsetLabel(-720))
    }

    @Test
    fun `offset options span minus 12h to plus 12h in 30-minute steps`() {
        val options = ChannelOptionsValues.offsetOptions
        assertEquals(49, options.size)
        assertEquals("-12:00", options.first().label)
        assertEquals("-720", options.first().raw)
        assertEquals("0:00", options[24].label)
        assertEquals("12:00", options.last().label)
        assertEquals("720", options.last().raw)
    }

    @Test
    fun `decoder options offer Default-Hardware-Software and Default clears`() {
        assertEquals(listOf("Default", "Hardware", "Software"), ChannelOptionsValues.decoderOptions.map { it.label })
        assertNull(ChannelOptionsValues.decoderRawOf("Default"))
        assertEquals("Software", ChannelOptionsValues.decoderRawOf("Software"))
        assertEquals("Default", ChannelOptionsValues.decoderLabel(null))
        assertEquals("Hardware", ChannelOptionsValues.decoderLabel("Hardware"))
    }
}
