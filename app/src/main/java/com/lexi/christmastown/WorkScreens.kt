package com.lexi.christmastown

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
private fun JobChip(game: GameState, kind: JobKind) {
    val job = game.s.jobs.firstOrNull { it.kind == kind } ?: return
    Txt(
        "${kind.emoji} ${job.text}: ${if (job.finished) "✅" else "${job.done}/${job.goal}"}", 16,
        if (job.finished) Color.White else Pal.ink,
        modifier = Modifier.panel(if (job.finished) Pal.green else Color.White).padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun Elf(game: GameState, height: Int = 130) {
    Bob { ElfPx(game.s.look, height.dp) }
}

// MARK: - Greenhouse

@Composable
fun GreenhouseScreen(game: GameState) {
    val s = game.s
    Row(horizontalArrangement = Arrangement.spacedBy(30.dp), verticalAlignment = Alignment.Bottom) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Bubble("Plant seeds, water them, then pick your crops! 🌱", 200.dp)
            Elf(game)
            Column(Modifier.panel().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Txt("Basket", 16)
                for (c in game.crops) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Px(c.sprite, 24.dp); Txt("${c.title} ×${s.crops.n(c)}", 14)
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Txt("Plant:", 18)
                for (c in game.crops) {
                    val sel = game.selectedSeed == c
                    Row(
                        Modifier.squish { game.selectedSeed = c }.panel(if (sel) hex(0xFFF1B8) else Color.White, if (sel) Pal.red else Pal.ink).padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) { Px(c.sprite, 26.dp); Txt("${s.seeds.n(c)}", 16) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("🌱 Plant all", Pal.green, 15) { game.plantAll() }
                Btn("💧 Water all", Pal.blue, 15) { game.waterAll() }
                Btn("🧺 Pick all", Pal.brown, 15) { game.pickAll() }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in s.plots.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (p in row) PlotBox(p, game.now) { game.tapPlot(p.id) }
                }
            }
        }
    }
}

@Composable
private fun PlotBox(p: Plot, now: Long, onTap: () -> Unit) {
    Box(Modifier.size(150.dp, 108.dp).squish(onClick = onTap).panel(hex(0x8A5A3A)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(140.dp, 98.dp)) {
            for (i in 0..2) drawRect(hex(0x6E4429), Offset(10 * density, (24 + i * 26) * density), Size(size.width - 20 * density, 5 * density))
        }
        when (p.stage(now)) {
            0 -> Txt("+ Plant", 17, Color.White.copy(alpha = 0.85f))
            1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Px("sprout", 40.dp); Bob { Txt("💧 Water me!", 14, Color.White) } }
            2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Px("sprout", (40 + 20 * p.progress(now)).dp)
                Box(Modifier.size(100.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.3f))) {
                    Box(Modifier.fillMaxHeight().width(100.dp * p.progress(now)).background(Pal.green))
                }
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Bob { Px(p.crop!!.sprite, 60.dp) }
                Txt("Pick me! ✨", 14, Color.White)
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

    Row(horizontalArrangement = Arrangement.spacedBy(30.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            JobChip(game, JobKind.Bake)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Elf(game, 140)
                Oven(baking)
            }
            Row(Modifier.panel().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Txt("Cookie jar:", 16)
                for (c in Cookie.entries) Row(verticalAlignment = Alignment.CenterVertically) { Px(c.sprite, 24.dp); Txt("×${s.cookies.n(c)}", 15) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Txt("Recipes", 24)
            for (c in Cookie.entries) {
                val locked = c.unlockLevel > game.level
                Row(
                    Modifier.width(440.dp).panel(if (locked) hex(0xD5DBE6) else Color.White).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Px(c.sprite, 54.dp)
                    Column(Modifier.weight(1f)) {
                        Txt(c.title, 18)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for ((crop, n) in c.recipe) { Txt("$n×", 14); Px(crop.sprite, 22.dp) }
                            Txt("  ⚡5", 14)
                        }
                    }
                    if (locked) Tag("🔒 Level ${c.unlockLevel}", hex(0x8D96A8))
                    else Btn("Bake!", Pal.red, 16, enabled = baking == null) {
                        if (game.canBake(c)) {
                            baking = c
                            scope.launch { delay(1200); game.bake(c); baking = null }
                        } else game.say("Not enough ingredients! Grow some in the Greenhouse 🌱")
                    }
                }
            }
        }
    }
}

@Composable
private fun Oven(baking: Cookie?) {
    Box(Modifier.size(170.dp, 190.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp, 190.dp)) {
            val u = density
            drawRect(Pal.ink, Offset(0f, 0f), Size(170 * u, 190 * u))
            drawRect(hex(0xC9CED8), Offset(4 * u, 4 * u), Size(162 * u, 182 * u))
            drawRect(Pal.ink, Offset(20 * u, 50 * u), Size(130 * u, 100 * u))
            drawRect(if (baking != null) hex(0xFF9A3C) else hex(0x3A2A3E), Offset(24 * u, 54 * u), Size(122 * u, 92 * u))
            for (i in 0..3) drawRect(Pal.red, Offset((26 + i * 34) * u, 16 * u), Size(16 * u, 16 * u))
        }
        if (baking != null) Bob(3f, 300) { Px(baking.sprite, 60.dp) }
    }
}

