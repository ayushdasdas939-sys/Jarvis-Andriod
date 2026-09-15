package com.ayush.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var talkButton: Button
    private lateinit var tts: TextToSpeech

    private val speechRequestCode = 100
    private val audioPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusText = TextView(this).apply {
            text = "JARVIS READY"
            textSize = 24f
        }

        commandText = TextView(this).apply {
            text = "Tap TALK TO JARVIS"
            textSize = 18f
        }

        talkButton = Button(this).apply {
            text = "🎙️ TALK TO JARVIS"
            setOnClickListener {
                startListening()
            }
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
            addView(statusText)
            addView(commandText)
            addView(talkButton)
        }

        setContentView(layout)

        tts = TextToSpeech(this, this)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                audioPermissionCode
            )
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "Speech recognition unavailable"
            speak("Speech recognition is not available.")
            return
        }

        statusText.text = "LISTENING..."

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }

        startActivityForResult(intent, speechRequestCode)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechRequestCode &&
            resultCode == RESULT_OK
        ) {
            val results =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            val command = results?.firstOrNull()

            if (!command.isNullOrEmpty()) {
                commandText.text = command
                statusText.text = "COMMAND RECEIVED"
                speak("I heard you say $command")
            } else {
                statusText.text = "NO COMMAND"
            }
        }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_RESPONSE"
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
