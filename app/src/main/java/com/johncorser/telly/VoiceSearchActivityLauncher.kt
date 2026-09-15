package com.johncorser.telly

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.johncorser.telly.features.search.VoiceSearch

/**
 * MainActivity's platform glue for the search mic orb (the external-player
 * launcher precedent; decision logic lives in the tested [VoiceSearch]):
 * launches the system speech recognizer through an ActivityResult contract
 * and feeds the transcript back. A device without a recognizer (TV
 * emulators, some Shields) throws on launch, which [VoiceSearch] turns
 * into an honest toast.
 */
internal class VoiceSearchActivityLauncher(
    private val activity: ComponentActivity,
) {
    val voice =
        VoiceSearch(
            launch = ::launchRecognizer,
            notify = { message -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show() },
        )

    private val launcher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            voice.onResult(result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull())
        }

    private fun launchRecognizer(): Boolean =
        runCatching {
            launcher.launch(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                ),
            )
        }.isSuccess
}
