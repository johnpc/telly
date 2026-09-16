package com.johncorser.telly

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

/**
 * Minimal [RecognitionListener]: forwards the best transcript to [onText]
 * and any failure to [onFail]; every other lifecycle callback is a no-op.
 * Android glue only — the tested decision logic lives in VoiceSearch.
 */
internal class VoiceRecognitionListener(
    private val onText: (String?) -> Unit,
    private val onFail: () -> Unit,
) : RecognitionListener {
    override fun onResults(results: Bundle?) {
        onText(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull())
    }

    override fun onError(error: Int) = onFail()

    override fun onReadyForSpeech(params: Bundle?) = Unit

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() = Unit

    override fun onPartialResults(partialResults: Bundle?) = Unit

    override fun onEvent(
        eventType: Int,
        params: Bundle?,
    ) = Unit
}
