package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class VideoProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val nicheCategory: String,
    val topic: String,
    val hookHeadline: String,
    val fullScript: String,
    val scenesJson: String, // Serialized list of SceneItem
    val voiceStyle: String = "Energetic Storyteller", // "Deep Narrator", "Mysterious Male", "Hyper Female", "Calm Stoic", "Energetic Storyteller"
    val bgmTrackName: String = "Cyber Pulse", // "Cyber Pulse", "Deep Astral", "Cinematic Suspense", "Epic Motivation", "Lo-Fi Focus"
    val hashtags: String = "#AI #Viral #Shorts",
    val durationSeconds: Int = 20,
    val status: String = "READY", // DRAFT, READY, SCHEDULED, PUBLISHED
    val scheduledDailyTime: String = "09:00 AM",
    val targetPlatforms: String = "YouTube Shorts,TikTok,Instagram Reels",
    val publishedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val viewsSimulated: Int = 0,
    val likesSimulated: Int = 0
)
