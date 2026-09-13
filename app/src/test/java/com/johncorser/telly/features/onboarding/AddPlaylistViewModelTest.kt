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
    fun `a valid url is fetched parsed stored and completes the wizard`() =
        runTest {
            val vm = viewModel()
            vm.chooseType(PlaylistType.M3U)
            vm.setUrl("http://example.com/playlist.m3u")

            vm.submitUrl()
            assertEquals(WizardStep.PROCESSING, vm.state.value.step)
            advanceUntilIdle()

            assertEquals(WizardStep.DONE, vm.state.value.step)
            assertEquals(2, vm.state.value.channelCount)
            val stored = repository.playlists.value.single()
            assertEquals("http://example.com/playlist.m3u", stored.sourceUrl)
            assertEquals("http://epg.example/g.xml", stored.playlist.epgUrl)
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
        val state = WizardUiState(step = WizardStep.URL_ENTRY, url = "http://a", error = null, channelCount = 1)

        assertEquals(state, state.copy())
        assertEquals(state.hashCode(), state.copy().hashCode())
        assertNotEquals(state, state.copy(url = "http://b"))
        assertEquals(WizardStep.URL_ENTRY, state.component1())
        assertEquals("http://a", state.component2())
        assertNull(state.component3())
        assertEquals(1, state.component4())
        assertTrue(state.toString().contains("URL_ENTRY"))
    }
}
