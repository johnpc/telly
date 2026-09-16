package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.settings.BackupCodec
import com.johncorser.telly.core.settings.BackupPayload
import org.junit.Assert.assertEquals
import org.junit.Test

class WelcomeRestoreTest {
    private val backupJson =
        BackupCodec.encode(BackupPayload(settings = mapOf("k" to "v"), playlists = emptyList()))

    @Test
    fun `a readable telly backup is offered directly`() {
        assertEquals(
            RestoreOffer.Ready(backupJson),
            WelcomeRestore.offerOf(json = backupJson, sighted = true, canRequestAccess = false),
        )
    }

    @Test
    fun `a sighted-but-unreadable file needs the access grant`() {
        assertEquals(
            RestoreOffer.NeedsAccess,
            WelcomeRestore.offerOf(json = null, sighted = true, canRequestAccess = true),
        )
    }

    @Test
    fun `non-telly JSON never offers a restore`() {
        assertEquals(
            RestoreOffer.None,
            WelcomeRestore.offerOf(json = "{\"not\":\"telly\"}", sighted = false, canRequestAccess = false),
        )
    }

    @Test
    fun `nothing sighted or no grantable access is the plain welcome`() {
        assertEquals(RestoreOffer.None, WelcomeRestore.offerOf(json = null, sighted = false, canRequestAccess = true))
        assertEquals(RestoreOffer.None, WelcomeRestore.offerOf(json = null, sighted = true, canRequestAccess = false))
    }
}
