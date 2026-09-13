package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.BackupCodec
import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.features.playlist.M3uPlaylist
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsBackupManagerTest {
    @get:Rule
    val temp = TemporaryFolder()

    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val playlists = InMemoryPlaylistRepository()

    private fun manager(withDir: Boolean = false) =
        SettingsBackupManager(settings, playlists, if (withDir) temp.root else null)

    private val playlist =
        M3uPlaylist(
            epgUrl = "http://p/epg.xml",
            channels = listOf(M3uChannel(title = "One", streamUrl = "http://s/1.ts")),
        )

    @Test
    fun `export carries every setting and playlist identity`() =
        runTest {
            settings.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 3)
            playlists.add("http://p/x.m3u", playlist, name = "Home")

            val payload = BackupCodec.decode(manager().exportJson())!!

            assertEquals("3", payload.settings[TellySettings.EPG_PAST_DAYS_TO_KEEP.key])
            assertEquals(listOf("http://p/x.m3u"), payload.playlists.map { it.url })
            assertEquals("Home", payload.playlists.single().name)
            assertEquals("http://p/epg.xml", payload.playlists.single().epgUrl)
        }

    @Test
    fun `import restores settings and re-adds playlists channel-less`() =
        runTest {
            settings.set(TellySettings.CONFIRM_EXIT, true)
            val json = manager().exportJson()
            settings.set(TellySettings.CONFIRM_EXIT, false)

            assertTrue(manager().importJson(json))

            assertTrue(settings.get(TellySettings.CONFIRM_EXIT))
        }

    @Test
    fun `imported playlists keep name and epg source but wait for an update`() =
        runTest {
            playlists.add("http://p/x.m3u", playlist, name = "Home")
            val json = manager().exportJson()
            playlists.delete("http://p/x.m3u")

            manager().importJson(json)

            val restored = playlists.playlists.value.single()
            assertEquals("Home", restored.name)
            assertEquals("http://p/epg.xml", restored.playlist.epgUrl)
            assertEquals(0, restored.playlist.channels.size)
        }

    @Test
    fun `foreign json is rejected without touching anything`() =
        runTest {
            settings.set(TellySettings.CONFIRM_EXIT, true)
            assertFalse(manager().importJson("not a backup"))
            assertTrue(settings.get(TellySettings.CONFIRM_EXIT))
        }

    @Test
    fun `export to dir writes the local file and importFromFile reads it back`() =
        runTest {
            settings.set(TellySettings.USER_AGENT, "telly/1")
            val file = manager().exportToDir(temp.root)
            settings.set(TellySettings.USER_AGENT, "changed")

            assertEquals(SettingsBackupManager.BACKUP_FILE_NAME, file.name)
            assertTrue(manager().importFromFile(file))
            assertEquals("telly/1", settings.get(TellySettings.USER_AGENT))
        }

    @Test
    fun `importFromFile is false for a missing file`() =
        runTest {
            assertFalse(manager().importFromFile(temp.root.resolve("nope.json")))
        }

    @Test
    fun `exportLocal writes only when a local dir is configured`() =
        runTest {
            assertNull(manager(withDir = false).exportLocal())
            val local = manager(withDir = true).exportLocal()
            assertTrue(local!!.exists())
        }
}
