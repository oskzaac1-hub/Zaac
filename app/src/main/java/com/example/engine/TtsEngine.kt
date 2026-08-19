package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsEngine(context: Context, private val onInitCompleted: (Boolean) -> Unit = {}) {

    private var textToSpeech: TextToSpeech? = null
    var isReady: Boolean = false
        private set

    private var onDoneCallback: (() -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                isReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
                setupListener()
                onInitCompleted(isReady)
            } else {
                Log.e("TtsEngine", "TTS Init failed with status: $status")
                isReady = false
                onInitCompleted(false)
            }
        }
    }

    private fun setupListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onDoneCallback?.invoke()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDoneCallback?.invoke()
            }
        })
    }

    fun speak(text: String, voiceStyle: String = "Energetic Storyteller", onDone: (() -> Unit)? = null) {
        if (!isReady || text.isBlank()) {
            onDone?.invoke()
            return
        }

        this.onDoneCallback = onDone

        // Configure pitch and speed based on voice style
        when (voiceStyle) {
            "Deep Narrator" -> {
                textToSpeech?.setPitch(0.8f)
                textToSpeech?.setSpeechRate(0.95f)
            }
            "Mysterious Male" -> {
                textToSpeech?.setPitch(0.85f)
                textToSpeech?.setSpeechRate(0.9f)
            }
            "Hyper Female" -> {
                textToSpeech?.setPitch(1.25f)
                textToSpeech?.setSpeechRate(1.15f)
            }
            "Calm Stoic" -> {
                textToSpeech?.setPitch(0.95f)
                textToSpeech?.setSpeechRate(0.92f)
            }
            else -> { // "Energetic Storyteller"
                textToSpeech?.setPitch(1.05f)
                textToSpeech?.setSpeechRate(1.08f)
            }
        }

        val utteranceId = "scene_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("TtsEngine", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsEngine", "Error shutting down TTS", e)
        }
    }
}