// MARK: - Stable

@Composable
fun StableScreen(game: GameState) {
    val s = game.s
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            JobChip(game, JobKind.Reindeer)
            Row(Modifier.panel().padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Px("carrot", 26.dp); Txt("Carrots ×${s.crops.n(Crop.Carrot)}", 16)
            }
        }
        Row(Modifier.width(1040.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (name in game.reindeer) {
                val fed = name in s.fed
                val brushed = name in s.brushed
                val rudolph = name == "Rudolph"
                Column(
                    Modifier.width(196.dp).panel(if (fed && brushed) hex(0xD8F5DD) else Pal.cream).padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.height(110.dp), contentAlignment = Alignment.BottomCenter) {
                        val img = Sprites.bitmap("reindeer|$rudolph", Sprites.rows("reindeer"), mapOf('N' to if (rudolph) Pal.red else Pal.ink))
                        Bob(if (fed && brushed) 6f else 2f, if (fed && brushed) 400 else 900) { Px(img, 100.dp) }
                    }
                    Txt(if (rudolph) "$name 🔴" else name, 18)
                    Txt((if (fed) "🥕✅ " else "🥕❌ ") + (if (brushed) "🪮✅" else "🪮❌"), 14)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Btn("Feed", hex(0xE0843A), 14, enabled = !fed) { game.feed(name) }
                        Btn("Brush", Pal.blue, 14, enabled = !brushed) { game.brush(name) }
                    }
                }
            }
        }
        Txt("Reindeer eat carrots. Grow more in the Greenhouse! 🥕", 16, Color.White)
    }
}


// MARK: - Workshop

@Composable
fun WorkshopScreen(game: GameState) {
    var toy by remember { mutableStateOf(Toy.Teddy) }
    var step by remember { mutableIntStateOf(0) }   // 0 = not started
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(horizontalArrangement = Arrangement.spacedBy(30.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(250.dp).panel().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Txt("Toys to make", 20)
            for (t in Toy.entries) {
                val locked = t.unlockLevel > game.level
                Row(
                    Modifier.fillMaxWidth()
                        .squish(enabled = !locked && step == 0) { toy = t; done = false }
                        .panel(if (toy == t) hex(0xFFF1B8) else Color.White, if (toy == t) Pal.red else Pal.ink).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Px(t.sprite, 32.dp, alpha = if (locked) 0.3f else 1f)
                    Txt(t.title, 15, modifier = Modifier.weight(1f))
                    if (locked) Txt("🔒Lv${t.unlockLevel}", 13) else Txt("×${game.s.toys.n(t)}", 14)
                }
            }
        }

        Column(Modifier.width(460.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            JobChip(game, JobKind.Toys)
            Box(Modifier.size(300.dp, 260.dp).panel(hex(0xC98B5E)), contentAlignment = Alignment.Center) {
                val reveal = if (done) 1f else step / (toy.steps.size + 0f)
                Px(toy.sprite, 200.dp, alpha = 0.15f)
                Bob(if (done) 8f else 0f, 300) { Px(toy.sprite, 200.dp, reveal = reveal) }
            }
            val label = when {
                done -> "Ta-da! A ${toy.title}! 🎁"
                step == 0 -> "Tap Start to make a ${toy.title}"
                else -> "Step $step of ${toy.steps.size}"
            }
            Txt(label, 20, align = TextAlign.Center)
            when {
                done -> {}
                step == 0 -> Btn("🔨 Start (⚡5)", Pal.red, 20) { if (game.startToy()) step = 1 }
                step <= toy.steps.size -> Btn(toy.steps[step - 1] + "!", Pal.green, 20) {
                    if (step == toy.steps.size) {
                        done = true
                        game.finishToy(toy)
                        scope.launch { delay(1300); done = false; step = 0 }
                    } else step++
                }
            }
        }

        Bob { ElfPx(game.s.look, 150.dp) }
    }
}
