package com.johncorser.telly.features.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.core.ui.ScreenCrossfade
import com.johncorser.telly.features.playlist.PlaylistRepository

/**
 * Add-playlist wizard host: guidance pane on the left, the active step on the
 * right, steps swapped with TiviMate's fast cross-fade. BACK walks one step
 * backwards and leaves the wizard from step one.
 */
@Composable
fun WizardScreen(
    repository: PlaylistRepository,
    fetchPlaylist: suspend (String) -> String,
    onExit: () -> Unit,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { AddPlaylistViewModel(scope, fetchPlaylist, repository) }
    val state by viewModel.state.collectAsState()
    BackHandler { if (!viewModel.back()) onExit() }
    LaunchedEffect(state.step) {
        if (state.step == WizardStep.DONE) onComplete()
    }
    ScreenCrossfade(
        target = state.step,
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(TELLY_ONBOARDING_BACKGROUND)),
    ) { step ->
        Row(Modifier.fillMaxSize()) {
            WizardScreenGuidance(state.copy(step = step))
            WizardScreenStep(step, state, viewModel, onExit, Modifier.weight(1f))
        }
    }
}

/** The middle + buttons columns for one wizard step. */
@Composable
private fun WizardScreenStep(
    step: WizardStep,
    state: WizardUiState,
    viewModel: AddPlaylistViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (step) {
        WizardStep.TYPE_CHOOSER ->
            WizardScreenTypeStep(
                onChoose = viewModel::chooseType,
                onCancel = onExit,
                modifier = modifier,
            )
        WizardStep.URL_ENTRY ->
            WizardScreenUrlStep(
                url = state.url,
                error = state.error,
                onUrlChange = viewModel::setUrl,
                onNext = viewModel::submitUrl,
                onBack = { if (!viewModel.back()) onExit() },
                modifier = modifier,
            )
        WizardStep.PROCESSING -> WizardScreenProcessing(modifier)
        else ->
            WizardScreenNameStep(
                name = state.name,
                kind = state.kind,
                onNameChange = viewModel::setName,
                onKindChange = viewModel::chooseKind,
                onNext = viewModel::confirm,
                onBack = { if (!viewModel.back()) onExit() },
                modifier = modifier,
            )
    }
}
