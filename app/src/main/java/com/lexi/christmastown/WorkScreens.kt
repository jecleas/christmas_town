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
import androidx.compose.runtime.LaunchedEffect
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
        WorkmateBar(game, Place.Greenhouse)
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


/** Your workmate at this job: tips when you tap them, a cheer when you finish, and a little help once a day. */
@Composable
fun WorkmateBar(game: GameState, place: Place) {
    val w = workmateAt(place) ?: return
    val npc = npcs.first { it.name == w.name }
    var tip by remember(place) { mutableIntStateOf(0) }
    var said by remember(place) { mutableStateOf<String?>(null) }
    val cheer = game.workmateCheer?.takeIf { it.first == w.name }?.second
    LaunchedEffect(cheer) { if (cheer != null) { kotlinx.coroutines.delay(4000); game.workmateCheer = null } }
    Row(
        Modifier.fillMaxWidth().panel(if (cheer != null) hex(0xD8F5DD) else Color.White).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.squish { tip++; said = w.tips[tip % w.tips.size]; game.chat(npc); Audio.play(Sfx.Message) }) {
            Bob(if (cheer != null) 8f else 2f, if (cheer != null) 250 else 900) { ElfPx(npc.look, 56.dp) }
        }
        Column(Modifier.weight(1f)) {
            Txt("${npc.name} · your workmate", 12, Pal.purple)
            Txt(cheer ?: said ?: "Hi ${game.s.name}! ${w.tips.first()}", 14, if (cheer != null) Pal.darkGreen else Pal.ink)
        }
        Btn("🤝 Help", Pal.green, 14, Modifier.width(88.dp), enabled = w.name !in game.s.helpedBy) { game.workmateHelp(w) }
    }
}
