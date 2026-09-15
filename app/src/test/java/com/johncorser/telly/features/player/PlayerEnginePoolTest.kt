package com.johncorser.telly.features.player

import com.johncorser.telly.testutil.FakePlayerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerEnginePoolTest {
    private val created = mutableListOf<FakePlayerEngine>()
    private val pool = PlayerEnginePool { FakePlayerEngine().also { created += it } }

    @Test
    fun `acquire builds one independent engine per call`() {
        val first = pool.acquire()
        val second = pool.acquire()

        assertEquals(2, created.size)
        assertEquals(2, pool.size)
        assertFalse(first === second)
    }

    @Test
    fun `release frees one engine and forgets it`() {
        val engine = pool.acquire()

        pool.release(engine)

        assertTrue(created.single().released)
        assertEquals(0, pool.size)
    }

    @Test
    fun `releasing a foreign engine is ignored`() {
        pool.acquire()

        pool.release(FakePlayerEngine())

        assertFalse(created.single().released)
        assertEquals(1, pool.size)
    }

    @Test
    fun `releaseAll frees every engine`() {
        pool.acquire()
        pool.acquire()

        pool.releaseAll()

        assertTrue(created.all { it.released })
        assertEquals(0, pool.size)
    }
}
