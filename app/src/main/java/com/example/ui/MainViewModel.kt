package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.LanguageOption
import com.example.data.model.PublishLog
import com.example.data.model.SceneItem
import com.example.data.model.SceneJsonHelper
import com.example.data.model.SupportedLanguages
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

enum class NavigationTab {
    DISCOVER,
    STUDIO,
    PLAYER,
    AUTO_POST,
    LIBRARY
}

sealed class GenerationState {
    object Idle : GenerationState()
    data class Generating(val stepMessage: String, val progress: Float) : GenerationState()
    data class Success(val videoProject: VideoProject) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository
    private val geminiService = GeminiService()
    val autoPublisher: AutoPublishScheduler
    private var ttsEngine: TtsEngine? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VideoRepository(db.videoProjectDao(), db.publishLogDao())
        autoPublisher = AutoPublishScheduler(application, repository)
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(NavigationTab.DISCOVER)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Language Selection State
    private val _selectedLanguage = MutableStateFlow(SupportedLanguages.defaultLanguage)
    val selectedLanguage: StateFlow<LanguageOption> = _selectedLanguage.asStateFlow()

    // Studio & Discovery Selection States
    private val _selectedNiche = MutableStateFlow(TrendingNichesRepository.curatedNiches.first())
    val selectedNiche: StateFlow<TrendingNiche> = _selectedNiche.asStateFlow()

    private val _customTopicInput = MutableStateFlow("")
    val customTopicInput: StateFlow<String> = _customTopicInput.asStateFlow()

    private val _selectedVoiceStyle = MutableStateFlow("Narrador Grave Phonk BR")
    val selectedVoiceStyle: StateFlow<String> = _selectedVoiceStyle.asStateFlow()

    private val _selectedBgmTrack = MutableStateFlow("Drift Phonk Beast")
    val selectedBgmTrack: StateFlow<String> = _selectedBgmTrack.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Video Playback Engine State
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
        viewModelScope.launch {
            delay(300)
            if (allVideos.value.isEmpty()) {
                seedInitialProjects()
            }
        }
    }

    private suspend fun seedInitialProjects() {
        val lang = _selectedLanguage.value
        val initialVideo = geminiService.generateVideoForNiche(
            nicheTitle = "⚡ Anime Beast Motivation & Gym Arc",
            topic = "Anime Villain Training Arc",
            voiceStyle = "Narrador Grave Phonk BR",
            bgmTrack = "Drift Phonk Beast",
            languageCode = lang.code,
            languageName = lang.name
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

    fun setLanguage(language: LanguageOption) {
        _selectedLanguage.value = language
        val firstVoice = language.voices.firstOrNull()
        if (firstVoice != null) {
            _selectedVoiceStyle.value = firstVoice.name
        }
    }

    fun selectNiche(niche: TrendingNiche) {
        _selectedNiche.value = niche
        _customTopicInput.value = niche.suggestedTopics.firstOrNull() ?: ""
    }

    fun setCustomTopic(topic: String) {
        _customTopicInput.value = topic
    }

    fun setVoiceStyle(voice: String) {
        _selectedVoiceStyle.value = voice
    }

    fun setBgmTrack(track: String) {
        _selectedBgmTrack.value = track
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
            val lang = _selectedLanguage.value

            _generationState.value = GenerationState.Generating("Analisando Gancho Viral em ${lang.name}...", 0.2f)
            delay(600)

            _generationState.value = GenerationState.Generating("Escrevendo Roteiro & Cenas (${lang.code})...", 0.45f)
            delay(700)

            _generationState.value = GenerationState.Generating("Gerando Storyboard Visual 9:16...", 0.75f)

            val result = geminiService.generateVideoForNiche(
                nicheTitle = niche,
                topic = topic,
                voiceStyle = voice,
                bgmTrack = bgm,
                languageCode = lang.code,
                languageName = lang.name
            )

            result.onSuccess { video ->
                _generationState.value = GenerationState.Generating("Sincronizando Áudio, Legendas & Timings...", 0.95f)
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

    private fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        ttsEngine?.stop()
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
        val video = _activeVideo.value
        val voiceStyle = video?.voiceStyle ?: _selectedVoiceStyle.value
        val langCode = video?.languageCode ?: _selectedLanguage.value.code
        ttsEngine?.speak(currentScene.narrationText, voiceStyle, langCode)
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

    fun updateSceneNarration(sceneIndex: Int, newNarration: String) {
        val current = _activeScenes.value.toMutableList()
        if (sceneIndex in current.indices) {
            val updatedScene = current[sceneIndex].copy(narrationText = newNarration)
            current[sceneIndex] = updatedScene
            _activeScenes.value = current

            val activeVid = _activeVideo.value
            if (activeVid != null) {
                val updatedVid = activeVid.copy(
                    scenesJson = SceneJsonHelper.toJson(current),
                    fullScript = current.joinToString(" ") { it.narrationText }
                )
                _activeVideo.value = updatedVid
                viewModelScope.launch {
                    repository.updateVideo(updatedVid)
                }
            }
        }
    }

    fun publishActiveVideoNow() {
        val video = _activeVideo.value ?: return
        viewModelScope.launch {
            _isPublishingNow.value = true
            val platforms = _enabledPlatforms.value.toList()
            autoPublisher.publishVideoNow(video, platforms)
            val updated = repository.getVideoById(video.id)
            if (updated != null) {
                _activeVideo.value = updated
            }
            _isPublishingNow.value = false
        }
    }

    fun scheduleActiveVideo(timeStr: String = _dailyScheduleTime.value) {
        val video = _activeVideo.value ?: return
        viewModelScope.launch {
            val scheduled = video.copy(
                status = "SCHEDULED",
                scheduledDailyTime = timeStr,
                targetPlatforms = _enabledPlatforms.value.joinToString(",")
            )
            repository.updateVideo(scheduled)
            _activeVideo.value = scheduled

            val log = PublishLog(
                videoId = video.id,
                videoTitle = video.title,
                platform = "Auto-Schedule Queue",
                publishedAt = System.currentTimeMillis(),
                status = "SCHEDULED",
                logMessage = "Scheduled for daily automated release at $timeStr to ${_enabledPlatforms.value.joinToString(", ")}",
                postUrl = "https://tiktok.com/@oskaiviral"
            )
            repository.recordPublishLog(log)
        }
    }

    fun triggerAutoPilotBatch() {
        viewModelScope.launch {
            _isPublishingNow.value = true
            val randomNiche = TrendingNichesRepository.curatedNiches.random()
            val topic = randomNiche.suggestedTopics.random()
            val lang = _selectedLanguage.value
            val voice = lang.voices.firstOrNull()?.name ?: "Narrador Grave Phonk BR"

            val generated = geminiService.generateVideoForNiche(
                nicheTitle = randomNiche.title,
                topic = topic,
                voiceStyle = voice,
                bgmTrack = "Drift Phonk Beast",
                languageCode = lang.code,
                languageName = lang.name
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
            repository.deleteVideo(video)
            if (_activeVideo.value?.id == video.id) {
                pausePlayback()
                val remaining = allVideos.value.filter { it.id != video.id }
                val next = remaining.firstOrNull()
                _activeVideo.value = next
                _activeScenes.value = next?.let { SceneJsonHelper.fromJson(it.scenesJson) } ?: emptyList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pausePlayback()
        ttsEngine?.shutdown()
    }
}
