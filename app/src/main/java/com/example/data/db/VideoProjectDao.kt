package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoProject
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoProjectDao {
    @Query("SELECT * FROM video_projects ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<VideoProject>>

    @Query("SELECT * FROM video_projects WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoProject?

    @Query("SELECT * FROM video_projects WHERE status = 'SCHEDULED' ORDER BY createdAt ASC")
    fun getScheduledVideos(): Flow<List<VideoProject>>

    @Query("SELECT * FROM video_projects WHERE status = 'PUBLISHED' ORDER BY publishedAt DESC")
    fun getPublishedVideos(): Flow<List<VideoProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoProject): Long

    @Update
    suspend fun updateVideo(video: VideoProject)

    @Delete
    suspend fun deleteVideo(video: VideoProject)

    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteVideoById(id: Long)
}
