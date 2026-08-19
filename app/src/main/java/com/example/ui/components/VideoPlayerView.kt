package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SceneItem
import com.example.data.model.VideoProject
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCobalt
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StealthBorder
import com.example.ui.theme.TitaniumGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VideoPlayerView(
    video: VideoProject?,
    scenes: List<SceneItem>,
    currentSceneIndex: Int,
    isPlaying: Boolean,
    sceneProgress: Float,
    isVoiceEnabled: Boolean,
    onTogglePlay: () -> Unit,
    onNextScene: () -> Unit,
    onPreviousScene: () -> Unit,
    onToggleVoice: () -> Unit,
    onPublishNow: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (video == null || scenes.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "No Video",
                        tint = CyberBlue,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚡ No Video Loaded",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select a trending niche in Discover to start!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val currentScene = scenes.getOrNull(currentSceneIndex) ?: scenes.first()

    // Animation drivers for live Cyber Canvas
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_player_fx")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .shadow(20.dp, RoundedCornerShape(24.dp))
            .border(2.dp, CyberBlue.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .testTag("video_player_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090B10))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dynamic Generative Cyber Background Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onTogglePlay() }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                // Dark cyber radial base
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF101B2E), Color(0xFF0A0F1A), Color(0xFF05070B)),
                        center = center,
                        radius = canvasWidth * 0.9f
                    )
                )

                // Concentric Tech Glow Circles
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberBlue.copy(alpha = 0.35f * pulseScale), Color.Transparent),
                        center = center,
                        radius = canvasWidth * 0.6f * pulseScale
                    ),
                    radius = canvasWidth * 0.6f * pulseScale,
                    center = center
                )

                // Floating HUD Particle Elements
                for (i in 0 until 32) {
                    val angle = (i * 45.0f + rotationAngle * 0.5f) * (Math.PI / 180.0)
                    val dist = (canvasWidth * 0.44f) * ((i % 4 + 1) / 4f)
                    val x = center.x + (dist * cos(angle)).toFloat()
                    val y = center.y + (dist * sin(angle)).toFloat()
                    drawCircle(
                        color = if (i % 2 == 0) CyberBlue.copy(alpha = 0.8f) else CyberCrimson.copy(alpha = 0.8f),
                        radius = (i % 3 + 2.5f).dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // Top Segmented Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scenes.forEachIndexed { index, _ ->
                        val progress = when {
                            index < currentSceneIndex -> 1f
                            index == currentSceneIndex -> sceneProgress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyberBlue,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Top Header Overlay (TikTok Tag, Scene #, Voice toggle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ TIKTOK VIRAL 9:16",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberBlue,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StealthBorder)
                        ) {
                            Text(
                                text = "Scene ${currentSceneIndex + 1}/${scenes.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleVoice,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.75f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isVoiceEnabled) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                                contentDescription = "Toggle Voice",
                                tint = if (isVoiceEnabled) CyberBlue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Center Dynamic Kinetic Subtitle Banner
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentSceneIndex == 0) {
                    Surface(
                        color = CyberCrimson,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "🔥 3-SECOND VIRAL HOOK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = currentScene.narrationText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }

                if (currentScene.keywordsToHighlight.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currentScene.keywordsToHighlight.take(3).forEach { kw ->
                            Surface(
                                color = TitaniumGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TitaniumGold)
                            ) {
                                Text(
                                    text = "⚡ ${kw.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TitaniumGold,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Player Controls and Action Deck
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f), Color.Black)
                        )
                    )
                    .padding(16.dp)
            ) {
                // Audio & Motion spec indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = CyberBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${video.bgmTrackName} • ${video.voiceStyle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    Surface(
                        color = CyberBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${video.durationSeconds}s TikTok 9:16",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playback Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousScene,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Previous Scene",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(CyberBlue, CyberCobalt)),
                                CircleShape
                            )
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = onNextScene,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Next Scene",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Quick Actions: "Post to TikTok" & "Share Video"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPublishNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("publish_now_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberBlue,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post to TikTok", fontWeight = FontWeight.Black, color = Color.Black)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_video_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share & Export", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
