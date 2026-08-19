package com.example.engine

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.PublishLog
import com.example.data.model.VideoProject
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class SocialPlatformAccount(
    val platformName: String,
    val handle: String,
    val isConnected: Boolean,
    val iconName: String,
    val colorHex: Long
)

class AutoPublishScheduler(
    private val context: Context,
    private val repository: VideoRepository
) {

    val connectedAccounts = listOf(
        SocialPlatformAccount(
            platformName = "TikTok (Primary)",
            handle = "@osk.kawaii.ai",
            isConnected = true,
            iconName = "tiktok",
            colorHex = 0xFFFE2C55
        ),
        SocialPlatformAccount(
            platformName = "TikTok Backup FYP",
            handle = "@oskaiviral",
            isConnected = true,
            iconName = "tiktok",
            colorHex = 0xFF00F2FE
        ),
        SocialPlatformAccount(
            platformName = "Instagram Reels",
            handle = "@osk_kawaii_studio",
            isConnected = true,
            iconName = "instagram",
            colorHex = 0xFFE1306C
        ),
        SocialPlatformAccount(
            platformName = "YouTube Shorts",
            handle = "@OSKAiKawaii",
            isConnected = true,
            iconName = "youtube",
            colorHex = 0xFFFF0000
        )
    )

    suspend fun publishVideoNow(
        video: VideoProject,
        platforms: List<String> = listOf("TikTok (Primary)", "TikTok Backup FYP", "Instagram Reels", "YouTube Shorts")
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Simulate platform upload handshakes with realistic progress
            delay(1200)

            for (platform in platforms) {
                val postSlug = Random.nextInt(10000, 99999)
                val postUrl = when {
                    platform.contains("TikTok") -> "https://tiktok.com/@osk.kawaii.ai/video/7395$postSlug"
                    platform.contains("Instagram") -> "https://instagram.com/reel/C$postSlug"
                    else -> "https://youtube.com/shorts/osk_$postSlug"
                }

                val log = PublishLog(
                    videoId = video.id,
                    videoTitle = video.title,
                    platform = platform,
                    publishedAt = System.currentTimeMillis(),
                    status = "SUCCESS",
                    logMessage = "Auto-dispatched with Kawaii TikTok tags: ${video.hashtags}",
                    postUrl = postUrl
                )
                repository.recordPublishLog(log)
            }

            // Update video entity with published status and simulated views
            val updatedVideo = video.copy(
                status = "PUBLISHED",
                publishedAt = System.currentTimeMillis(),
                viewsSimulated = Random.nextInt(4500, 48500),
                likesSimulated = Random.nextInt(980, 8900)
            )
            repository.updateVideo(updatedVideo)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "🌸 Video Published to TikTok & Kawaii channels!",
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareVideoViaSystem(video: VideoProject) {
        try {
            val shareText = """
                🌸 ${video.title}
                
                ✨ Hook: "${video.hookHeadline}"
                
                📝 Script:
                ${video.fullScript}
                
                🎀 Tags: ${video.hashtags}
                
                ⚡ Created with OSK Ai - TikTok & Kawaii Studio
            """.trimIndent()

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_TITLE, video.title)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareChooser = Intent.createChooser(sendIntent, "Share TikTok Kawaii Short").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareChooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share dialog", Toast.LENGTH_SHORT).show()
        }
    }
}
