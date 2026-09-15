package com.johncorser.telly.features.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingFilesTest {
    private val pair = tempRecordingFiles()
    private val dir = pair.first
    private val files = pair.second

    @Test
    fun `capture filenames sanitize the channel and stamp the start`() {
        val file = files.newFile("News One / HD+", 0L)

        assertEquals("News_One___HD_-19700101-0000.ts", file.name)
        assertEquals(dir, file.parentFile)
    }

    @Test
    fun `an existing capture of the same slot gets a numbered suffix`() {
        files.newFile("News One", 0L).writeText("first")

        assertEquals("News_One-19700101-0000-1.ts", files.newFile("News One", 0L).name)
    }

    @Test
    fun `storage sums the capture files and delete removes one`() {
        val a = files.newFile("A", 0L).apply { writeText("12345") }
        files.newFile("B", 60_000L).apply { writeText("123") }

        assertEquals(8L, files.storage().usedBytes)
        assertTrue(files.storage().freeBytes > 0)

        files.delete(a.path)
        assertFalse(files.exists(a.path))
        assertEquals(3L, files.storage().usedBytes)
    }

    @Test
    fun `deleteAllFiles empties the capture dir and blank paths are ignored`() {
        files.newFile("A", 0L).writeText("12345")
        files.newFile("B", 60_000L).writeText("123")

        files.delete("")
        files.deleteAllFiles()

        assertEquals(0L, files.storage().usedBytes)
    }

    @Test
    fun `sizeOf reads the file length and zero for missing files`() {
        val file = files.newFile("A", 0L).apply { writeText("1234") }

        assertEquals(4L, files.sizeOf(file.path))
        assertEquals(0L, files.sizeOf(file.path + ".missing"))
    }
}
