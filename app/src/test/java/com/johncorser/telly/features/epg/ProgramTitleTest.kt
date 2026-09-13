package com.johncorser.telly.features.epg

import com.johncorser.telly.features.epg.db.ProgramDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramTitleTest {
    @Test
    fun `title plus sub-title plus episode matches the reference rendering`() {
        assertEquals(
            "Global Update: Episode 10. S1 E10",
            ProgramTitle.of(ProgramDetails("Global Update", subTitle = "Episode 10", episode = "S1 E10")),
        )
    }

    @Test
    fun `sub-title without an episode keeps the colon form only`() {
        assertEquals(
            "Global Update: Global Update Special",
            ProgramTitle.of(ProgramDetails("Global Update", subTitle = "Global Update Special")),
        )
    }

    @Test
    fun `episode without a sub-title appends after a full stop`() {
        assertEquals(
            "Business Hour. S1 E7",
            ProgramTitle.of(ProgramDetails("Business Hour", episode = "S1 E7")),
        )
    }

    @Test
    fun `a bare title renders unchanged`() {
        assertEquals("Business Hour", ProgramTitle.of(ProgramDetails("Business Hour")))
    }

    @Test
    fun `blank sub-title and episode are treated as absent`() {
        assertEquals(
            "Business Hour",
            ProgramTitle.of(ProgramDetails("Business Hour", subTitle = " ", episode = "")),
        )
    }
}
