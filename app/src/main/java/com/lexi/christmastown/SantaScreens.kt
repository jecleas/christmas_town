package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.sin

// MARK: - Santa's office

@Composable
fun SantaScreen(game: GameState) {
    var feedback by remember { mutableStateOf<String?>(null) }
    Row(horizontalArrangement = Arrangement.spacedBy(30.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Bubble(
                when {
                    game.s.paidToday -> "Ho ho ho! All done for today. Here's your ${money(PAY)}! Go get some rest."
                    game.level < 2 -> "Ho ho ho, ${game.s.name}! Finish your jobs and I'll pay you ${money(PAY)}."
                    else -> "Ho ho ho! Help me check my list, and deliver presents when you can!"
                }, 260.dp,
            )
            Bob(3f, 900) { Px("santa", 210.dp) }
        }
        Column(Modifier.width(560.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // The naughty / nice list
            Column(Modifier.fillMaxWidth().panel(hex(0xFFF6DD)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Txt("📜 Santa's List", 24)
                val job = game.s.jobs.firstOrNull { it.kind == JobKind.SortList }
                val kid = game.currentKid
                when {
                    job == null -> Txt("Santa's list unlocks at Level 2! 🔒", 18)
                    kid == null || job.finished -> Txt("The list is checked twice! ✅", 18, Pal.darkGreen)
                    else -> {
                        Txt("${job.done + 1} of ${job.goal}", 15, Pal.brown)
                        Txt(kid.name, 30, Pal.red)
                        Txt("“${kid.deed}”", 20, align = TextAlign.Center)
                        Txt("Naughty or nice?", 17)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Btn("😈 Naughty", Pal.ink, 20) {
                                val right = game.sortKid(false)
                                feedback = if (right) "Correct! ${kid.name} gets coal 🪨" else "Hmm, ${kid.name} was actually nice! 😇"
                            }
                            Btn("😇 Nice", Pal.green, 20) {
                                val right = game.sortKid(true)
                                feedback = if (right) "Correct! ${kid.name} gets a present 🎁" else "Hmm, ${kid.name} was actually naughty! 😈"
                            }
                        }
                    }
                }
                feedback?.let { Txt(it, 16, Pal.darkGreen) }
            }
            // Deliveries
            Column(Modifier.fillMaxWidth().panel(hex(0xDDE7FF)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Px("sleigh", 50.dp); Txt("Present Delivery", 22); Px("gift", 36.dp)
                }
                val job = game.s.jobs.firstOrNull { it.kind == JobKind.Deliver }
                when {
                    job == null -> Txt("Deliveries unlock at Level 3! 🔒", 17)
                    job.finished -> Txt("All of today's presents are delivered! ✅", 17, Pal.darkGreen)
                    else -> {
                        Txt("Deliver ${job.goal - job.done} more. You have ${game.presents} toys to give. 🎁", 17, align = TextAlign.Center)
                        Btn("🛷 Fly the sleigh! (⚡10)", Pal.red, 20) { game.startDelivery() }
                    }
                }
            }
        }
    }
}

// MARK: - Sleigh ride

private const val HOUSE_GAP = 230f
private const val SPEED = 110f   // stage units per second

@Composable
fun DeliveryScreen(game: GameState) {
    val christmas = game.christmasEveRide
    val job = game.s.jobs.firstOrNull { it.kind == JobKind.Deliver }
    val target = if (christmas) 8 else (job?.let { it.goal - it.done } ?: 0)
    var elapsed by remember { mutableFloatStateOf(0f) }
    var delivered by remember { mutableIntStateOf(0) }
    val visited = remember { mutableStateOf(setOf<Int>()) }
    var message by remember { mutableStateOf("Tap the 😇 nice houses to drop presents!") }
    val finished = delivered >= target

    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) { withFrameMillis { elapsed = (it - start) / 1000f } }
    }
    LaunchedEffect(finished) {
        if (finished) { message = "All presents delivered! 🎉"; delay(1800); game.endDelivery(true) }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(hex(0x0E1633), hex(0x2B3A75))))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(hex(0xFFF3B0), 46 * density, Offset(size.width * 0.86f, 90 * density))
            for (i in 0 until 40) drawRect(Color.White, Offset(((i * 131) % 1000) / 1000f * size.width, ((i * 71) % 400) / 1000f * size.height), Size(3 * density, 3 * density))
        }
        Snow(game.now)

        // Sleigh and reindeer flying along
        Box(Modifier.offset(x = 240.dp, y = (90 + 10 * sin(elapsed * 2)).dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Px("sleigh", 70.dp)
                Box(Modifier.offset(x = (-10).dp, y = (-26).dp)) { Px("santa", 70.dp) }
                val r = Sprites.bitmap("reindeer|fly", Sprites.rows("reindeer"), mapOf('N' to Pal.red))
                Box(Modifier.offset(x = 20.dp, y = (-20).dp)) { Px(r, 60.dp) }
            }
        }

        // Rooftops scrolling past
        val scroll = elapsed * SPEED
        val first = floor(scroll / HOUSE_GAP).toInt()
        for (i in first..first + 6) {
            val x = 1120f + i * HOUSE_GAP - scroll - HOUSE_GAP * 4
            val nice = ((i * 7 + 3) % 5) != 0
            val done = i in visited.value
            Column(
                Modifier.offset(x = x.dp, y = 460.dp).width(170.dp).squish(enabled = !done && !finished) {
                    if (nice) {
                        if (game.deliver()) {
                            visited.value = visited.value + i
                            delivered++
                            message = listOf("Ho ho ho! 🎁", "Present delivered! ✨", "A happy kid tomorrow! 😊").random()
                        }
                    } else {
                        visited.value = visited.value + i
                        message = "That's a naughty house! Coal for them 🪨"
                    }
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Txt(if (done) (if (nice) "🎁" else "🪨") else if (nice) "😇" else "😈", 30)
                RoofHouse(nice, done, i)
            }
        }

        Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Txt(if (christmas) "🌟 Christmas Eve Delivery 🌟" else "🛷 Present Delivery", 28, Color.White)
            Txt("Delivered $delivered / $target", 22, hex(0xFFCC4D))
            Txt(message, 18, Color.White)
        }
        if (!christmas) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomEnd) {
                Btn("🏠 Fly home", Pal.blue, 18) { game.endDelivery(false) }
            }
        }
    }
}

