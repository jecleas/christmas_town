package com.lexi.christmastown

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        Sprites.load(this)
        Audio.init(this)
        val prefs = getSharedPreferences("christmas-town", Context.MODE_PRIVATE)
        setContent {
            val game = remember {
                GameState(prefs).also {
                    Audio.soundOn = it.s.soundOn
                    Audio.musicOn = it.s.musicOn
                }
            }
            GameApp(game)
        }
    }

    // Go quiet when the app is in the background (home button, phone call...).
    override fun onPause() { super.onPause(); Audio.onPause() }
    override fun onResume() { super.onResume(); Audio.onResume() }
}
