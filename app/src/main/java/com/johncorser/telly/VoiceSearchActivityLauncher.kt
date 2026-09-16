package com.johncorser.telly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.johncorser.telly.features.search.VoiceSearch

/**
 * MainActivity's platform glue for the search mic orb. Android TV has NO
 * ACTION_RECOGNIZE_SPEECH activity (that intent only resolves on phones), so
 * voice runs through the bound SpeechRecognizer service + RECORD_AUDIO; the
 * tested decision logic stays in [VoiceSearch]. A device without a recognizer
 * degrades to an honest toast, and a denied mic says so.
 */
internal class VoiceSearchActivityLauncher(
    private val activity: ComponentActivity,
) {
    val voice = VoiceSearch(launch = ::launchRecognizer, notify = ::toast)

    // Built lazily on first orb press, never at construction: the launcher
    // is a MainActivity field so its <init> runs before attachBaseContext,
    // and SpeechRecognizer touches the (still-null) Context.
    private val recognizer: SpeechRecognizer? by lazy {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            SpeechRecognizer.createSpeechRecognizer(activity).apply {
                setRecognitionListener(
                    VoiceRecognitionListener(onText = voice::onResult, onFail = ::onRecognizerError),
                )
            }
        } else {
            null
        }
    }

    private val micPermission =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) listen() else toast(MIC_DENIED_MESSAGE)
        }

    /** Orb OK: with the mic granted start listening now, else ask for it. */
    private fun launchRecognizer(): Boolean {
        if (recognizer == null) return false
        requestOrListen()
        return true
    }

    private fun requestOrListen() {
        if (hasMic()) listen() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun listen() = recognizer?.startListening(recognizeIntent())

    private fun onRecognizerError() = toast(NO_MATCH_MESSAGE)

    private fun hasMic(): Boolean =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun toast(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    private fun recognizeIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )

    private companion object {
        const val MIC_DENIED_MESSAGE = "Microphone permission is needed for voice search"
        const val NO_MATCH_MESSAGE = "Didn't catch that — try again"
    }
}
