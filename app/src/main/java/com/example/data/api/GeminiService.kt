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
        bgmTrack: String,
        languageCode: String = "pt-BR",
        languageName: String = "Português"
    ): Result<VideoProject> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.success(createLocalizedFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack, languageCode, languageName))
            }

            val prompt = """
                You are OSK Ai - the world's leading AI engine for high-impact viral TikTok, Shorts, and Reels videos.
                
                CRITICAL INSTRUCTION - TARGET LANGUAGE:
                You MUST write the title, hookHeadline, all scene narrationText, keywordsToHighlight, and relevant hashtags completely and fluently in $languageName (language code: $languageCode).
                
                Input Specifications:
                - Niche: "$nicheTitle"
                - Topic: "$topic"
                - Narration Persona: "$voiceStyle"
                - Soundtrack Vibe: "$bgmTrack"
                - Language: "$languageName ($languageCode)"

                Requirements:
                1. Scene 1 MUST be an explosive 3-second hook in $languageName with power emojis (🔥, ⚡, ⚔️, 🏎️, 🛡️, 🚀) that stops scrolling immediately.
                2. Structure 4 to 5 high-impact scenes (total 20-30 seconds duration).
                3. Camera motions: "Fast Whip Pan", "Aggressive Zoom In", "Cinematic Low Angle", "Speed Ramp", "Macro Cyber Pan".
                4. Visual themes: "cyberpunk_matrix", "supercar_midnight", "gym_beast_warrior", "space_fortress", or "stealth_battlestation".
                5. Highlight 2-3 aggressive high-impact keyword words per scene for kinetic subtitles in $languageName.
                6. Include 5-6 viral TikTok hashtags localized or global.

                Respond ONLY with raw valid JSON:
                {
                    "title": "Title in $languageName",
                    "hookHeadline": "Explosive 3-second hook in $languageName with emoji",
                    "targetPlatforms": "TikTok, Instagram Reels, YouTube Shorts",
                    "totalDurationSec": 24,
                    "hashtags": "#OSKAi #TikTokViral #Grindset #FYP",
                    "scenes": [
                        {
                            "sceneNumber": 1,
                            "narrationText": "Voiceover line in $languageName",
                            "visualPrompt": "Detailed visual prompt for high-impact scene",
                            "visualTheme": "cyberpunk_matrix",
                            "cameraMotion": "Aggressive Zoom In",
                            "durationSec": 4.5,
                            "keywordsToHighlight": ["KEYWORD1", "KEYWORD2"]
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
                return@withContext Result.success(createLocalizedFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack, languageCode, languageName))
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
            val hookHeadline = parsedVideoObj.optString("hookHeadline", "🔥 $topic")
            val duration = parsedVideoObj.optInt("totalDurationSec", 24)
            val hashtags = parsedVideoObj.optString("hashtags", "#OSKAi #TikTokViral #Grindset #FYP")
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
                return@withContext Result.success(createLocalizedFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack, languageCode, languageName))
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
                languageCode = languageCode,
                languageName = languageName,
                hashtags = hashtags,
                durationSeconds = duration,
                targetPlatforms = "TikTok, Instagram Reels, YouTube Shorts",
                status = "READY"
            )

            Result.success(video)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(createLocalizedFallbackVideo(nicheTitle, topic, voiceStyle, bgmTrack, languageCode, languageName))
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

    fun createLocalizedFallbackVideo(
        nicheTitle: String,
        topic: String,
        voiceStyle: String,
        bgmTrack: String,
        languageCode: String = "pt-BR",
        languageName: String = "Português"
    ): VideoProject {
        val scenes = when (languageCode) {
            "en-US" -> listOf(
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
            "es-LA" -> listOf(
                SceneItem(
                    sceneNumber = 1,
                    narrationText = "🔥 ¡Detén el scroll ahora mismo! ¡Mientras todos duermen, así es como construyes una dominación imparable en $topic!",
                    visualPrompt = "Silueta guerrera en gimnasio oscuro rodeada de chispas azul neón",
                    visualTheme = "gym_beast_warrior",
                    cameraMotion = "Aggressive Zoom In",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("IMPARABLE", "DOMINIO", "PODER")
                ),
                SceneItem(
                    sceneNumber = 2,
                    narrationText = "⚡ La mayoría busca motivación. Los verdaderos ganadores forjan una disciplina despiadada cada día.",
                    visualPrompt = "Superdeportivo negro mate acelerando por calles cyberpunk",
                    visualTheme = "supercar_midnight",
                    cameraMotion = "Speed Ramp",
                    durationSec = 5.0f,
                    keywordsToHighlight = listOf("DISCIPLINA", "GANADORES", "ENERGÍA")
                ),
                SceneItem(
                    sceneNumber = 3,
                    narrationText = "⚔️ Cuando dominas tu enfoque y eliminas las distracciones, el éxito se vuelve totalmente inevitable.",
                    visualPrompt = "Estación de batalla de alta tecnología con pantallas tácticas",
                    visualTheme = "stealth_battlestation",
                    cameraMotion = "Cinematic Low Angle",
                    durationSec = 5.5f,
                    keywordsToHighlight = listOf("ENFOQUE", "INEVITABLE", "ÉXITO")
                ),
                SceneItem(
                    sceneNumber = 4,
                    narrationText = "🛡️ Deja un '⚡' en los comentarios si estás listo para cumplir tus metas. ¡Guarda este video!",
                    visualPrompt = "Emblema cibernético con pulso neón y marca de agua TikTok",
                    visualTheme = "cyberpunk_matrix",
                    cameraMotion = "Fast Whip Pan",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("METAS", "DISCIPLINA", "ACCION")
                )
            )
            "ja-JP" -> listOf(
                SceneItem(
                    sceneNumber = 1,
                    narrationText = "🔥 今すぐスクロールを止めろ！誰もが寝ている間に、$topic で圧倒的な勝利を掴む方法がこれだ！",
                    visualPrompt = "ネオンブルーのオーラを纏った戦士のシルエット",
                    visualTheme = "gym_beast_warrior",
                    cameraMotion = "Aggressive Zoom In",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("勝利", "圧倒的", "覚醒")
                ),
                SceneItem(
                    sceneNumber = 2,
                    narrationText = "⚡ 凡人はモチベーションを探す。本物は毎日の無敵の規律を創り出すのだ。",
                    visualPrompt = "サイバーパンクの夜街を爆走する漆黒のハイパーカー",
                    visualTheme = "supercar_midnight",
                    cameraMotion = "Speed Ramp",
                    durationSec = 5.0f,
                    keywordsToHighlight = listOf("規律", "本物", "突破")
                ),
                SceneItem(
                    sceneNumber = 3,
                    narrationText = "⚔️ 雑音を遮断し集中を極めた時、成功は必然となる！",
                    visualPrompt = "超近未来タクティカルコンソールとホログラム",
                    visualTheme = "stealth_battlestation",
                    cameraMotion = "Cinematic Low Angle",
                    durationSec = 5.5f,
                    keywordsToHighlight = listOf("集中", "必然", "制覇")
                ),
                SceneItem(
                    sceneNumber = 4,
                    narrationText = "🛡️ 今日の目標を達成する奴はコメントに「⚡」を残せ！今すぐ保存！",
                    visualPrompt = "輝くサイバーエンブレム",
                    visualTheme = "cyberpunk_matrix",
                    cameraMotion = "Fast Whip Pan",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("目標", "達成", "覚悟")
                )
            )
            else -> listOf(
                // Portuguese default
                SceneItem(
                    sceneNumber = 1,
                    narrationText = "🔥 Pare tudo agora mesmo! Enquanto todos estão dormindo, é assim que você constrói uma disciplina inabalável em $topic!",
                    visualPrompt = "Silhueta cinematográfica em academia escura com faíscas azul neon e anilhas pesadas",
                    visualTheme = "gym_beast_warrior",
                    cameraMotion = "Aggressive Zoom In",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("INABALÁVEL", "DOMÍNIO", "DISCIPLINA")
                ),
                SceneItem(
                    sceneNumber = 2,
                    narrationText = "⚡ A maioria das pessoas espera por motivação. Quem realmente vence constrói uma disciplina implacável todos os dias.",
                    visualPrompt = "Supercarro preto fosco biturbo acelerando na chuva noturna com labaredas no escapamento",
                    visualTheme = "supercar_midnight",
                    cameraMotion = "Speed Ramp",
                    durationSec = 5.0f,
                    keywordsToHighlight = listOf("IMPLACÁVEL", "VENCEDORES", "FOCO")
                ),
                SceneItem(
                    sceneNumber = 3,
                    narrationText = "⚔️ Quando você domina seu foco e elimina distrações, o sucesso deixa de ser uma sorte e se torna inevitável.",
                    visualPrompt = "Setup gamer stealth tático com monitores curvos e telemetria holográfica",
                    visualTheme = "stealth_battlestation",
                    cameraMotion = "Cinematic Low Angle",
                    durationSec = 5.5f,
                    keywordsToHighlight = listOf("FOCO", "INEVITÁVEL", "PODER")
                ),
                SceneItem(
                    sceneNumber = 4,
                    narrationText = "🛡️ Deixe um '⚡' nos comentários se você vai executar suas metas hoje. Salve esse vídeo e continue no foco!",
                    visualPrompt = "Emblema metálico cyber com pulso neon azul e vermelho e marca d'água TikTok",
                    visualTheme = "cyberpunk_matrix",
                    cameraMotion = "Fast Whip Pan",
                    durationSec = 4.5f,
                    keywordsToHighlight = listOf("METAS", "EXECUÇÃO", "FOCO")
                )
            )
        }

        val hashtags = when (languageCode) {
            "en-US" -> "#OSKAi #GymTok #Motivation #Grindset #FYP #Discipline"
            "es-LA" -> "#OSKAi #Motivacion #Disciplina #GymTok #FYP #Exito"
            "ja-JP" -> "#OSKAi #筋トレ #モチベーション #TikTok教室 #FYP #規律"
            "de-DE" -> "#OSKAi #Motivation #Disziplin #GymTok #Erfolg #FYP"
            "fr-FR" -> "#OSKAi #Motivation #Discipline #Succes #PourToi #FYP"
            "it-IT" -> "#OSKAi #Motivazione #Disciplina #Successo #PerTe #FYP"
            else -> "#OSKAi #Motivacao #Disciplina #GymTok #Foco #FYP"
        }

        val headline = when (languageCode) {
            "en-US" -> "🔥 Stop scrolling! Build unstoppable $topic dominance!"
            "es-LA" -> "🔥 ¡Detén el scroll! ¡Domina $topic con disciplina implacable!"
            "ja-JP" -> "🔥 スクロール停止！$topic で圧倒的覚醒！"
            "de-DE" -> "🔥 Hör auf zu scrollen! Erobere $topic mit eiserner Disziplin!"
            "fr-FR" -> "🔥 Arrête de scroller ! Domine $topic avec détermination !"
            "it-IT" -> "🔥 Fermati subito! Domina $topic con disciplina pura!"
            else -> "🔥 Pare tudo! Domine $topic com disciplina inabalável!"
        }

        return VideoProject(
            title = "⚡ $topic ($languageName)",
            nicheCategory = nicheTitle,
            topic = topic,
            hookHeadline = headline,
            fullScript = scenes.joinToString(" ") { it.narrationText },
            scenesJson = SceneJsonHelper.toJson(scenes),
            voiceStyle = voiceStyle,
            bgmTrackName = bgmTrack,
            languageCode = languageCode,
            languageName = languageName,
            hashtags = hashtags,
            durationSeconds = 20,
            targetPlatforms = "TikTok, Instagram Reels, YouTube Shorts",
            status = "READY"
        )
    }
}
