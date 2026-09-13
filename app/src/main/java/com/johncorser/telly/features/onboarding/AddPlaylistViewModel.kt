package com.johncorser.telly.features.onboarding

import com.johncorser.telly.features.playlist.M3uParser
import com.johncorser.telly.features.playlist.PlaylistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Plain JVM-testable state holder for the add-playlist wizard. The UI feeds
 * user intents in; [state] carries the current step, URL draft and errors out.
 */
class AddPlaylistViewModel(
    private val scope: CoroutineScope,
    private val fetchPlaylist: suspend (String) -> String,
    private val repository: PlaylistRepository,
) {
    private val mutableState = MutableStateFlow(WizardUiState())
    val state: StateFlow<WizardUiState> = mutableState.asStateFlow()
    private var loadJob: Job? = null

    /** Only the M3U path exists in this slice; other types are inert. */
    fun chooseType(type: PlaylistType) {
        if (type == PlaylistType.M3U) {
            mutableState.update { it.copy(step = WizardStep.URL_ENTRY) }
        }
    }

    fun setUrl(url: String) {
        mutableState.update { it.copy(url = url, error = null) }
    }

    /** Validates the URL (http/https only) and kicks off fetch + parse. */
    fun submitUrl() {
        val url = state.value.url.trim()
        if (url.toHttpUrlOrNull() == null) {
            mutableState.update { it.copy(error = WizardError.INVALID_URL) }
            return
        }
        mutableState.update { it.copy(step = WizardStep.PROCESSING, error = null) }
        loadJob = scope.launch { load(url) }
    }

    private suspend fun load(url: String) {
        runCatching { M3uParser.parse(fetchPlaylist(url)) }
            .onSuccess { playlist ->
                repository.add(url, playlist)
                mutableState.update {
                    it.copy(step = WizardStep.DONE, channelCount = playlist.channels.size)
                }
            }.onFailure { failure ->
                if (failure is CancellationException) throw failure
                mutableState.update {
                    it.copy(step = WizardStep.URL_ENTRY, error = WizardError.LOAD_FAILED)
                }
            }
    }

    /** BACK semantics: one step backwards; false means "leave the wizard". */
    fun back(): Boolean =
        when (state.value.step) {
            WizardStep.URL_ENTRY -> {
                mutableState.update { it.copy(step = WizardStep.TYPE_CHOOSER, error = null) }
                true
            }
            WizardStep.PROCESSING -> {
                loadJob?.cancel()
                mutableState.update { it.copy(step = WizardStep.URL_ENTRY) }
                true
            }
            else -> false
        }
}
