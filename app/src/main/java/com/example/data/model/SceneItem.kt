package com.example.data.model

data class SceneItem(
    val sceneNumber: Int,
    val narrationText: String,
    val visualPrompt: String,
    val durationSec: Float = 5.0f,
    val visualTheme: String = "cyber_grid", // cyber_grid, space_nebula, golden_statue, matrix_rain, abstract_fluid, glitch_motion
    val cameraMotion: String = "Zoom In", // Zoom In, Pan Right, Dolly Out, Pulse
    val keywordsToHighlight: List<String> = emptyList()
)
