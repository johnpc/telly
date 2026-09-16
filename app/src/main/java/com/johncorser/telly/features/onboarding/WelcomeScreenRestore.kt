package com.johncorser.telly.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import kotlinx.coroutines.launch

/**
 * The welcome screen's "Restore previous setup" pill: applies a readable
 * backup directly, or opens the all-files-access grant screen first when
 * scoped storage hides a previous install's file ([RestoreOffer.NeedsAccess]).
 */
@Composable
fun WelcomeScreenRestorePill(
    restore: OnboardingRestore,
    offer: RestoreOffer,
    modifier: Modifier = Modifier,
) {
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    WelcomeScreenPill(
        text = stringResource(if (busy) R.string.welcome_restoring else R.string.welcome_restore),
        onClick = {
            when (offer) {
                is RestoreOffer.Ready ->
                    if (!busy) {
                        busy = true
                        scope.launch {
                            if (restore.restore(offer.json)) restore.land() else busy = false
                        }
                    }
                RestoreOffer.NeedsAccess -> restore.requestAccess()
                RestoreOffer.None -> Unit
            }
        },
        modifier = modifier,
    )
}
