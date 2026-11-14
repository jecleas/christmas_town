package com.lexi.christmastown

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Bob(amount: Float = 4f, millis: Int = 700, content: @Composable () -> Unit) {
    val t = rememberInfiniteTransition(label = "bob")
    val v by t.animateFloat(0f, 1f, infiniteRepeatable(tween(millis, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    Box(Modifier.graphicsLayer { translationY = -amount * v * density }) { content() }
}

@Composable
fun JobChip(game: GameState, kind: JobKind) {
    val job = game.s.jobs.firstOrNull { it.kind == kind } ?: return
    Txt(
        "${kind.emoji} ${job.text}: ${if (job.finished) "✅" else "${job.done}/${job.goal}"}", 16,
        if (job.finished) Color.White else Pal.ink, align = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().panel(if (job.finished) Pal.green else Color.White).padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

// MARK: - Greenhouse

@Composable
fun GreenhouseScreen(game: GameState) {
    val s = game.s
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt("Pick a seed, tap a pot to plant, water it, then pick your crops! 🌱", 15, align = TextAlign.Center)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            for (c in game.crops) {
                val sel = game.selectedSeed == c
                Column(
                    Modifier.squish { game.selectedSeed = c }.panel(if (sel) hex(0xFFF1B8) else Color.White, if (sel) Pal.red else Pal.ink).padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { Px(c.sprite, 30.dp); Txt("${c.title}: ${s.seeds.n(c)}", 13) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn("🌱 Plant all", Pal.green, 14, Modifier.weight(1f)) { game.plantAll() }
            Btn("💧 Water all", Pal.blue, 14, Modifier.weight(1f)) { game.waterAll() }
            Btn("🧺 Pick all", Pal.brown, 14, Modifier.weight(1f)) { game.pickAll() }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in s.plots.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (p in row) PlotBox(p, game.now) { game.tapPlot(p.id) }
            }
        }
        Row(Modifier.fillMaxWidth().panel().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Txt("Basket:", 15)
            for (c in game.crops) Row(verticalAlignment = Alignment.CenterVertically) { Px(c.sprite, 24.dp); Txt("×${s.crops.n(c)}", 14) }
        }
    }
}

@Composable
private fun PlotBox(p: Plot, now: Long, onTap: () -> Unit) {
    Box(Modifier.size(128.dp, 104.dp).squish(onClick = onTap).panel(hex(0x8A5A3A)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(118.dp, 94.dp)) {
            for (i in 0..2) drawRect(hex(0x6E4429), Offset(8 * density, (22 + i * 26) * density), Size(size.width - 16 * density, 5 * density))
        }
        when (p.stage(now)) {
            0 -> Txt("+ Plant", 17, Color.White.copy(alpha = 0.85f))
            1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Px("sprout", 38.dp); Bob { Txt("💧 Water me!", 13, Color.White) } }
            2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Px("sprout", (38 + 18 * p.progress(now)).dp)
                Box(Modifier.size(90.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.3f))) {
                    Box(Modifier.fillMaxHeight().width(90.dp * p.progress(now)).background(Pal.green))
                }
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Bob { Px(p.crop!!.sprite, 56.dp) }
                Txt("Pick me! ✨", 13, Color.White)
            }
        }
    }
}

// MARK: - Bakery

@Composable
fun BakeryScreen(game: GameState) {
    val s = game.s
    val scope = rememberCoroutineScope()
    var baking by remember { mutableStateOf<Cookie?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        JobChip(game, JobKind.Bake)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Bob { ElfPx(s.look, 140.dp) }
            Oven(baking)
        }
        for (c in Cookie.entries) {
            val locked = c.unlockLevel > game.level
            Row(
                Modifier.fillMaxWidth().panel(if (locked) hex(0xD5DBE6) else Color.White).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Px(c.sprite, 48.dp)
                Column(Modifier.weight(1f)) {
                    Txt("${c.title}  ×${s.cookies.n(c)}", 16)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for ((crop, n) in c.recipe) { Txt("$n×", 13); Px(crop.sprite, 20.dp) }
                        Txt(" ⚡5", 13)
                    }
                }
                if (locked) Tag("🔒 Lv ${c.unlockLevel}", hex(0x8D96A8))
                else Btn("Bake!", Pal.red, 16, Modifier.width(90.dp), enabled = baking == null) {
                    if (game.canBake(c)) {
                        baking = c
                        scope.launch { delay(1200); game.bake(c); baking = null }
                    } else game.say("Not enough ingredients! Grow some in the Greenhouse 🌱")
                }
            }
        }
    }
}

