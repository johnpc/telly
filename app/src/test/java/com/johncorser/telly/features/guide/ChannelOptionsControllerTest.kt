package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelOverrides
import com.johncorser.telly.testutil.FakeChannelDao
import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.withOverrides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pane's shared state machine: dialogs, persistence and host effects. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChannelOptionsControllerTest {
    private val dao = FakeChannelDao(listOf(testChannel(1, 1, "News One")))
    private var globalExternal = false

    private fun TestScope.build(): ChannelOptionsController {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        return ChannelOptionsController(ChannelOptionsStore(dao, scope)) { globalExternal }
    }

    private val channel get() = dao.channels.value.single()

    @Test
    fun `rename opens prefilled with the display name and commits the custom name`() =
        runTest {
            val controller = build()

            assertNull(controller.onRow(channel, GuideChannelOptions.NAME))
            assertEquals(ChannelOptionsDialog.Rename(1L, "News One"), controller.dialog.value)

            controller.submitRename("News Uno")
            assertEquals("News Uno", channel.overrides.customName)
            assertNull(controller.dialog.value)

            // Reopening prefills with the CURRENT display name.
            controller.onRow(channel, GuideChannelOptions.NAME)
            assertEquals(ChannelOptionsDialog.Rename(1L, "News Uno"), controller.dialog.value)
        }

    @Test
    fun `a blank rename and the restore row both clear the custom name`() =
        runTest {
            val controller = build()
            controller.onRow(channel, GuideChannelOptions.NAME)
            controller.submitRename("News Uno")

            controller.onRow(channel, GuideChannelOptions.NAME)
            controller.submitRename("  ")
            assertNull(channel.overrides.customName)

            controller.onRow(channel, GuideChannelOptions.NAME)
            controller.submitRename("News Dos")
            assertNull(controller.onRow(channel, GuideChannelOptions.RESTORE_NAME))
            assertNull(channel.overrides.customName)
        }

    @Test
    fun `the decoder pickers persist overrides and Default clears them`() =
        runTest {
            val controller = build()

            controller.onRow(channel, GuideChannelOptions.AUDIO_DECODER)
            assertEquals(
                ChannelOptionsDialog.Picker(1L, ChannelOptionsPicker.AUDIO_DECODER),
                controller.dialog.value,
            )
            controller.choose("Software")
            assertEquals("Software", channel.overrides.audioDecoder)
            assertNull(controller.dialog.value)

            controller.onRow(channel, GuideChannelOptions.VIDEO_DECODER)
            controller.choose("Hardware")
            assertEquals("Hardware", channel.overrides.videoDecoder)

            controller.onRow(channel, GuideChannelOptions.AUDIO_DECODER)
            controller.choose("Default")
            assertNull(channel.overrides.audioDecoder)
        }

    @Test
    fun `the offset picker persists minutes and highlights the current pick`() =
        runTest {
            val controller = build()

            controller.onRow(channel, GuideChannelOptions.EPG_OFFSET)
            controller.choose("60")
            assertEquals(60, channel.overrides.epgOffsetMinutes)

            assertEquals("60", controller.pickerCurrent(ChannelOptionsPicker.EPG_OFFSET, channel))
            assertEquals("Default", controller.pickerCurrent(ChannelOptionsPicker.AUDIO_DECODER, channel))
            assertEquals(
                ChannelOptionsValues.offsetOptions,
                controller.pickerOptions(ChannelOptionsPicker.EPG_OFFSET),
            )
            assertEquals(
                ChannelOptionsValues.decoderOptions,
                controller.pickerOptions(ChannelOptionsPicker.VIDEO_DECODER),
            )
        }

    @Test
    fun `the external toggle flips the effective state into an explicit override`() =
        runTest {
            val controller = build()

            // Global Off, no override: toggling turns the channel On.
            controller.onRow(channel, GuideChannelOptions.EXTERNAL_PLAYER)
            assertEquals("On", channel.overrides.externalPlayer)

            controller.onRow(channel, GuideChannelOptions.EXTERNAL_PLAYER)
            assertEquals("Off", channel.overrides.externalPlayer)

            // Global On with no override: the first toggle turns it Off.
            globalExternal = true
            dao.channels.value = listOf(channel.withOverrides(ChannelOverrides()))
            controller.onRow(channel, GuideChannelOptions.EXTERNAL_PLAYER)
            assertEquals("Off", channel.overrides.externalPlayer)
        }

    @Test
    fun `block, hide and the names editor defer to the host`() =
        runTest {
            val controller = build()
            assertEquals(ChannelOptionsEffect.BLOCK, controller.onRow(channel, GuideChannelOptions.BLOCK))
            assertEquals(ChannelOptionsEffect.HIDE, controller.onRow(channel, GuideChannelOptions.HIDE))
            assertEquals(
                ChannelOptionsEffect.NAMES_EDITOR,
                controller.onRow(channel, GuideChannelOptions.NAMES_EDITOR),
            )
            assertNull(controller.dialog.value)
        }

    @Test
    fun `closeDialog reports whether anything was open`() =
        runTest {
            val controller = build()
            assertFalse(controller.closeDialog())

            controller.onRow(channel, GuideChannelOptions.EPG_OFFSET)
            assertTrue(controller.closeDialog())
            assertNull(controller.dialog.value)
        }

    @Test
    fun `mutations re-read the fresh row so consecutive edits never clobber`() =
        runTest {
            val controller = build()
            val stale = channel

            controller.onRow(stale, GuideChannelOptions.NAME)
            controller.submitRename("News Uno")
            // The second edit passes the STALE snapshot on purpose.
            controller.onRow(stale, GuideChannelOptions.AUDIO_DECODER)
            controller.choose("Software")

            assertEquals("News Uno", channel.overrides.customName)
            assertEquals("Software", channel.overrides.audioDecoder)
        }

    @Test
    fun `rows reflect the live channel state`() =
        runTest {
            val controller = build()
            controller.onRow(channel, GuideChannelOptions.EPG_OFFSET)
            controller.choose("-30")

            val offsetRow =
                controller
                    .rows(channel)
                    .first { it.id == GuideChannelOptions.EPG_OFFSET }
            assertEquals("-0:30", (offsetRow as com.johncorser.telly.features.settings.SettingsRow.Value).summary)
        }
}
