package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistKeyMigrationTest {
    private val old = "http://p/old.m3u"
    private val new = "http://p/new.m3u"
    private val store = InMemoryKeyValueStore()
    private val settings = SettingsRepository(store)

    @Test
    fun `moves every per-playlist key family from the old url to the new`() {
        settings.set(playlistEnabledSetting(old), false)
        settings.set(playlistUserAgentSetting(old), "telly-agent")
        settings.set(playlistUpdateIntervalSetting(old), 8)
        settings.set(playlistUpdateOnStartSetting(old), true)
        settings.set(playlistGroupEnabledSetting(old, "News"), false)

        PlaylistKeyMigration.apply(settings, old, new)

        assertFalse(settings.get(playlistEnabledSetting(new)))
        assertEquals("telly-agent", settings.get(playlistUserAgentSetting(new)))
        assertEquals(8, settings.get(playlistUpdateIntervalSetting(new)))
        assertTrue(settings.get(playlistUpdateOnStartSetting(new)))
        assertFalse(settings.get(playlistGroupEnabledSetting(new, "News")))
        assertTrue(settings.snapshot().keys.none { it.contains(old) })
    }

    @Test
    fun `group keys keep their group suffix even when it contains colons`() {
        settings.set(playlistGroupEnabledSetting(old, "News: Local"), false)

        val moves = PlaylistKeyMigration.moves(settings.snapshot(), old, new)

        assertEquals(
            listOf(playlistGroupEnabledSetting(new, "News: Local").key),
            moves.map { it.to },
        )
    }

    @Test
    fun `other playlists' keys and unrelated keys stay untouched`() {
        val other = "http://p/other.m3u"
        settings.set(playlistUserAgentSetting(other), "keep")
        settings.writeRaw("general_user_agent", "global")

        val moves = PlaylistKeyMigration.moves(settings.snapshot(), old, new)

        assertTrue(moves.isEmpty())
        assertEquals("keep", settings.get(playlistUserAgentSetting(other)))
    }

    @Test
    fun `unset defaults produce no moves`() {
        assertNull(store.read(playlistEnabledSetting(old).key))
        assertTrue(PlaylistKeyMigration.moves(settings.snapshot(), old, new).isEmpty())
    }
}