@Composable
private fun RoofHouse(nice: Boolean, done: Boolean, seed: Int) {
    val wall = listOf(hex(0xE8C7A0), hex(0xC9E0F2), hex(0xF2C9D6), hex(0xD8EBC6))[((seed % 4) + 4) % 4]
    Canvas(Modifier.size(170.dp, 190.dp)) {
        val u = density
        fun r(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, Offset(x * u, y * u), Size(w * u, h * u))
        r(hex(0x6B4A3A), 110f, 10f, 26f, 40f)   // chimney
        r(Color.White, 106f, 6f, 34f, 8f)
        drawPath(androidx.compose.ui.graphics.Path().apply { moveTo(0f, 70 * u); lineTo(85 * u, 18 * u); lineTo(170 * u, 70 * u); close() }, if (nice) hex(0xA02338) else hex(0x4A4458))
        r(Color.White, 10f, 62f, 150f, 8f)
        r(Pal.ink, 14f, 70f, 142f, 120f)
        r(wall, 18f, 74f, 134f, 116f)
        val lit = if (done && nice) hex(0xFFE27A) else if (nice) hex(0xFFCC4D) else hex(0x3A3550)
        r(Pal.ink, 34f, 96f, 36f, 36f); r(lit, 37f, 99f, 30f, 30f)
        r(Pal.ink, 100f, 96f, 36f, 36f); r(lit, 103f, 99f, 30f, 30f)
        r(hex(0x6B4A3A), 70f, 140f, 30f, 50f)
    }
}
