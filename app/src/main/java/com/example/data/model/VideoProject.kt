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
    val voiceStyle: String = "Narrador Grave Phonk BR",
    val bgmTrackName: String = "Drift Phonk Beast",
    val languageCode: String = "pt-BR",
    val languageName: String = "Português",
    val hashtags: String = "#OSKAi #TikTokViral #Grindset #FYP #Motivacao",
    val durationSeconds: Int = 20,
    val status: String = "READY", // DRAFT, READY, SCHEDULED, PUBLISHED
    val scheduledDailyTime: String = "09:00 AM",
    val targetPlatforms: String = "TikTok, Instagram Reels, YouTube Shorts",
    val publishedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val viewsSimulated: Int = 0,
    val likesSimulated: Int = 0
)
