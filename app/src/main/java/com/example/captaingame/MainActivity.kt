package com.example.captaingame

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.captaingame.ui.theme.CaptainGameTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaptainGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    CaptainGame()
                }
            }
        }
    }
}

@Composable
fun CaptainGame() {

    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.ocean}")

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    val wavePlayer = remember {
        MediaPlayer.create(context, R.raw.waves).apply {
            isLooping = true
            setVolume(0.4f, 0.4f)
        }
    }

    val treasurePlayer = remember { MediaPlayer.create(context, R.raw.treasure) }
    val stormPlayer = remember { MediaPlayer.create(context, R.raw.storm) }
    val gameOverPlayer = remember { MediaPlayer.create(context, R.raw.gameover) }
    val winnerPlayer = remember { MediaPlayer.create(context, R.raw.winner) }

    fun stopAllEffects() {
        listOf(treasurePlayer, stormPlayer, gameOverPlayer, winnerPlayer).forEach {
            if (it.isPlaying) {
                it.pause()
                it.seekTo(0)
            }
        }
    }

    fun stopBackground() {
        if (wavePlayer.isPlaying) wavePlayer.pause()
        videoViewRef?.pause()
    }

    fun startBackground() {
        if (!wavePlayer.isPlaying) {
            wavePlayer.seekTo(0)
            wavePlayer.start()
        }
        videoViewRef?.start()
    }

    fun playEffect(player: MediaPlayer, stopBg: Boolean = false) {
        stopAllEffects()
        if (stopBg) stopBackground()
        player.seekTo(0)
        player.start()
    }

    LaunchedEffect(Unit) {
        wavePlayer.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            wavePlayer.release()
            treasurePlayer.release()
            stormPlayer.release()
            gameOverPlayer.release()
            winnerPlayer.release()
        }
    }

    var treasuresFound by remember { mutableStateOf(0) }
    var direction by remember { mutableStateOf("North") }
    var result by remember { mutableStateOf("") }
    var gameOver by remember { mutableStateOf(false) }
    var lives by remember { mutableStateOf(3) }

    fun sail(selectedDirection: String) {

        if (gameOver) return

        direction = selectedDirection

        if (Random.nextBoolean()) {
            treasuresFound++
            result = "🎉 WE FOUND A TREASURE!"
            playEffect(treasurePlayer)
        } else {
            lives--
            result = "⛈ STORM AHEAD!"
            playEffect(stormPlayer)
        }

        if (treasuresFound >= 5) {
            result = "🏆 YOU WIN THE GAME!"
            gameOver = true
            playEffect(winnerPlayer, stopBg = true)
        }

        if (lives <= 0) {
            result = "💀 GAME OVER!"
            gameOver = true
            playEffect(gameOverPlayer, stopBg = true)
        }
    }

    val resultColor = when {
        result.contains("TREASURE") -> Color.Yellow
        result.contains("STORM") -> Color.Red
        result.contains("WIN") -> Color.Green
        result.contains("GAME OVER") -> Color.Red
        else -> Color.White
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🎬 Background Video
        AndroidView(
            factory = {
                VideoView(it).apply {
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                    }
                    videoViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🌑 Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // 🧊 Glass style panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🏴‍☠️ Captain Treasure Hunt",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Treasures Found: $treasuresFound", color = Color.White)
                Text("Lives Left: $lives", color = Color.White)
                Text("Direction: $direction", color = Color.White)

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = result.isNotEmpty(),
                    enter = fadeIn() + scaleIn()
                ) {
                    Text(
                        text = result,
                        color = resultColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                listOf("East", "West", "North", "South").forEach {
                    Button(
                        onClick = { sail(it) },
                        enabled = !gameOver,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text("Sail $it")
                    }
                }

                if (gameOver) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            treasuresFound = 0
                            lives = 3
                            direction = "North"
                            result = ""
                            gameOver = false

                            stopAllEffects()
                            startBackground()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Restart Game")
                    }
                }
            }
        }
    }
}