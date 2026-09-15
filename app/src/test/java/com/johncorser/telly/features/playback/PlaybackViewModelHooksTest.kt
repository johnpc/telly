package com.johncorser.telly.features.playback

import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.player.PlayerPlatformHooks
import com.johncorser.telly.features.player.VideoDetails
import com.johncorser.telly.features.player.external.ExternalPlayer
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The ViewModel's cross-slice hook seams: PIP, the external player, AFR and
 * the My-list rows (split from PlaybackViewModelTest for the LargeClass gate).
 */
class PlaybackViewModelHooksTest : PlaybackVmHarness() {
    @Test
    fun `the quick-bar's picture-in-picture slot clears the chrome then enters pip`() =
        runTest {
            var entered = 0
            val vm = buildVm(onEnterPip = { entered += 1 })
            vm.onKey(PlaybackKey.LONG_OK)
            assertEquals(PlaybackOverlay.QuickBar, vm.overlay.value)

            vm.onQuickBarItem(QuickBarAction.PICTURE_IN_PICTURE)

            assertEquals(1, entered)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `a background stop is vetoed while the pip window plays`() =
        runTest {
            val pip = PipState()
            val vm = buildVm(pip = pip)
            pip.setInPip(true)

            vm.lifecycle.onBackground()
            vm.lifecycle.onForeground()

            assertEquals(0, engine.stops)
            assertEquals(0, exitedToGuide)

            // Closing the PIP window flips the mode off before the stop lands.
            pip.setInPip(false)
            vm.lifecycle.onBackground()
            assertEquals(1, engine.stops)
        }

    @Test
    fun `the sheet's external player row fires the chooser and returns to the panel`() =
        runTest {
            val opened = mutableListOf<String>()
            val vm =
                buildVm(
                    hooks =
                        PlaybackHooks(
                            platform =
                                PlayerPlatformHooks(
                                    external =
                                        ExternalPlayer(enabledForTuning = { false }, launch = { url ->
                                            opened += url
                                            true
                                        }),
                                ),
                        ),
                )
            vm.openPanel()
            vm.showChannelMenu(channels[1])

            vm.menu.onMenuItem(PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER)

            // The explicit row fires even while "Use external player" is Off.
            assertEquals(listOf("http://s/2.ts"), opened)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `with use external player on tunes launch externally instead of the engine`() =
        runTest {
            val opened = mutableListOf<String>()
            val vm =
                buildVm(
                    hooks =
                        PlaybackHooks(
                            platform =
                                PlayerPlatformHooks(
                                    external =
                                        ExternalPlayer(enabledForTuning = { true }, launch = { url ->
                                            opened += url
                                            true
                                        }),
                                ),
                        ),
                )
            // The cold-start restore stays internal — no chooser on app open.
            assertEquals(listOf("http://s/1.ts"), engine.loaded)
            assertTrue(opened.isEmpty())

            vm.onKey(PlaybackKey.CHANNEL_UP)

            assertEquals(listOf("http://s/2.ts"), opened)
            assertEquals(listOf("http://s/1.ts"), engine.loaded)
            assertEquals(2L, vm.current.value?.id)
            assertEquals(2L, store.getLong(TuneController.LAST_CHANNEL_KEY))
        }

    @Test
    fun `a failed external launch falls back to the internal engine`() =
        runTest {
            val vm =
                buildVm(
                    hooks =
                        PlaybackHooks(
                            platform =
                                PlayerPlatformHooks(
                                    external = ExternalPlayer(enabledForTuning = { true }, launch = { false }),
                                ),
                        ),
                )

            vm.onKey(PlaybackKey.CHANNEL_UP)

            assertEquals(listOf("http://s/1.ts", "http://s/2.ts"), engine.loaded)
            assertEquals(2L, vm.current.value?.id)
        }

    @Test
    fun `the engine's frame rate feeds the AFR hook and close signals the restore`() =
        runTest {
            val rates = mutableListOf<Float>()
            var stops = 0
            val vm =
                buildVm(
                    hooks =
                        PlaybackHooks(
                            platform =
                                PlayerPlatformHooks(
                                    onFrameRateChanged = rates::add,
                                    onPlaybackStopped = { stops += 1 },
                                ),
                        ),
                )

            engine.video.value = VideoDetails(1920, 1080, 0f, 2)
            engine.video.value = VideoDetails(1920, 1080, 25f, 2)
            engine.video.value = VideoDetails(1920, 1080, 50f, 2)

            // Unknown (0) rates never reach the hook.
            assertEquals(listOf(25f, 50f), rates)

            vm.close()
            assertEquals(1, stops)
        }

    @Test
    fun `the my-list row of a channel menu toggles the airing programme and returns to the panel`() =
        runTest {
            programs.programs.value = listOf(testProgram("tvg-1", 900_000L, 1_100_000L, "Business Hour"))
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])
            assertFalse(vm.menu.myList?.savedFor(channels[0], vm.myList.keys.value) == true)

            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)

            assertEquals(listOf(900_000L), myListStore.entries.first().map { it.startMs })
            assertTrue(vm.menu.myList?.savedFor(channels[0], vm.myList.keys.value) == true)
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)

            vm.showChannelMenu(channels[0])
            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)
            assertEquals(emptyList<Any>(), myListStore.entries.first())
        }

    @Test
    fun `the my-list row without EPG leaves the store alone but still dismisses the sheet`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.ADD_TO_MY_LIST)

            assertEquals(emptyList<Any>(), myListStore.entries.first())
            assertEquals(PlaybackOverlay.Panel, vm.overlay.value)
        }

    @Test
    fun `manage favorites opens its screen over bare playback`() =
        runTest {
            val vm = buildVm()
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.MANAGE_FAVORITES)

            assertEquals(1, manageOpens)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }

    @Test
    fun `reorder channels opens its screen on the panel's selected group`() =
        runTest {
            val vm = buildVm()
            vm.panel.selectGroup("News")
            vm.openPanel()
            vm.showChannelMenu(channels[0])

            vm.menu.onMenuItem(PlayerMenuItem.REORDER_CHANNELS)

            assertEquals(listOf("News"), reorderGroups)
            assertEquals(PlaybackOverlay.None, vm.overlay.value)
        }
}
