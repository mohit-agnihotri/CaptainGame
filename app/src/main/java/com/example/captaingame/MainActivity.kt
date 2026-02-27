package com.example.captaingame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    var treasuresFound by remember { mutableStateOf(0) }
    var direction by remember { mutableStateOf("North") }
    var result by remember { mutableStateOf("") }
    var lives by remember { mutableStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }

    fun sail(selectedDirection: String) {

        if (gameOver) return

        direction = selectedDirection

        if (Random.nextBoolean()) {
            treasuresFound++
            result = "🎉 WE FOUND A TREASURE!"
        } else {
            lives--
            result = "⛈ STORM AHEAD!"
        }

        if (treasuresFound >= 5) {
            result = "🏆 YOU WIN THE GAME!"
            gameOver = true
        }

        if (lives <= 0) {
            result = "💀 GAME OVER!"
            gameOver = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Treasures Found: $treasuresFound")
        Text("Lives Left: $lives")
        Text("Current Direction: $direction")
        Text("Result: $result")

        Button(onClick = { sail("East") }, enabled = !gameOver) {
            Text("Sail East")
        }

        Button(onClick = { sail("West") }, enabled = !gameOver) {
            Text("Sail West")
        }

        Button(onClick = { sail("North") }, enabled = !gameOver) {
            Text("Sail North")
        }

        Button(onClick = { sail("South") }, enabled = !gameOver) {
            Text("Sail South")
        }

        if (gameOver) {
            Button(onClick = {
                treasuresFound = 0
                lives = 3
                direction = "North"
                result = ""
                gameOver = false
            }) {
                Text("Restart Game")
            }
        }
    }
}