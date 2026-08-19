package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.PublishLog
import com.example.data.model.SceneItem
import com.example.data.model.SceneJsonHelper
import com.example.data.model.TrendingNiche
import com.example.data.model.TrendingNichesRepository
import com.example.data.model.VideoProject
import com.example.data.repository.VideoRepository
import com.example.engine.AutoPublishScheduler
import com.example.engine.TtsEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenerationState {
    object Idle : GenerationState
    data class Generating(val stepMessage: String, val progress: Float) : GenerationState
    data class Success(val video: VideoProject) : GenerationState
    data class Error(val message: String) : GenerationState
}

enum class NavigationTab {
    DISCOVER,
    STUDIO,
    PLAYER,
    AUTO_POST,
    LIBRARY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = VideoRepository(db.videoProjectDao(), db.publishLogDao())
    private val geminiService = GeminiService()
    val autoPublisher = AutoPublishScheduler(application, repository)

    private var ttsEngine: TtsEngine? = null

    // Navigation & UI States
    private val _currentTab = MutableStateFlow(NavigationTab.DISCOVER)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    private val _selectedNiche = MutableStateFlow<TrendingNiche>(TrendingNichesRepository.curatedNiches.first())
    val selectedNiche: StateFlow<TrendingNiche> = _selectedNiche.asStateFlow()

    private val _customTopicInput = MutableStateFlow("")
    val customTopicInput: StateFlow<String> = _customTopicInput.asStateFlow()

    private val _selectedVoiceStyle = MutableStateFlow("Energetic Storyteller")
    val selectedVoiceStyle: StateFlow<String> = _selectedVoiceStyle.asStateFlow()

    private val _selectedBgmTrack = MutableStateFlow("Cyber Pulse")
    val selectedBgmTrack: StateFlow<String> = _selectedBgmTrack.asStateFlow()

    // Active Video Player States
    private val _activeVideo = MutableStateFlow<VideoProject?>(null)
    val activeVideo: StateFlow<VideoProject?> = _activeVideo.asStateFlow()

    private val _activeScenes = MutableStateFlow<List<SceneItem>>(emptyList())
    val activeScenes: StateFlow<List<SceneItem>> = _activeScenes.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _sceneProgress = MutableStateFlow(0f)
    val sceneProgress: StateFlow<Float> = _sceneProgress.asStateFlow()

    private val _isVoiceTtsEnabled = MutableStateFlow(true)
    val isVoiceTtsEnabled: StateFlow<Boolean> = _isVoiceTtsEnabled.asStateFlow()

    // Auto-Post Daily Automation State
    private val _isAutoPilotEnabled = MutableStateFlow(true)
    val isAutoPilotEnabled: StateFlow<Boolean> = _isAutoPilotEnabled.asStateFlow()

    private val _dailyScheduleTime = MutableStateFlow("09:00 AM")
    val dailyScheduleTime: StateFlow<String> = _dailyScheduleTime.asStateFlow()

    private val _enabledPlatforms = MutableStateFlow(setOf("YouTube Shorts", "TikTok", "Instagram Reels"))
    val enabledPlatforms: StateFlow<Set<String>> = _enabledPlatforms.asStateFlow()

    private val _isPublishingNow = MutableStateFlow(false)
    val isPublishingNow: StateFlow<Boolean> = _isPublishingNow.asStateFlow()

    // Database streams
    val allVideos: StateFlow<List<VideoProject>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledVideos: StateFlow<List<VideoProject>> = repository.scheduledVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedVideos: StateFlow<List<VideoProject>> = repository.publishedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishLogs: StateFlow<List<PublishLog>> = repository.allPublishLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var playbackJob: Job? = null

    init {
        ttsEngine = TtsEngine(application)
        // Check if there are existing videos; if none, seed initial demo project
        viewModelScope.launch {
            delay(300)
            if (allVideos.value.isEmpty()) {
                seedInitialProjects()
            }
        }
    }

