package com.johncorser.telly.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
    private val payload =
        BackupPayload(
            settings = mapOf("epg_past_days_to_keep" to "3", "general_confirm_exit" to "true"),
            playlists = listOf(BackupPlaylist(name = "Home", url = "http://p/x.m3u", epgUrl = "http://p/e.xml")),
        )

    @Test
    fun `encode decode round-trips`() {
        assertEquals(payload, BackupCodec.decode(BackupCodec.encode(payload)))
    }

    @Test
    fun `encoded json carries the version marker`() {
        val json = BackupCodec.encode(payload)
        assertTrue(json.contains("\"version\""))
        assertEquals(1, BackupCodec.decode(json)!!.version)
    }

    @Test
    fun `foreign or corrupt text decodes to null`() {
        assertNull(BackupCodec.decode("not json"))
        assertNull(BackupCodec.decode("{\"something\":\"else\"}"))
    }

    @Test
    fun `unknown fields are ignored for forward compatibility`() {
        val json = """{"version":1,"settings":{},"playlists":[],"future":"field"}"""
        assertEquals(BackupPayload(settings = emptyMap(), playlists = emptyList()), BackupCodec.decode(json))
    }

    @Test
    fun `playlist epg url is optional`() {
        val json = """{"version":1,"settings":{},"playlists":[{"name":"n","url":"u"}]}"""
        assertEquals(BackupPlaylist("n", "u", epgUrl = null), BackupCodec.decode(json)!!.playlists.single())
    }
}
