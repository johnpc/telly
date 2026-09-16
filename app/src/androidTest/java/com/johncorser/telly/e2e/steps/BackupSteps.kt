package com.johncorser.telly.e2e.steps

import android.os.SystemClock
import com.johncorser.telly.core.settings.BackupCodec
import com.johncorser.telly.core.settings.BackupPayload
import com.johncorser.telly.core.settings.BackupPlaylist
import com.johncorser.telly.e2e.TellyWorld
import com.johncorser.telly.features.settings.MediaStoreBackupDocuments
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.junit.Assert.assertTrue

/**
 * Reinstall-proof backup steps. The harness cannot drive a real
 * uninstall/reinstall, so the previous-install file is seeded through the
 * same [MediaStoreBackupDocuments] seam the app writes with, and the
 * scenarios prove the full JSON round trip: automatic export on change,
 * and welcome-screen restore back to playing channels.
 */
class BackupSteps(
    private val world: TellyWorld,
) {
    private val store get() = MediaStoreBackupDocuments(world.targetContext)

    @Given("a local backup of a configured install exists")
    fun seedBackup() {
        val playlist = world.mapFixtureText("http://10.0.2.2:8090/playlist.m3u")
        val epg = world.mapFixtureText("http://10.0.2.2:8090/epg.xml")
        val json =
            BackupCodec.encode(
                BackupPayload(
                    settings = emptyMap(),
                    playlists = listOf(BackupPlaylist(name = "10.0.2.2", url = playlist, epgUrl = epg)),
                ),
            )
        store.write(json)
    }

    @Then("the automatic backup file eventually contains the playlist {string}")
    fun backupEventuallyContains(url: String) {
        val expected = world.mapFixtureText(url)
        val deadline = SystemClock.uptimeMillis() + EXPORT_TIMEOUT_MS
        var playlists: List<BackupPlaylist> = emptyList()
        while (SystemClock.uptimeMillis() < deadline) {
            playlists = store.read()?.let { BackupCodec.decode(it) }?.playlists.orEmpty()
            if (playlists.any { it.url == expected }) return
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("backup never exported $expected; last saw $playlists", false)
    }

    private companion object {
        // The engine debounces ~5 s; leave slack for emulator IO.
        const val EXPORT_TIMEOUT_MS = 20_000L
        const val POLL_MS = 500L
    }
}
