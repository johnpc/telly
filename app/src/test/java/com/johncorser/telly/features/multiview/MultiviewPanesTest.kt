package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.player.PlayerEnginePool
import com.johncorser.telly.testutil.FakePlayerEngine
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiviewPanesTest {
    private val engines = mutableListOf<FakePlayerEngine>()
    private val panes = MultiviewPanes(PlayerEnginePool { FakePlayerEngine().also { engines += it } })
    private val channels = (1L..6L).map { testChannel(it, it.toInt(), "Channel $it") }

    @Test
    fun `adding a pane loads its stream, focuses it and hands it the audio`() {
        panes.add(channels[0])
        panes.add(channels[1])

        assertEquals(listOf("http://s/1.ts"), engines[0].loaded)
        assertEquals(listOf("http://s/2.ts"), engines[1].loaded)
        assertEquals(2, panes.panes.value[1].id)
        assertEquals(2, panes.focusedId.value)
        assertTrue(engines[0].muted)
        assertFalse(engines[1].muted)
    }

    @Test
    fun `panes cap at four`() {
        channels.forEach(panes::add)

        assertEquals(MultiviewGrid.MAX_PANES, panes.panes.value.size)
        assertFalse(panes.add(channels[4]))
        assertEquals(4, engines.size)
    }

    @Test
    fun `focus moves audio ownership between panes`() {
        panes.add(channels[0])
        panes.add(channels[1])

        panes.focus(1)

        assertEquals(1, panes.focusedId.value)
        assertFalse(engines[0].muted)
        assertTrue(engines[1].muted)
    }

    @Test
    fun `focusing an unknown pane id is ignored`() {
        panes.add(channels[0])

        panes.focus(99)

        assertEquals(1, panes.focusedId.value)
    }

    @Test
    fun `change retunes the focused pane in place`() {
        panes.add(channels[0])
        panes.add(channels[1])

        panes.change(channels[2])

        assertEquals(listOf("http://s/2.ts", "http://s/3.ts"), engines[1].loaded)
        assertEquals(3L, panes.panes.value[1].channel.id)
        assertEquals(2, panes.panes.value.size)
    }

    @Test
    fun `removing the focused pane releases its engine and refocuses a neighbour`() {
        panes.add(channels[0])
        panes.add(channels[1])
        panes.add(channels[2])
        panes.focus(2)

        assertTrue(panes.removeFocused())

        assertEquals(listOf(1L, 3L), panes.panes.value.map { it.channel.id })
        assertTrue(engines[1].released)
        assertEquals(3, panes.focusedId.value)
        assertFalse(engines[2].muted)
    }

    @Test
    fun `removing the last remaining pane at the end refocuses the previous one`() {
        panes.add(channels[0])
        panes.add(channels[1])

        assertTrue(panes.removeFocused())

        assertEquals(listOf(1L), panes.panes.value.map { it.channel.id })
        assertEquals(1, panes.focusedId.value)
        assertFalse(engines[0].muted)
    }

    @Test
    fun `the only pane can never be removed`() {
        panes.add(channels[0])

        assertFalse(panes.removeFocused())
        assertFalse(engines[0].released)
    }

    @Test
    fun `releaseAll releases every pane engine`() {
        panes.add(channels[0])
        panes.add(channels[1])

        panes.releaseAll()

        assertTrue(engines.all { it.released })
    }
}
