package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PublishLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PublishLogDao {
    @Query("SELECT * FROM publish_logs ORDER BY publishedAt DESC")
    fun getAllLogs(): Flow<List<PublishLog>>

    @Query("SELECT * FROM publish_logs WHERE videoId = :videoId ORDER BY publishedAt DESC")
    fun getLogsForVideo(videoId: Long): Flow<List<PublishLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PublishLog): Long

    @Query("DELETE FROM publish_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM publish_logs")
    suspend fun clearAllLogs()
}