    private suspend fun seedInitialProjects() {
        val initialVideo = geminiService.generateVideoForNiche(
            nicheTitle = "⚡ Anime Beast Motivation & Gym Arc",
            topic = "Anime Villain Training Arc",
            voiceStyle = "Deep Phonk Narrator (Grit & Bass)",
            bgmTrack = "Drift Phonk Beast"
        ).getOrNull()

        if (initialVideo != null) {
            val id = repository.saveVideo(initialVideo)
            val saved = repository.getVideoById(id)
            if (saved != null) {
                _activeVideo.value = saved
                _activeScenes.value = SceneJsonHelper.fromJson(saved.scenesJson)
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectNiche(niche: TrendingNiche) {
        _selectedNiche.value = niche
        _customTopicInput.value = niche.suggestedTopics.firstOrNull() ?: ""
        _currentTab.value = NavigationTab.STUDIO
    }

    fun setCustomTopic(topic: String) {
        _customTopicInput.value = topic
    }

    fun setVoiceStyle(voice: String) {
        _selectedVoiceStyle.value = voice
    }

    fun setBgmTrack(bgm: String) {
        _selectedBgmTrack.value = bgm
    }

    fun setDailyScheduleTime(time: String) {
        _dailyScheduleTime.value = time
    }

    fun togglePlatform(platform: String) {
        val current = _enabledPlatforms.value.toMutableSet()
        if (current.contains(platform)) {
            if (current.size > 1) current.remove(platform)
        } else {
            current.add(platform)
        }
        _enabledPlatforms.value = current
    }

    fun toggleAutoPilot(enabled: Boolean) {
        _isAutoPilotEnabled.value = enabled
    }

    fun generateVideo() {
        viewModelScope.launch {
            val niche = _selectedNiche.value.title
            val topic = if (_customTopicInput.value.isNotBlank()) _customTopicInput.value else _selectedNiche.value.suggestedTopics.first()
            val voice = _selectedVoiceStyle.value
            val bgm = _selectedBgmTrack.value

            _generationState.value = GenerationState.Generating("Analyzing Trending Hook & Virality...", 0.2f)
            delay(600)

            _generationState.value = GenerationState.Generating("Writing High-Retention Script & Scenes...", 0.45f)
            delay(700)

            _generationState.value = GenerationState.Generating("Generating Visual Art Storyboard...", 0.75f)

            val result = geminiService.generateVideoForNiche(
                nicheTitle = niche,
                topic = topic,
                voiceStyle = voice,
                bgmTrack = bgm
            )

            result.onSuccess { video ->
                _generationState.value = GenerationState.Generating("Synchronizing Audio, Captions & Timings...", 0.95f)
                delay(400)

                val savedId = repository.saveVideo(video)
                val persistedVideo = repository.getVideoById(savedId) ?: video.copy(id = savedId)

                _activeVideo.value = persistedVideo
                _activeScenes.value = SceneJsonHelper.fromJson(persistedVideo.scenesJson)
                _currentSceneIndex.value = 0
                _sceneProgress.value = 0f
                _generationState.value = GenerationState.Success(persistedVideo)
                _currentTab.value = NavigationTab.PLAYER
                startPlayback()
            }.onFailure { error ->
                _generationState.value = GenerationState.Error(error.localizedMessage ?: "Generation failed")
            }
        }
    }

    fun loadVideoForPlayback(video: VideoProject) {
        pausePlayback()
        _activeVideo.value = video
        val scenes = SceneJsonHelper.fromJson(video.scenesJson)
        _activeScenes.value = scenes
        _currentSceneIndex.value = 0
        _sceneProgress.value = 0f
        _currentTab.value = NavigationTab.PLAYER
        startPlayback()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun seekToScene(index: Int) {
        val scenes = _activeScenes.value
        if (index in scenes.indices) {
            _currentSceneIndex.value = index
            _sceneProgress.value = 0f
            if (_isPlaying.value) {
                speakCurrentScene()
            }
        }
    }

    fun nextScene() {
        val scenes = _activeScenes.value
        if (_currentSceneIndex.value < scenes.size - 1) {
            _currentSceneIndex.value += 1
            _sceneProgress.value = 0f
            if (_isPlaying.value) {
                speakCurrentScene()
            }
        } else {
            // Loop back to start
            _currentSceneIndex.value = 0
            _sceneProgress.value = 0f
            if (_isPlaying.value) {
                speakCurrentScene()
            }
        }
    }

    fun previousScene() {
        if (_currentSceneIndex.value > 0) {
            _currentSceneIndex.value -= 1
            _sceneProgress.value = 0f
            if (_isPlaying.value) {
                speakCurrentScene()
            }
        }
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        _isPlaying.value = true
        speakCurrentScene()

        playbackJob = viewModelScope.launch {
            while (_isPlaying.value) {
                val scenes = _activeScenes.value
                if (scenes.isEmpty()) break

                val currentIndex = _currentSceneIndex.value
                val currentScene = scenes.getOrNull(currentIndex) ?: break
                val durationMs = (currentScene.durationSec * 1000).toLong().coerceAtLeast(3000L)
                val stepMs = 50L
                val totalSteps = (durationMs / stepMs).toInt()

                for (step in 0..totalSteps) {
                    if (!_isPlaying.value) break
                    _sceneProgress.value = step.toFloat() / totalSteps.toFloat()
                    delay(stepMs)
                }

                if (_isPlaying.value) {
                    if (currentIndex < scenes.size - 1) {
                        _currentSceneIndex.value = currentIndex + 1
                        _sceneProgress.value = 0f
                        speakCurrentScene()
                    } else {
                        // Loop video
                        _currentSceneIndex.value = 0
                        _sceneProgress.value = 0f
                        speakCurrentScene()
                    }
                }
            }
        }
    }

    private fun speakCurrentScene() {
        if (!_isVoiceTtsEnabled.value) return
        val scenes = _activeScenes.value
        val currentScene = scenes.getOrNull(_currentSceneIndex.value) ?: return
        val voiceStyle = _activeVideo.value?.voiceStyle ?: "Energetic Storyteller"
        ttsEngine?.speak(currentScene.narrationText, voiceStyle)
    }

    fun toggleVoiceTts() {
        val next = !_isVoiceTtsEnabled.value
        _isVoiceTtsEnabled.value = next
        if (!next) {
            ttsEngine?.stop()
        } else if (_isPlaying.value) {
            speakCurrentScene()
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        ttsEngine?.stop()
    }

    fun publishActiveVideoNow() {
        val video = _activeVideo.value ?: return
        viewModelScope.launch {
            _isPublishingNow.value = true
            val success = autoPublisher.publishVideoNow(video, _enabledPlatforms.value.toList())
            _isPublishingNow.value = false
            if (success) {
                // Refresh local entity
                val updated = repository.getVideoById(video.id)
                if (updated != null) {
                    _activeVideo.value = updated
                }
            }
        }
    }

    fun scheduleActiveVideo(timeStr: String) {
        val video = _activeVideo.value ?: return
        viewModelScope.launch {
            val updated = video.copy(
                status = "SCHEDULED",
                scheduledDailyTime = timeStr,
                targetPlatforms = _enabledPlatforms.value.joinToString(",")
            )
            repository.updateVideo(updated)
            _activeVideo.value = updated
            val log = PublishLog(
                videoId = video.id,
                videoTitle = video.title,
                platform = "Daily Scheduler",
                publishedAt = System.currentTimeMillis(),
                status = "SCHEDULED",
                logMessage = "Scheduled for daily automated release at $timeStr to ${_enabledPlatforms.value.joinToString(", ")}"
            )
            repository.recordPublishLog(log)
        }
    }

    fun triggerAutoPilotBatch() {
        viewModelScope.launch {
            _isPublishingNow.value = true
            // Pick a random trending niche and create a fresh video
            val randomNiche = TrendingNichesRepository.curatedNiches.random()
            val topic = randomNiche.suggestedTopics.random()

            val generated = geminiService.generateVideoForNiche(
                nicheTitle = randomNiche.title,
                topic = topic,
                voiceStyle = "Deep Phonk Narrator (Grit & Bass)",
                bgmTrack = "Drift Phonk Beast"
            ).getOrNull()

            if (generated != null) {
                val savedId = repository.saveVideo(generated)
                val video = repository.getVideoById(savedId) ?: generated.copy(id = savedId)
                autoPublisher.publishVideoNow(video, _enabledPlatforms.value.toList())
                _activeVideo.value = video
                _activeScenes.value = SceneJsonHelper.fromJson(video.scenesJson)
            }
            _isPublishingNow.value = false
        }
    }

    fun shareVideo(video: VideoProject) {
        autoPublisher.shareVideoViaSystem(video)
    }

    fun deleteVideo(video: VideoProject) {
        viewModelScope.launch {
            if (_activeVideo.value?.id == video.id) {
                pausePlayback()
                _activeVideo.value = null
                _activeScenes.value = emptyList()
            }
            repository.deleteVideo(video)
        }
    }

    fun updateSceneNarration(sceneIndex: Int, newNarration: String) {
        val currentVideo = _activeVideo.value ?: return
        val scenes = _activeScenes.value.toMutableList()
        if (sceneIndex in scenes.indices) {
            val old = scenes[sceneIndex]
            scenes[sceneIndex] = old.copy(narrationText = newNarration)
            _activeScenes.value = scenes
            val updatedJson = SceneJsonHelper.toJson(scenes)
            val updatedFullScript = scenes.joinToString(" ") { it.narrationText }
            val updatedVideo = currentVideo.copy(scenesJson = updatedJson, fullScript = updatedFullScript)
            _activeVideo.value = updatedVideo
            viewModelScope.launch {
                repository.updateVideo(updatedVideo)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pausePlayback()
        ttsEngine?.shutdown()
    }
}
