package com.johncorser.telly.features.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class PinEntryTest {
    @Test
    fun `starts at 0000 with the first wheel active`() {
        assertEquals("0000", PinEntry().value)
        assertEquals(0, PinEntry().cursor)
    }

    @Test
    fun `up and down spin only the active wheel and wrap`() {
        assertEquals("1000", PinEntry().up().value)
        assertEquals("9000", PinEntry().down().value)
        assertEquals("0000", PinEntry().up().down().value)
        assertEquals("0100", PinEntry().right().up().value)
    }

    @Test
    fun `left and right move within the four wheels`() {
        assertEquals(0, PinEntry().left().cursor)
        assertEquals(3, PinEntry().right().right().right().right().cursor)
    }

    @Test
    fun `a full pin can be dialed`() {
        val entry =
            PinEntry()
                .up()
                .up()
                .right()
                .up()
                .right()
                .up()
                .up()
                .up()
                .right()
                .down()
        assertEquals("2139", entry.value)
    }

    @Test
    fun `commit submits the dialed pin and blanks the wheels for a kept-open prompt`() {
        val submitted = mutableListOf<String>()
        val wrongPin = PinEntry(digits = listOf(1, 1, 1, 1), cursor = 3)

        val after = wrongPin.commit { submitted += it }

        assertEquals(listOf("1111"), submitted)
        // A rejected PIN keeps the prompt open: the next entry must dial
        // from 0000 with the first wheel active, not from the stale digits.
        assertEquals(PinEntry(), after)
    }

    @Test
    fun `keyboard commit hands the pin over and clears the field`() {
        val submitted = mutableListOf<String>()

        val after = PinKeyboard.commit("1111") { submitted += it }

        assertEquals(listOf("1111"), submitted)
        assertEquals("", after)
        // A cleared field can complete again; a stale full one never could.
        assertEquals(false, PinKeyboard.isComplete(after))
    }

    @Test
    fun `keyboard entry keeps digits only, capped at four`() {
        assertEquals("2468", PinKeyboard.sanitize("2468"))
        assertEquals("2468", PinKeyboard.sanitize("24-68x9"))
        assertEquals("1234", PinKeyboard.sanitize("123456"))
        assertEquals("", PinKeyboard.sanitize("abc"))
    }

    @Test
    fun `keyboard entry commits exactly at the fourth digit`() {
        assertEquals(false, PinKeyboard.isComplete(""))
        assertEquals(false, PinKeyboard.isComplete("246"))
        assertEquals(true, PinKeyboard.isComplete("2468"))
    }
}
