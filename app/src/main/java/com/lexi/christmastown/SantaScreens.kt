package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.sin

// MARK: - Santa's naughty / nice list

@Composable
fun SantaListScreen(game: GameState) {
    var feedback by remember { mutableStateOf<String?>(null) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 900) { Px("santa", 150.dp) }
            Bubble(
                when {
                    game.level < 2 -> "Ho ho ho! You can help with my list at Level 2. Keep working hard!"
                    else -> "Ho ho ho! Read what each kid did. Naughty or nice?"
                }, 210.dp,
            )
        }
        JobChip(game, JobKind.SortList)
        Column(Modifier.fillMaxWidth().panel(hex(0xFFF6DD)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Txt("📜 Santa's List", 24)
            val job = game.s.jobs.firstOrNull { it.kind == JobKind.SortList }
            val kid = game.currentKid
            when {
                job == null -> Txt("Unlocks at Level 2! 🔒", 18)
                kid == null || job.finished -> Txt("Checked it twice! ✅", 20, Pal.darkGreen)
                else -> {
                    Txt(kid.name, 32, Pal.red)
                    Txt("“${kid.deed}”", 20, align = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Btn("😈 Naughty", Pal.ink, 19, Modifier.weight(1f)) {
                            feedback = if (game.sortKid(false)) "Correct! ${kid.name} gets coal 🪨" else "Hmm, ${kid.name} was actually nice! 😇"
                        }
                        Btn("😇 Nice", Pal.green, 19, Modifier.weight(1f)) {
                            feedback = if (game.sortKid(true)) "Correct! ${kid.name} gets a present 🎁" else "Hmm, ${kid.name} was actually naughty! 😈"
                        }
                    }
                }
            }
            feedback?.let { Txt(it, 16, Pal.darkGreen, align = TextAlign.Center) }
        }
    }
}

// MARK: - Sleigh (before take-off)

@Composable
fun SleighScreen(game: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(300.dp, 170.dp), contentAlignment = Alignment.BottomCenter) {
            Bob(5f, 900) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Px("sleigh", 80.dp)
                    Box(Modifier.offset(x = (-12).dp, y = (-30).dp)) { Px("santa", 80.dp) }
                }
            }
        }
        JobChip(game, JobKind.Deliver)
        Column(Modifier.fillMaxWidth().panel(hex(0xDDE7FF)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val job = game.s.jobs.firstOrNull { it.kind == JobKind.Deliver }
            when {
                job == null -> Txt("The sleigh unlocks at Level 3! 🔒", 19, align = TextAlign.Center)
                job.finished -> Txt("All of today's presents are delivered! ✅", 19, Pal.darkGreen, align = TextAlign.Center)
                else -> {
                    Txt("Fly over town and tap the 😇 nice houses to drop presents!", 17, align = TextAlign.Center)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Px("gift", 30.dp); Txt("You have ${game.presents} toys to give", 17)
                    }
                    if (game.presents == 0) Txt("Make toys in the Workshop first! 🧸", 15, Pal.red)
                    Btn("🛷 Fly! (⚡10)", Pal.red, 22, Modifier.width(220.dp)) { game.startDelivery() }
                }
            }
        }
    }
}

// MARK: - Sleigh ride

private const val HOUSE_GAP = 175f
private const val SPEED = 80f   // stage units per second

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

    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(hex(0x0E1633), hex(0x2B3A75))))) {
        val h = maxHeight.value
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(hex(0xFFF3B0), 36 * density, Offset(size.width * 0.82f, 150 * density))
            for (i in 0 until 40) drawRect(Color.White, Offset(((i * 131) % 1000) / 1000f * size.width, ((i * 71) % 500) / 1000f * size.height), Size(3 * density, 3 * density))
        }
        Snow(game.now)

        Column(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt(if (christmas) "🌟 Christmas Eve 🌟" else "🛷 Present Delivery", 26, Color.White)
            Txt("Delivered $delivered / $target", 22, hex(0xFFCC4D))
            Txt(message, 17, Color.White, align = TextAlign.Center)
        }

        // Sleigh flying along
        Box(Modifier.offset(x = 60.dp, y = (h * 0.28f + 12 * sin(elapsed * 2)).dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Px("sleigh", 60.dp)
                Box(Modifier.offset(x = (-8).dp, y = (-22).dp)) { Px("santa", 60.dp) }
                val r = Sprites.bitmap("reindeer|fly", Sprites.rows("reindeer"), mapOf('N' to Pal.red))
                Box(Modifier.offset(x = 14.dp, y = (-16).dp)) { Px(r, 52.dp) }
            }
        }

        // Rooftops scrolling past
        val scroll = elapsed * SPEED
        val first = floor(scroll / HOUSE_GAP).toInt()
        for (i in first..first + 4) {
            val x = 450f + i * HOUSE_GAP - scroll - HOUSE_GAP * 3
            val nice = ((i * 7 + 3) % 5) != 0
            val done = i in visited.value
            Column(
                Modifier.offset(x = x.dp, y = (h - 270).dp).width(140.dp).squish(enabled = !done && !finished) {
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

        if (!christmas) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                Btn("🏠 Fly home", Pal.blue, 18, Modifier.width(180.dp)) { game.endDelivery(false) }
            }
        }
    }
}

@Composable
private fun RoofHouse(nice: Boolean, done: Boolean, seed: Int) {
    val wall = listOf(hex(0xE8C7A0), hex(0xC9E0F2), hex(0xF2C9D6), hex(0xD8EBC6))[((seed % 4) + 4) % 4]
    Canvas(Modifier.size(140.dp, 160.dp)) {
        val u = density
        fun r(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, Offset(x * u, y * u), Size(w * u, h * u))
        r(hex(0x6B4A3A), 92f, 8f, 22f, 34f)
        r(Color.White, 88f, 4f, 30f, 7f)
        drawPath(Path().apply { moveTo(0f, 60 * u); lineTo(70 * u, 14 * u); lineTo(140 * u, 60 * u); close() }, if (nice) hex(0xA02338) else hex(0x4A4458))
        r(Color.White, 8f, 54f, 124f, 7f)
        r(Pal.ink, 12f, 60f, 116f, 100f)
        r(wall, 15f, 63f, 110f, 97f)
        val lit = if (done && nice) hex(0xFFE27A) else if (nice) hex(0xFFCC4D) else hex(0x3A3550)
        r(Pal.ink, 26f, 80f, 30f, 30f); r(lit, 29f, 83f, 24f, 24f)
        r(Pal.ink, 84f, 80f, 30f, 30f); r(lit, 87f, 83f, 24f, 24f)
        r(hex(0x6B4A3A), 57f, 118f, 26f, 42f)
    }
}
