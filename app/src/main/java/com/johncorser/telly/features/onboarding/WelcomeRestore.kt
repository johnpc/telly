package com.johncorser.telly.features.onboarding

import com.johncorser.telly.core.settings.BackupCodec

/** The welcome screen's "Restore previous setup" affordance state. */
sealed interface RestoreOffer {
    /** No backup anywhere we can see: the plain two-button welcome. */
    data object None : RestoreOffer

    /** A readable telly backup: restoring applies [json] directly. */
    data class Ready(
        val json: String,
    ) : RestoreOffer

    /**
     * A previous install's file is on disk but scoped storage hides it
     * from this install: restoring first needs the all-files-access grant.
     */
    data object NeedsAccess : RestoreOffer
}

/** Pure policy: what the welcome screen offers, from the probe's findings. */
object WelcomeRestore {
    fun offerOf(
        json: String?,
        sighted: Boolean,
        canRequestAccess: Boolean,
    ): RestoreOffer =
        when {
            json != null && BackupCodec.decode(json) != null -> RestoreOffer.Ready(json)
            sighted && canRequestAccess -> RestoreOffer.NeedsAccess
            else -> RestoreOffer.None
        }
}
