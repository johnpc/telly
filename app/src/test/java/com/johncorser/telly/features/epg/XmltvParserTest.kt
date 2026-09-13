package com.johncorser.telly.features.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kxml2.io.KXmlParser
import java.io.InputStreamReader
import java.io.StringReader

class XmltvParserTest {
    private fun parse(xml: String): XmltvDocument = XmltvParser.parse(KXmlParser(), StringReader(xml))

    @Test
    fun `parses the fixture epg end to end`() {
        val document =
            javaClass.getResourceAsStream("/fixtures/epg.xml")!!.use { stream ->
                XmltvParser.parse(KXmlParser(), InputStreamReader(stream, Charsets.UTF_8))
            }

        assertEquals(30, document.channels.size)
        assertEquals(917, document.programs.size)

        val first = document.channels.first()
        assertEquals("news-one-1.fixture", first.id)
        assertEquals("News One", first.displayName)
        assertEquals("http://10.0.2.2:8090/logos/news-one.png", first.iconUrl)

        val program = document.programs.first()
        assertEquals("news-one-1.fixture", program.channelId)
        assertEquals(1_789_302_600_000L, program.startMs) // 20260913123000 +0000
        assertEquals(1_789_307_100_000L, program.endMs) // 20260913134500 +0000
        assertEquals("Weather Watch", program.details.title)
        assertEquals("Weather Watch Special", program.details.subTitle)
        assertEquals("News", program.details.category)
        assertTrue(program.details.description!!.startsWith("A fan-favorite returns"))
        assertNull(program.details.episode)
    }

    @Test
    fun `reads episode numbers when present`() {
        val withEpisode =
            javaClass.getResourceAsStream("/fixtures/epg.xml")!!.use { stream ->
                XmltvParser.parse(KXmlParser(), InputStreamReader(stream, Charsets.UTF_8))
            }.programs.first { it.details.episode != null }
        assertEquals("S1 E2", withEpisode.details.episode)
        assertEquals("Episode 2", withEpisode.details.subTitle)
    }

    @Test
    fun `an empty tv document parses to an empty result`() {
        assertEquals(XmltvDocument(), parse("<tv/>"))
    }

    @Test
    fun `channels without an id are skipped`() {
        val document = parse("""<tv><channel><display-name>Ghost</display-name></channel></tv>""")
        assertTrue(document.channels.isEmpty())
    }

    @Test
    fun `channels tolerate missing display-name and icon`() {
        val document = parse("""<tv><channel id="c1"/></tv>""")
        assertEquals(listOf(XmltvChannel(id = "c1")), document.channels)
    }

    @Test
    fun `only the first display-name and icon win`() {
        val document =
            parse(
                """<tv><channel id="c1"><display-name>First</display-name>
                  |<display-name>Second</display-name><icon src="a.png"/><icon src="b.png"/>
                  |</channel></tv>
                """.trimMargin(),
            )
        assertEquals("First", document.channels.single().displayName)
        assertEquals("a.png", document.channels.single().iconUrl)
    }

    @Test
    fun `programmes with a missing channel start stop or title are skipped`() {
        val document =
            parse(
                """<tv>
                  |<programme start="20260913123000 +0000" stop="20260913130000 +0000"><title>NoChannel</title></programme>
                  |<programme channel="c1" stop="20260913130000 +0000"><title>NoStart</title></programme>
                  |<programme channel="c1" start="20260913123000 +0000"><title>NoStop</title></programme>
                  |<programme channel="c1" start="bogus" stop="20260913130000 +0000"><title>BadStart</title></programme>
                  |<programme channel="c1" start="20260913123000 +0000" stop="20260913130000 +0000"/>
                  |</tv>
                """.trimMargin(),
            )
        assertTrue(document.programs.isEmpty())
    }

    @Test
    fun `programmes tolerate missing optional fields and unknown children`() {
        val document =
            parse(
                """<tv><programme channel="c1" start="20260913123000 +0000" stop="20260913130000 +0000">
                  |<title>Show</title><unknown><nested>deep</nested></unknown><rating><value>PG</value></rating>
                  |</programme></tv>
                """.trimMargin(),
            )
        val program = document.programs.single()
        assertEquals("Show", program.details.title)
        assertNull(program.details.description)
        assertNull(program.details.category)
        assertNull(program.details.episode)
    }

    @Test
    fun `document types are value objects`() {
        val channel = XmltvChannel(id = "c1", displayName = "One", iconUrl = "l.png")
        assertEquals(channel, channel.copy())
        assertTrue(channel.toString().contains("c1"))

        val document = XmltvDocument(channels = listOf(channel))
        assertEquals(document, document.copy())
        assertTrue(document.hashCode() == document.copy().hashCode())
    }
}
