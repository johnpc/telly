package com.johncorser.telly.features.onboarding

/**
 * Applies a backup from the welcome screen: the existing import machinery
 * restores settings + playlist identities (with zero channels), then every
 * restored playlist is re-fetched so the normal cold-start policy has real
 * channels to land on. Success = at least one channel exists afterwards.
 */
class RestoreRunner(
    private val importJson: suspend (String) -> Boolean,
    private val playlistUrls: suspend () -> List<String>,
    private val updatePlaylists: suspend (List<String>) -> Unit,
    private val channelCount: suspend () -> Int,
) {
    suspend fun restore(json: String): Boolean {
        val imported = runCatching { importJson(json) }.getOrDefault(false)
        if (!imported) return false
        runCatching { updatePlaylists(playlistUrls()) }
        return runCatching { channelCount() }.getOrDefault(0) > 0
    }
}

/** Platform hooks the welcome screen's restore affordance runs on. */
class OnboardingRestore(
    /** Probes shared storage for a previous install's backup. */
    val probe: suspend () -> RestoreOffer,
    /** Applies a [RestoreOffer.Ready] backup; true when channels landed. */
    val restore: suspend (String) -> Boolean,
    /** Opens the all-files-access grant screen ([RestoreOffer.NeedsAccess]). */
    val requestAccess: () -> Unit = {},
    /** Leaves the welcome screen per the normal cold-start policy. */
    val land: () -> Unit = {},
)
