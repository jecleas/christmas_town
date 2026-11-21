package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ======================================================================
// MARK: - Ice skating with Snowflake: tap Twirl when the star is in the green
// ======================================================================

private const val SKATE_ROUNDS = 10

@Composable
fun SkateGame(game: GameState, setPlaying: (Boolean) -> Unit) {
    val coach = npcNamed("Snowflake")
    var running by remember { mutableStateOf(false) }
    var over by remember { mutableStateOf(false) }
    var t by remember { mutableFloatStateOf(0f) }
    var round by remember { mutableIntStateOf(0) }
    var twirls by remember { mutableIntStateOf(0) }
    var twirlAt by remember { mutableFloatStateOf(-9f) }
    var wobbleAt by remember { mutableFloatStateOf(-9f) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (running) withFrameMillis { t = (it - start) / 1000f }
    }
    val speed = 1.6f + round * 0.18f
    val marker = (sin(t * speed) + 1f) / 2f             // 0..1 along the bar
    val inZone = marker in 0.4f..0.6f

    fun twirl() {
        if (!running) return
        if (inZone) { twirls++; twirlAt = t; Audio.play(Sfx.Ding) } else { wobbleAt = t; Audio.play(Sfx.Oops) }
        round++
        if (round >= SKATE_ROUNDS) { running = false; over = true; setPlaying(false); game.skateDone(twirls) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tag("⛸️ Twirls: $twirls", Pal.blue, 16)
            Tag("Try ${minOf(round + 1, SKATE_ROUNDS)}/$SKATE_ROUNDS", Pal.purple, 16)
        }
        Box(Modifier.size(406.dp, 330.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(Brush.verticalGradient(listOf(hex(0x9BC8EE), hex(0xEAF6FF))))
                for (i in 0 until 7) drawCircle(Color.White, (40 + i * 6) * u, Offset((i * 70f) * u, 110 * u))
                drawOval(Pal.ink, Offset(20 * u, 120 * u), Size(366 * u, 190 * u))
                drawOval(hex(0xCDEBFA), Offset(24 * u, 124 * u), Size(358 * u, 182 * u))
                for (i in 0 until 6) drawLine(Color.White.copy(alpha = 0.7f), Offset((80 + i * 45) * u, (170 + (i % 3) * 30) * u), Offset((120 + i * 45) * u, (160 + (i % 3) * 30) * u), 3 * u)
            }
            // you and Snowflake skate round the pond
            for ((k, look) in listOf(game.s.look, coach.look).withIndex()) {
                val a = t * 1.1f + k * Math.PI.toFloat()
                val x = 203f + cos(a) * 120f
                val y = 215f + sin(a) * 45f
                val spin = k == 0 && t - twirlAt < 0.6f
                val wobble = k == 0 && t - wobbleAt < 0.6f
                ElfPx(look, 90.dp, Modifier.offset((x - 37).dp, (y - 80).dp).graphicsLayer {
                    if (spin) scaleX = cos((t - twirlAt) / 0.6f * 4f * Math.PI.toFloat())
                    if (wobble) rotationZ = sin((t - wobbleAt) * 30f) * 15f
                    if (k == 1) scaleX = if (cos(a) > 0) -1f else 1f
                })
            }
            if (t - twirlAt < 0.8f) Txt("✨ TWIRL! ✨", 22, Pal.blue, modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp))
            if (t - wobbleAt < 0.8f) Txt("Whoa, wobbly! 😅", 20, Pal.red, modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp))
            if (!running) Column(Modifier.align(Alignment.Center).width(320.dp).panel().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom) { ElfPx(coach.look, 60.dp); Bubble(if (over) "You did $twirls twirls! " + (if (twirls >= 6) "AMAZING! ⛸️🏆" else "Great skating! ❄️") else "Let's skate! Tap TWIRL when the ⭐ is in the green!", 220.dp) }
                Btn(if (over) "Skate again! (⚡2)" else "Let's skate! (⚡2)", Pal.blue, 18, Modifier.width(220.dp)) {
                    if (game.spend(2)) { round = 0; twirls = 0; t = 0f; twirlAt = -9f; wobbleAt = -9f; over = false; running = true; setPlaying(true) }
                }
            }
        }
        // the timing bar
        Box(Modifier.size(320.dp, 30.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(3.dp, Pal.ink, RoundedCornerShape(8.dp))) {
            Box(Modifier.offset(128.dp).width(64.dp).fillMaxHeight().background(Pal.green.copy(alpha = 0.6f)))
            Txt("⭐", 20, modifier = Modifier.offset((marker * 290).dp, 0.dp))
        }
        Btn("⛸️ TWIRL!", if (inZone && running) Pal.green else Pal.blue, 22, Modifier.width(240.dp), enabled = running) { twirl() }
    }
}

