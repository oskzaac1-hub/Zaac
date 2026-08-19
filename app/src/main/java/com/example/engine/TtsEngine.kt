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
                // Default to Portuguese or system locale
                val defaultLocale = Locale("pt", "BR")
                val result = textToSpeech?.setLanguage(defaultLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.US)
                }
                isReady = true
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

    fun speak(
        text: String,
        voiceStyle: String = "Narrador Grave Phonk BR",
        languageCode: String = "pt-BR",
        onDone: (() -> Unit)? = null
    ) {
        if (!isReady || text.isBlank()) {
            onDone?.invoke()
            return
        }

        this.onDoneCallback = onDone

        // Set TTS Locale corresponding to language code
        val targetLocale = when (languageCode) {
            "pt-BR" -> Locale("pt", "BR")
            "en-US" -> Locale.US
            "es-LA", "es-ES" -> Locale("es", "ES")
            "ja-JP" -> Locale.JAPAN
            "de-DE" -> Locale.GERMAN
            "fr-FR" -> Locale.FRENCH
            "it-IT" -> Locale.ITALIAN
            else -> Locale("pt", "BR")
        }

        try {
            textToSpeech?.setLanguage(targetLocale)
        } catch (e: Exception) {
            Log.w("TtsEngine", "Failed setting locale $targetLocale", e)
        }

        // Configure pitch and speed based on voice style
        when {
            voiceStyle.contains("Grave", ignoreCase = true) || voiceStyle.contains("Phonk", ignoreCase = true) || voiceStyle.contains("Deep", ignoreCase = true) -> {
                textToSpeech?.setPitch(0.72f)
                textToSpeech?.setSpeechRate(0.96f)
            }
            voiceStyle.contains("Coach", ignoreCase = true) || voiceStyle.contains("Motivação", ignoreCase = true) || voiceStyle.contains("Unstoppable", ignoreCase = true) -> {
                textToSpeech?.setPitch(0.92f)
                textToSpeech?.setSpeechRate(1.12f)
            }
            voiceStyle.contains("Cyber", ignoreCase = true) || voiceStyle.contains("Tático", ignoreCase = true) || voiceStyle.contains("Operative", ignoreCase = true) -> {
                textToSpeech?.setPitch(0.85f)
                textToSpeech?.setSpeechRate(1.05f)
            }
            voiceStyle.contains("Estoico", ignoreCase = true) || voiceStyle.contains("Stoic", ignoreCase = true) || voiceStyle.contains("Zen", ignoreCase = true) -> {
                textToSpeech?.setPitch(0.80f)
                textToSpeech?.setSpeechRate(0.90f)
            }
            voiceStyle.contains("Viral", ignoreCase = true) || voiceStyle.contains("TikTok", ignoreCase = true) || voiceStyle.contains("Hero", ignoreCase = true) -> {
                textToSpeech?.setPitch(1.05f)
                textToSpeech?.setSpeechRate(1.18f)
            }
            else -> {
                textToSpeech?.setPitch(0.90f)
                textToSpeech?.setSpeechRate(1.05f)
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
