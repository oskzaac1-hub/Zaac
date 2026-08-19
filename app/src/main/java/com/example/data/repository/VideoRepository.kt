package com.example.data.repository

import com.example.data.db.PublishLogDao
import com.example.data.db.VideoProjectDao
import com.example.data.model.PublishLog
import com.example.data.model.VideoProject
import kotlinx.coroutines.flow.Flow

class VideoRepository(
    private val videoDao: VideoProjectDao,
    private val logDao: PublishLogDao
) {
    val allVideos: Flow<List<VideoProject>> = videoDao.getAllVideos()
    val scheduledVideos: Flow<List<VideoProject>> = videoDao.getScheduledVideos()
    val publishedVideos: Flow<List<VideoProject>> = videoDao.getPublishedVideos()
    val allPublishLogs: Flow<List<PublishLog>> = logDao.getAllLogs()

    suspend fun getVideoById(id: Long): VideoProject? = videoDao.getVideoById(id)

    suspend fun saveVideo(video: VideoProject): Long = videoDao.insertVideo(video)

    suspend fun updateVideo(video: VideoProject) = videoDao.updateVideo(video)

    suspend fun deleteVideo(video: VideoProject) = videoDao.deleteVideo(video)

    suspend fun deleteVideoById(id: Long) = videoDao.deleteVideoById(id)

    suspend fun recordPublishLog(log: PublishLog): Long = logDao.insertLog(log)

    suspend fun clearPublishLogs() = logDao.clearAllLogs()
}
