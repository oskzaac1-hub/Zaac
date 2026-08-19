package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publish_logs")
data class PublishLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val videoTitle: String,
    val platform: String, // "YouTube Shorts", "TikTok", "Instagram Reels", "X / Twitter"
    val publishedAt: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "SCHEDULED", "QUEUED", "FAILED"
    val logMessage: String,
    val postUrl: String = "https://youtube.com/shorts/ai_sample"
)