@Composable
private fun Oven(baking: Cookie?) {
    Box(Modifier.size(170.dp, 180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp, 180.dp)) {
            val u = density
            drawRect(Pal.ink, Offset(0f, 0f), Size(170 * u, 180 * u))
            drawRect(hex(0xC9CED8), Offset(4 * u, 4 * u), Size(162 * u, 172 * u))
            drawRect(Pal.ink, Offset(20 * u, 48 * u), Size(130 * u, 100 * u))
            drawRect(if (baking != null) hex(0xFF9A3C) else hex(0x3A2A3E), Offset(24 * u, 52 * u), Size(122 * u, 92 * u))
            for (i in 0..3) drawRect(Pal.red, Offset((26 + i * 34) * u, 16 * u), Size(16 * u, 16 * u))
        }
        if (baking != null) Bob(3f, 300) { Px(baking.sprite, 60.dp) }
    }
}

// MARK: - Stable

@Composable
fun StableScreen(game: GameState) {
    val s = game.s
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        JobChip(game, JobKind.Reindeer)
        Row(Modifier.panel().padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Px("carrot", 24.dp); Txt("Carrots ×${s.crops.n(Crop.Carrot)}  (grow more in the Greenhouse)", 14)
        }
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (pair in game.reindeer.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (name in pair) ReindeerCard(game, name)
            }
        }
    }
}

@Composable
private fun ReindeerCard(game: GameState, name: String) {
    val fed = name in game.s.fed
    val brushed = name in game.s.brushed
    val rudolph = name == "Rudolph"
    Column(
        Modifier.width(196.dp).panel(if (fed && brushed) hex(0xD8F5DD) else Pal.cream).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.height(86.dp), contentAlignment = Alignment.BottomCenter) {
            val img = Sprites.bitmap("reindeer|$rudolph", Sprites.rows("reindeer"), mapOf('N' to if (rudolph) Pal.red else Pal.ink))
            Bob(if (fed && brushed) 6f else 2f, if (fed && brushed) 400 else 900) { Px(img, 80.dp) }
        }
        Txt(if (rudolph) "$name 🔴" else name, 17)
        Txt((if (fed) "🥕✅ " else "🥕❌ ") + (if (brushed) "🪮✅" else "🪮❌"), 13)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn("Feed", hex(0xE0843A), 13, Modifier.weight(1f), enabled = !fed) { game.feed(name) }
            Btn("Brush", Pal.blue, 13, Modifier.weight(1f), enabled = !brushed) { game.brush(name) }
        }
    }
}

// MARK: - Workshop

@Composable
fun WorkshopScreen(game: GameState) {
    var toy by remember { mutableStateOf(Toy.Teddy) }
    var step by remember { mutableIntStateOf(0) }   // 0 = not started
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        JobChip(game, JobKind.Toys)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (t in Toy.entries) {
                val locked = t.unlockLevel > game.level
                Column(
                    Modifier.width(78.dp)
                        .squish(enabled = step == 0) { if (locked) game.say("${t.title}s unlock at Level ${t.unlockLevel} 🔒") else { toy = t; done = false } }
                        .panel(if (toy == t) hex(0xFFF1B8) else Color.White, if (toy == t) Pal.red else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Px(t.sprite, 40.dp, alpha = if (locked) 0.3f else 1f)
                    Txt(if (locked) "🔒 Lv${t.unlockLevel}" else "×${game.s.toys.n(t)}", 12)
                }
            }
        }
        Box(Modifier.size(300.dp, 250.dp).panel(hex(0xC98B5E)), contentAlignment = Alignment.Center) {
            val reveal = if (done) 1f else step / toy.steps.size.toFloat()
            Px(toy.sprite, 200.dp, alpha = 0.15f)
            Bob(if (done) 8f else 0f, 300) { Px(toy.sprite, 200.dp, reveal = reveal) }
        }
        Txt(
            when {
                done -> "Ta-da! A ${toy.title}! 🎁"
                step == 0 -> "Make a ${toy.title}!"
                else -> "Step $step of ${toy.steps.size}"
            }, 20, align = TextAlign.Center,
        )
        when {
            done -> {}
            step == 0 -> Btn("🔨 Start (⚡5)", Pal.red, 20, Modifier.width(220.dp)) { if (game.startToy()) step = 1 }
            else -> Btn(toy.steps[step - 1] + "!", Pal.green, 20, Modifier.width(260.dp)) {
                if (step == toy.steps.size) {
                    done = true
                    game.finishToy(toy)
                    scope.launch { delay(1300); done = false; step = 0 }
                } else { step++; Audio.play(Sfx.Pop) }
            }
        }
        Bob { ElfPx(game.s.look, 110.dp) }
    }
}
