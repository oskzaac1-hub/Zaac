package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.SceneItem
import com.example.data.model.SceneJsonHelper
import com.example.data.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun generateVideoForNiche(
        nicheTitle: String,
        topic: String,
        voiceStyle: String,
        bgmTrack: String
    ): Result<VideoProject> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.success(createMasculineFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack))
            }

            val prompt = """
                You are OSK Ai - the world's most powerful AI for high-impact, high-retention viral TikTok, Shorts and Reels videos.
                Generate a viral 9:16 short video script:
                - Niche: "$nicheTitle"
                - Topic: "$topic"
                - Narration Persona: "$voiceStyle"
                - Soundtrack Vibe: "$bgmTrack"

                Requirements:
                1. Scene 1 MUST be an explosive 3-second hook with power emojis (🔥, ⚡, ⚔️, 🏎️, 🛡️, 🚀) that stops scrolling instantly.
                2. Structure 4 to 5 high-impact scenes (total 20-30 seconds duration).
                3. Camera motions: "Fast Whip Pan", "Aggressive Zoom In", "Cinematic Low Angle", "Speed Ramp", "Macro Cyber Pan".
                4. Visual themes: "cyberpunk_matrix", "supercar_midnight", "gym_beast_warrior", "space_fortress", or "stealth_battlestation".
                5. Highlight 2-3 aggressive high-impact keyword words per scene for kinetic subtitles.
                6. Include 5-6 viral TikTok hashtags (#GymTok, #CarTok, #Discipline, #TikTokViral, #FYP).

                Respond ONLY with raw valid JSON:
                {
                    "title": "Title of the video",
                    "hookHeadline": "Explosive 3-second hook with emoji",
                    "targetPlatforms": "TikTok, Instagram Reels, YouTube Shorts",
                    "totalDurationSec": 24,
                    "hashtags": "#OSKAi #TikTokViral #Grindset #FYP #Motivation",
                    "scenes": [
                        {
                            "sceneNumber": 1,
                            "narrationText": "Voiceover line for scene 1",
                            "visualPrompt": "Detailed visual prompt for high-impact scene",
                            "visualTheme": "cyberpunk_matrix",
                            "cameraMotion": "Aggressive Zoom In",
                            "durationSec": 4.5,
                            "keywordsToHighlight": ["POWER", "DISCIPLINE"]
                        }
                    ]
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                return@withContext Result.success(createMasculineFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack))
            }

            val rawJson = JSONObject(responseBody)
            val candidates = rawJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedText = cleanJsonString(textPart)
            val parsedVideoObj = JSONObject(cleanedText)

            val title = parsedVideoObj.optString("title", "$topic ⚡ OSK Ai")
            val hookHeadline = parsedVideoObj.optString("hookHeadline", "🔥 They doubted you. Now show them what true discipline looks like!")
            val duration = parsedVideoObj.optInt("totalDurationSec", 24)
            val hashtags = parsedVideoObj.optString("hashtags", "#OSKAi #TikTokViral #Grindset #FYP #Motivation")
            val rawScenes = parsedVideoObj.optJSONArray("scenes") ?: JSONArray()

            val sceneItems = mutableListOf<SceneItem>()
            val fullScriptBuilder = StringBuilder()

            for (i in 0 until rawScenes.length()) {
                val sc = rawScenes.optJSONObject(i) ?: continue
                val sceneNumber = sc.optInt("sceneNumber", i + 1)
                val narration = sc.optString("narrationText", "")
                val visualPrompt = sc.optString("visualPrompt", "Cinematic dark masculine visual")
                val visualTheme = sc.optString("visualTheme", "cyberpunk_matrix")
                val cameraMotion = sc.optString("cameraMotion", "Aggressive Zoom In")
                val sceneDuration = sc.optDouble("durationSec", 5.0).toFloat()
                val kwArray = sc.optJSONArray("keywordsToHighlight")
                val keywords = mutableListOf<String>()
                if (kwArray != null) {
                    for (k in 0 until kwArray.length()) {
                        keywords.add(kwArray.optString(k))
                    }
                }

                fullScriptBuilder.append(narration).append(" ")
                sceneItems.add(
                    SceneItem(
                        sceneNumber = sceneNumber,
                        narrationText = narration,
                        visualPrompt = visualPrompt,
                        visualTheme = visualTheme,
                        cameraMotion = cameraMotion,
                        durationSec = sceneDuration,
                        keywordsToHighlight = keywords
                    )
                )
            }

            if (sceneItems.isEmpty()) {
                return@withContext Result.success(createMasculineFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack))
            }

            val video = VideoProject(
                title = title,
                nicheCategory = nicheTitle,
                topic = topic,
                hookHeadline = hookHeadline,
                fullScript = fullScriptBuilder.toString().trim(),
                scenesJson = SceneJsonHelper.toJson(sceneItems),
                voiceStyle = voiceStyle,
                bgmTrackName = bgmTrack,
                hashtags = hashtags,
                durationSeconds = duration,
                targetPlatforms = "TikTok, Instagram Reels, YouTube Shorts",
                status = "READY"
            )

            Result.success(video)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(createMasculineFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack))
        }
    }

    private fun cleanJsonString(input: String): String {
        var trimmed = input.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json").trim()
        }
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```").trim()
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```").trim()
        }
        return trimmed
    }

    fun createMasculineFallbackVideo(
        nicheTitle: String,
        topic: String,
        voiceStyle: String,
        bgmTrack: String
    ): VideoProject {
        val scenes = listOf(
            SceneItem(
                sceneNumber = 1,
                narrationText = "🔥 Stop scrolling right now! While everyone else is sleeping, this is how you build unstoppable $topic dominance!",
                visualPrompt = "Cinematic dark gym warrior silhouette surrounded by glowing electric blue sparks and heavy barbell plates",
                visualTheme = "gym_beast_warrior",
                cameraMotion = "Aggressive Zoom In",
                durationSec = 4.5f,
                keywordsToHighlight = listOf("UNSTOPPABLE", "DOMINANCE", "GRIND")
            ),
            SceneItem(
                sceneNumber = 2,
                narrationText = "⚡ Most people search for motivation. Real beasts build ruthless, unbreakable discipline every single day.",
                visualPrompt = "Dark matte black twin turbo hypercar accelerating through wet cyberpunk streets with crimson exhaust flames",
                visualTheme = "supercar_midnight",
                cameraMotion = "Speed Ramp",
                durationSec = 5.0f,
                keywordsToHighlight = listOf("RUTHLESS", "DISCIPLINE", "POWER")
            ),
            SceneItem(
                sceneNumber = 3,
                narrationText = "⚔️ When you master your focus and eliminate every distraction, success stops being a hope and becomes inevitable.",
                visualPrompt = "Stealth high-tech battlestation with curved multi-monitor tactical displays and titanium HUD telemetry",
                visualTheme = "stealth_battlestation",
                cameraMotion = "Cinematic Low Angle",
                durationSec = 5.5f,
                keywordsToHighlight = listOf("FOCUS", "INEVITABLE", "TACTICAL")
            ),
            SceneItem(
                sceneNumber = 4,
                narrationText = "🛡️ Drop a '⚡' in the comments if you're executing your goals today. Save this and keep grinding!",
                visualPrompt = "Metallic glowing cyber skull emblem with electric blue and crimson neon pulse, high-tech TikTok watermark",
                visualTheme = "cyberpunk_matrix",
                cameraMotion = "Fast Whip Pan",
                durationSec = 4.5f,
                keywordsToHighlight = listOf("EXECUTE", "GOALS", "GRIND")
            )
        )

        return VideoProject(
            title = "⚡ $topic (Viral Edition)",
            nicheCategory = nicheTitle,
            topic = topic,
            hookHeadline = "🔥 Stop scrolling! This is how you build unstoppable $topic dominance!",
            fullScript = scenes.joinToString(" ") { it.narrationText },
            scenesJson = SceneJsonHelper.toJson(scenes),
            voiceStyle = voiceStyle,
            bgmTrackName = bgmTrack,
            hashtags = "#OSKAi #GymTok #MotivationTok #Grindset #SigmaMindset #FYP #Discipline",
            durationSeconds = 20,
            targetPlatforms = "TikTok, Instagram Reels, YouTube Shorts",
            status = "READY"
        )
    }
}