// ======================================================================
// MARK: - Sled race against Pip: tap PUSH as fast as you can!
// ======================================================================

@Composable
fun SledGame(game: GameState, setPlaying: (Boolean) -> Unit) {
    val pip = npcNamed("Pip")
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Boolean?>(null) }   // true = you won
    var you by remember { mutableFloatStateOf(0f) }
    var them by remember { mutableFloatStateOf(0f) }
    var boost by remember { mutableFloatStateOf(0f) }
    var pipSpeed by remember { mutableFloatStateOf(0.14f) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (running) withFrameMillis { now ->
            val dt = (now - last) / 1000f; last = now
            you += (0.06f + boost) * dt
            boost -= boost * 1.5f * dt   // your push slowly wears off
            them += pipSpeed * dt
            if (you >= 1f || them >= 1f) {
                running = false; setPlaying(false)
                result = you >= them
                game.sledDone(result == true)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(406.dp, 360.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(Brush.verticalGradient(listOf(hex(0x2C4F8C), hex(0x9BC8EE))))
                // the big snowy hill, top left to bottom right, with two lanes
                val hill = Path().apply { moveTo(0f, 60 * u); lineTo(size.width, 250 * u); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                drawPath(hill, Color.White)
                for (lane in 0..1) drawLine(hex(0xC6DDF0), Offset(0f, (95 + lane * 70) * u), Offset(size.width, (285 + lane * 70) * u), 4 * u)
                drawRect(Pal.red, Offset(380 * u, 230 * u), Size(6 * u, 140 * u))   // finish flag
                drawRect(Pal.red, Offset(386 * u, 230 * u), Size(18 * u, 12 * u))
            }
            for ((lane, p) in listOf(you, them).withIndex()) {
                val x = 10f + p * 330f
                val y = 60f + lane * 70f + p * 190f
                Box(Modifier.offset(x.dp, y.dp)) {
                    Px("sled", 26.dp, Modifier.offset(0.dp, 46.dp))
                    ElfPx(if (lane == 0) game.s.look else pip.look, 60.dp, Modifier.offset(6.dp, 0.dp))
                }
                Txt(if (lane == 0) "You" else "Pip", 12, Color.White, modifier = Modifier.offset((x + 8).dp, (y - 16).dp).background(Pal.ink.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
            }
            if (!running) Column(Modifier.align(Alignment.Center).width(320.dp).panel().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    ElfPx(pip.look, 60.dp)
                    Bubble(when (result) { true -> "No WAY! You beat me! 🏆 Rematch?!"; false -> "I WON! Wheee! 🛷 Wanna race again?"; null -> "Race you to the bottom! Tap PUSH really fast! 🛷💨" }, 220.dp)
                }
                Btn(if (result != null) "Race again! (⚡2)" else "Ready, set, GO! (⚡2)", Pal.red, 18, Modifier.width(240.dp)) {
                    if (game.spend(2)) { you = 0f; them = 0f; boost = 0f; pipSpeed = 0.11f + Random.nextFloat() * 0.04f; result = null; running = true; setPlaying(true) }
                }
            }
        }
        Txt(if (running) "Tap PUSH as fast as you can! 💨" else "Beat Pip to the red flag!", 16, Color.White, align = TextAlign.Center)
        Btn("🛷 PUSH!", Pal.red, 24, Modifier.width(260.dp), enabled = running) { boost = (boost + 0.03f).coerceAtMost(0.4f); Audio.play(Sfx.Tap) }
    }
}
