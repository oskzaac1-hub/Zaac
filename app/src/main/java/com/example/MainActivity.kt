package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.screens.AutoPostScreen
import com.example.ui.screens.CreateStudioScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCobalt
import com.example.ui.theme.CyberCrimson
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StealthBorder
import com.example.ui.theme.TitaniumGold

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAutoPilot by viewModel.isAutoPilotEnabled.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    Brush.linearGradient(listOf(CyberBlue, CyberCrimson)),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OSK Ai",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = CyberBlue.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberBlue,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = if (isAutoPilot) MatrixGreen.copy(alpha = 0.15f) else Color.DarkGray,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAutoPilot) MatrixGreen else Color.Transparent
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isAutoPilot) MatrixGreen else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAutoPilot) "AUTOPILOT ON" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isAutoPilot) MatrixGreen else Color.LightGray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DISCOVER,
                    onClick = { viewModel.setTab(NavigationTab.DISCOVER) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                    label = { Text("Discover", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlue,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_discover")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.STUDIO,
                    onClick = { viewModel.setTab(NavigationTab.STUDIO) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Studio") },
                    label = { Text("Studio", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberBlue,
                        selectedTextColor = CyberBlue,
                        indicatorColor = CyberBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_studio")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.PLAYER,
                    onClick = { viewModel.setTab(NavigationTab.PLAYER) },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Player") },
                    label = { Text("Playback", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCrimson,
                        selectedTextColor = CyberCrimson,
                        indicatorColor = CyberCrimson.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_player")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.AUTO_POST,
                    onClick = { viewModel.setTab(NavigationTab.AUTO_POST) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Auto-Post") },
                    label = { Text("Auto-Post", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixGreen,
                        selectedTextColor = MatrixGreen,
                        indicatorColor = MatrixGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_autopost")
                )

                NavigationBarItem(
                    selected = currentTab == NavigationTab.LIBRARY,
                    onClick = { viewModel.setTab(NavigationTab.LIBRARY) },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                    label = { Text("Library", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TitaniumGold,
                        selectedTextColor = TitaniumGold,
                        indicatorColor = TitaniumGold.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_library")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                NavigationTab.DISCOVER -> DiscoverScreen(viewModel = viewModel)
                NavigationTab.STUDIO -> CreateStudioScreen(viewModel = viewModel)
                NavigationTab.PLAYER -> VideoPlayerScreen(viewModel = viewModel)
                NavigationTab.AUTO_POST -> AutoPostScreen(viewModel = viewModel)
                NavigationTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
            }
        }
    }
}
