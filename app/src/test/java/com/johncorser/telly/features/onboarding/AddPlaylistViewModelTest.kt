package com.johncorser.telly.features.onboarding

import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AddPlaylistViewModelTest {
    private val playlistBody =
        "#EXTM3U url-tvg=\"http://epg.example/g.xml\"\n" +
            "#EXTINF:-1 group-title=\"News\",News One\nhttp://stream.example/1.ts\n" +
            "#EXTINF:-1 group-title=\"Kids\",Kids Zone\nhttp://stream.example/2.ts"
    private val repository = InMemoryPlaylistRepository()

    private fun CoroutineScope.viewModel(fetch: suspend (String) -> String = { playlistBody }) =
        AddPlaylistViewModel(scope = this, fetchPlaylist = fetch, repository = repository)

    @Test
    fun `starts on the type chooser`() =
        runTest {
            assertEquals(WizardUiState(), viewModel().state.value)
        }

    @Test
    fun `only choosing m3u advances to url entry`() =
        runTest {
            val vm = viewModel()

            vm.chooseType(PlaylistType.XTREAM_CODES)
            vm.chooseType(PlaylistType.STALKER_PORTAL)
            assertEquals(WizardStep.TYPE_CHOOSER, vm.state.value.step)

            vm.chooseType(PlaylistType.M3U)
            assertEquals(WizardStep.URL_ENTRY, vm.state.value.step)
        }

    @Test
    fun `submitting a non-http url surfaces a validation error`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)

            vm.setUrl("ftp://example.com/playlist.m3u")
            vm.submitUrl()

            assertEquals(WizardError.INVALID_URL, vm.state.value.error)
            assertEquals(WizardStep.URL_ENTRY, vm.state.value.step)
        }

    @Test
    fun `editing the url clears a previous error`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("nope")
            vm.submitUrl()

            vm.setUrl("http://example.com/playlist.m3u")

            assertNull(vm.state.value.error)
        }

    @Test
    fun `a valid url is fetched parsed and lands on the processed step`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")

            vm.submitUrl()
            assertEquals(WizardStep.PROCESSING, vm.state.value.step)
            advanceUntilIdle()

            val state = vm.state.value
            assertEquals(WizardStep.PROCESSED, state.step)
            assertEquals("example.com", state.name)
            assertEquals(2, state.liveCount)
            assertEquals(0, state.movieCount)
            assertEquals(2, state.channelCount)
            assertEquals(2, state.groupCount)
            assertEquals(PlaylistKind.TV, state.kind)
            // Nothing is persisted until the processed step is confirmed.
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `confirming the processed step opens the epg step with url-tvg pre-filled`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()

            vm.setName("My IPTV")
            vm.chooseKind(PlaylistKind.VOD)
            vm.confirm()

            assertEquals(WizardStep.EPG_URL, vm.state.value.step)
            assertEquals("http://epg.example/g.xml", vm.state.value.epgUrl)
            // Nothing is persisted until the EPG step's Done (capture 14).
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `done on the epg step stores the playlist under the chosen name`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()

            vm.setName("My IPTV")
            vm.chooseKind(PlaylistKind.VOD)
            vm.confirm()
            vm.finishEpg()
            advanceUntilIdle()

            assertEquals(WizardStep.DONE, vm.state.value.step)
            assertEquals(PlaylistKind.VOD, vm.state.value.kind)
            val stored = repository.playlists.value.single()
            assertEquals("http://example.com/playlist.m3u", stored.sourceUrl)
            assertEquals("http://epg.example/g.xml", stored.playlist.epgUrl)
            assertEquals("My IPTV", stored.name)
        }

    @Test
    fun `an edited epg url overrides the playlist url-tvg`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            vm.confirm()

            vm.setEpgUrl("http://epg.example/other.xml")
            vm.finishEpg()
            advanceUntilIdle()

            assertEquals("http://epg.example/other.xml", repository.playlists.value.single().playlist.epgUrl)
        }

    @Test
    fun `a blank epg url skips the epg entirely`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            vm.confirm()

            vm.setEpgUrl("   ")
            vm.finishEpg()
            advanceUntilIdle()

            assertEquals(WizardStep.DONE, vm.state.value.step)
            assertNull(repository.playlists.value.single().playlist.epgUrl)
        }

    @Test
    fun `an invalid epg url keeps the step open with a validation error`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            vm.confirm()

            vm.setEpgUrl("not-a-url")
            vm.finishEpg()
            advanceUntilIdle()

            assertEquals(WizardStep.EPG_URL, vm.state.value.step)
            assertEquals(WizardError.INVALID_URL, vm.state.value.error)
            assertTrue(repository.playlists.value.isEmpty())

            vm.setEpgUrl("http://epg.example/fixed.xml")
            assertNull(vm.state.value.error)
        }

    @Test
    fun `paste playlist url copies the playlist url into the epg draft`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            vm.confirm()

            vm.pastePlaylistUrl()

            assertEquals("http://example.com/playlist.m3u", vm.state.value.epgUrl)
        }

    @Test
    fun `a blank name falls back to the repository default`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()

            vm.setName("   ")
            vm.confirm()
            vm.finishEpg()
            advanceUntilIdle()

            assertNull(repository.playlists.value.single().name)
        }

    @Test
    fun `confirm before processing finished is a no-op`() =
        runTest {
            val vm = viewModel()

            vm.confirm()
            advanceUntilIdle()

            assertEquals(WizardStep.TYPE_CHOOSER, vm.state.value.step)
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `fetch failures return to url entry with a load error`() =
        runTest {
            val vm = viewModel(fetch = { throw IOException("boom") })
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")

            vm.submitUrl()
            advanceUntilIdle()

            assertEquals(WizardStep.URL_ENTRY, vm.state.value.step)
            assertEquals(WizardError.LOAD_FAILED, vm.state.value.error)
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `back walks one step at a time and is unhandled on step one`() =
        runTest {
            val vm = viewModel()
            assertFalse(vm.back())

            vm.chooseType(PlaylistType.M3U)
            assertTrue(vm.back())
            assertEquals(WizardStep.TYPE_CHOOSER, vm.state.value.step)
            assertFalse(vm.back())
        }

    @Test
    fun `back from the epg step returns to processed keeping the parse`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            vm.confirm()
            assertEquals(WizardStep.EPG_URL, vm.state.value.step)

            assertTrue(vm.back())
            assertEquals(WizardStep.PROCESSED, vm.state.value.step)

            // The parse survives: forward again and finish normally.
            vm.confirm()
            vm.finishEpg()
            advanceUntilIdle()
            assertEquals(WizardStep.DONE, vm.state.value.step)
            assertEquals(1, repository.playlists.value.size)
        }

    @Test
    fun `back from the processed step discards the parse and returns to url entry`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            advanceUntilIdle()
            assertEquals(WizardStep.PROCESSED, vm.state.value.step)

            assertTrue(vm.back())
            assertEquals(WizardStep.URL_ENTRY, vm.state.value.step)

            // The discarded parse can no longer be confirmed or finished.
            vm.confirm()
            vm.finishEpg()
            advanceUntilIdle()
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `back during processing cancels the load and returns to url entry`() =
        runTest {
            val vm = viewModel(fetch = { awaitCancellation() })
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")
            vm.submitUrl()
            runCurrent()

            assertTrue(vm.back())
            advanceUntilIdle()

            assertEquals(WizardStep.URL_ENTRY, vm.state.value.step)
            assertNull(vm.state.value.error)
            assertTrue(repository.playlists.value.isEmpty())
        }

    @Test
    fun `ui state behaves as a value object`() {
        val state =
            WizardUiState(
                step = WizardStep.URL_ENTRY,
                url = "http://a",
                error = null,
                name = "n",
                kind = PlaylistKind.VOD,
                liveCount = 1,
                movieCount = 2,
                groupCount = 3,
            )

        assertEquals(state, state.copy())
        assertEquals(state.hashCode(), state.copy().hashCode())
        assertNotEquals(state, state.copy(url = "http://b"))
        assertEquals(WizardStep.URL_ENTRY, state.component1())
        assertEquals("http://a", state.component2())
        assertNull(state.component3())
        assertEquals("n", state.component4())
        assertEquals(PlaylistKind.VOD, state.component5())
        assertEquals(1, state.component6())
        assertEquals(2, state.component7())
        assertEquals(3, state.component8())
        assertEquals(3, state.channelCount)
        assertTrue(state.toString().contains("URL_ENTRY"))
    }
}
