package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Stable: tap a reindeer to open its close-up care screen
// ======================================================================

@Composable
fun StableScreen(game: GameState) {
    var caring by rememberSaveable { mutableStateOf<String?>(null) }
    val name = caring
    if (name != null && name in game.reindeer) { ReindeerCare(game, name) { caring = null }; return }
    val s = game.s
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        JobChip(game, JobKind.Reindeer)
        Row(Modifier.panel().padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Px("carrot", 24.dp); Txt("×${s.crops.n(Crop.Carrot)}  ·  🛷 Team ${game.teamPower}/${game.sleighTeam.size} happy  ·  Tap a reindeer!", 14)
        }
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in game.reindeer.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (r in row) ReindeerTile(game, r) { caring = r; Audio.play(Sfx.SleighBells) }
            }
        }
    }
}

@Composable
private fun ReindeerTile(game: GameState, name: String, onOpen: () -> Unit) {
    val s = game.s
    val fed = name in s.fed; val brushed = name in s.brushed; val washed = name in s.washed
    Column(
        Modifier.width(128.dp).squish(onClick = onOpen).panel(if (fed && brushed) hex(0xD8F5DD) else Pal.cream).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.height(72.dp), contentAlignment = Alignment.BottomCenter) {
            Bob(if (fed && brushed) 5f else 2f, if (fed && brushed) 400 else 900) { ReindeerPx(name, game.decosOn(name), 62.dp) }
        }
        Txt(name, 15, maxLines = 1)
        Txt((if (fed) "🥕" else "▫️") + (if (brushed) "🪮" else "▫️") + (if (washed) "🫧" else "▫️") + (if (name in game.sleighTeam) "🛷" else ""), 12)
    }
}

private enum class CareTool(val label: String) { Feed("🥕 Feed"), Brush("🪮 Brush"), Wash("🫧 Wash"), Decorate("🎀 Dress up"), Sleigh("🛷 Sleigh") }
private enum class WashStep { Idle, Scrub, Rinse }

private const val RUB_DISTANCE = 450f   // how far (dp) you rub to finish brushing
private const val BUBBLES = 16
private val reindeerLines = listOf(
    "*sniffs your pocket for carrots*", "*nuzzles you* 🥰", "Snort! (that means thank you)", "*does a little prance*",
    "*sneezes glitter* ✨", "I can fly, you know. Just saying.", "*wiggles ears*", "Is it Christmas yet? Is it? IS IT?",
)

/** Full-screen close-up of one reindeer: feed, brush, wash, dress up and hitch to the sleigh. */
@Composable
private fun ReindeerCare(game: GameState, name: String, onBack: () -> Unit) {
    val s = game.s
    val fed = name in s.fed; val brushed = name in s.brushed; val washed = name in s.washed
    var tool by remember(name) { mutableStateOf(if (!fed) CareTool.Feed else if (!brushed) CareTool.Brush else CareTool.Wash) }
    var bites by remember(name) { mutableIntStateOf(-1) }      // -1 = no carrot out
    var rubbed by remember(name) { mutableFloatStateOf(0f) }
    var wash by remember(name) { mutableStateOf(WashStep.Idle) }
    val bubbles = remember(name) { mutableStateListOf<Offset>() }
    var say by remember(name) { mutableStateOf<String?>(null) }
    var drops by remember(name) { mutableStateOf<Offset?>(null) }

    val drag = rememberUpdatedState { at: Offset, dist: Float ->
        when (tool) {
            CareTool.Brush -> if (name !in game.s.brushed) {
                val before = rubbed
                rubbed += dist
                if ((rubbed / 90).toInt() > (before / 90).toInt()) Audio.play(Sfx.Tap)
                if (rubbed >= RUB_DISTANCE) { game.brush(name); say = "Ooh, that's the spot! ✨" }
            }
            CareTool.Wash -> when (wash) {
                WashStep.Scrub -> if (bubbles.isEmpty() || bubbles.last().minus(at).getDistance() > 26f) {
                    bubbles.add(at); if (bubbles.size % 3 == 0) Audio.play(Sfx.Pop)
                    if (bubbles.size >= BUBBLES) { wash = WashStep.Rinse; say = "So bubbly! Now rinse with the water 🚿" }
                }
                WashStep.Rinse -> {
                    drops = at
                    val before = bubbles.size
                    bubbles.removeAll { (it - at).getDistance() < 44f }
                    if (bubbles.size < before) Audio.play(Sfx.Tap)
                    if (bubbles.isEmpty()) { wash = WashStep.Idle; drops = null; game.wash(name); say = "*shakes like a dog* Splish splash! 💦" }
                }
                else -> {}
            }
            else -> {}
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn("🦌 All", Pal.brown, 15, Modifier.width(96.dp), onClick = onBack)
            Txt(if (name == "Rudolph") "$name 🔴" else name, 26, Color.White, modifier = Modifier.weight(1f))
            Txt((if (fed) "🥕✅" else "🥕") + (if (brushed) " 🪮✅" else " 🪮") + (if (washed) " 🫧✅" else " 🫧"), 14, Color.White)
        }

        // The stall with the big reindeer
        Box(
            Modifier.size(406.dp, 300.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))
                .pointerInput(name) {
                    detectDragGestures(onDragEnd = { drops = null }) { change, d ->
                        change.consume()
                        drag.value(Offset(change.position.x / density, change.position.y / density), d.getDistance() / density)
                    }
                }
                .pointerInput(name) { detectTapGestures { if (tool != CareTool.Feed || bites < 0) { say = reindeerLines.random(); Audio.play(Sfx.SleighBells) } } },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(hex(0x6C4A3A))
                var x = 0f
                while (x < size.width) { drawRect(hex(0x5E3E30), Offset(x, 0f), Size(4 * u, size.height * 0.72f)); x += 34 * u }
                drawRect(hex(0xE8C77A), Offset(0f, size.height * 0.72f), Size(size.width, size.height))
                for (i in 0 until 30) drawRect(hex(0xC9A050), Offset(((i * 53) % 400) * u, size.height * 0.74f + ((i * 29) % 70) * u), Size(14 * u, 2 * u))
                if (tool == CareTool.Wash && wash != WashStep.Idle) drawRect(hex(0x7EC8F0).copy(alpha = 0.25f))
            }
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)) {
                Bob(if (fed && brushed) 6f else 2f, if (fed && brushed) 420 else 900) { ReindeerPx(name, game.decosOn(name), 200.dp) }
            }
            if (tool == CareTool.Brush && name !in s.brushed && rubbed > 0) {
                val sparkles = (rubbed / 50).toInt().coerceAtMost(9)
                for (i in 0 until sparkles) Txt("✨", 20, modifier = Modifier.offset((70 + (i * 41) % 260).dp, (60 + (i * 37) % 150).dp))
            }
            for (b in bubbles) Box(Modifier.offset((b.x - 16).dp, (b.y - 16).dp).size(32.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp)).border(2.dp, hex(0x9BD3FF), RoundedCornerShape(16.dp)))
            drops?.let { d -> Txt("🚿", 40, modifier = Modifier.offset((d.x - 24).dp, (d.y - 60).dp)) }
            if (bites >= 0) {
                Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp).squish {
                    bites++
                    Audio.play(Sfx.Munch)
                    if (bites >= 3) { game.feed(name); bites = -1; say = "Crunch crunch! Best. Carrot. EVER. 🥕" }
                }) { Px("carrot", (80 - bites * 20).dp) }
            }
            say?.let { Txt(it, 15, align = TextAlign.Center, modifier = Modifier.align(Alignment.TopCenter).padding(8.dp).panel(Color.White).padding(horizontal = 10.dp, vertical = 6.dp)) }
        }

        // Tools
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (t in CareTool.entries) Txt(
                t.label, 13, if (tool == t) Color.White else Pal.ink, align = TextAlign.Center,
                modifier = Modifier.weight(1f).squish { tool = t; bites = -1; Audio.play(Sfx.Tap) }.panel(if (tool == t) Pal.red else Color.White).padding(vertical = 7.dp),
            )
        }

        Column(Modifier.fillMaxWidth().panel().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (tool) {
                CareTool.Feed -> when {
                    fed -> Txt("$name is full and happy! 🥕✅", 17, Pal.darkGreen)
                    bites >= 0 -> Txt("Tap the carrot! 🥕 (${bites}/3 bites)", 17, Pal.darkGreen)
                    else -> {
                        Txt("Carrots: ${s.crops.n(Crop.Carrot)}", 15)
                        Btn("Give a carrot 🥕 (⚡2)", hex(0xE0843A), 17, Modifier.width(260.dp)) {
                            if (s.crops.n(Crop.Carrot) <= 0) game.feed(name) else if (game.hasEnergy(2)) bites = 0
                        }
                    }
                }
                CareTool.Brush -> if (brushed) Txt("$name is super shiny! ✨", 17, Pal.darkGreen) else {
                    Txt("Rub $name with your finger to brush and pet! (⚡2)", 16, align = TextAlign.Center)
                    Bar(rubbed / RUB_DISTANCE, hex(0xFFCC4D))
                }
                CareTool.Wash -> when {
                    washed -> Txt("$name is squeaky clean! 🫧✅", 17, Pal.darkGreen)
                    wash == WashStep.Idle -> {
                        Txt("A bubble bath makes reindeer extra happy!", 15, align = TextAlign.Center)
                        Btn("🧽 Start the bath (⚡2)", Pal.blue, 17, Modifier.width(260.dp)) { if (game.hasEnergy(2)) { bubbles.clear(); wash = WashStep.Scrub; say = "Scrub scrub! Rub to make bubbles 🧽" } }
                    }
                    wash == WashStep.Scrub -> { Txt("Rub to make bubbles! 🧽", 16); Bar(bubbles.size / BUBBLES.toFloat(), hex(0x9BD3FF)) }
                    else -> { Txt("Drag the water over the bubbles to rinse! 🚿", 16, align = TextAlign.Center); Txt("${bubbles.size} bubbles left", 14) }
                }
                CareTool.Decorate -> {
                    Txt("Tap to put on or take off. They show everywhere $name goes!", 14, align = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in Deco.entries) {
                            val on = d in game.decosOn(name)
                            Column(
                                Modifier.weight(1f).squish { game.toggleDeco(name, d) }.panel(if (on) hex(0xFFF1B8) else Color.White, if (on) Pal.red else Pal.ink).padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Txt(d.emoji, 22)
                                Txt(d.title, 11, align = TextAlign.Center, maxLines = 2)
                                Txt(if (d !in s.decos) money(d.price) else if (on) "On ✨" else "Put on", 11, if (d !in s.decos) Pal.red else Pal.darkGreen)
                            }
                        }
                    }
                }
                CareTool.Sleigh -> {
                    val inTeam = name in s.team.filter { it in game.reindeer }
                    Txt("Sleigh team: " + game.sleighTeam.joinToString(", ") { if (it in s.fed && it in s.brushed) "$it 😊" else "$it 😐" }, 14, align = TextAlign.Center)
                    Txt("Fed + brushed team reindeer steer the sleigh better and fly faster through clouds!", 13, Pal.brown, align = TextAlign.Center)
                    Btn(if (inTeam) "Unhitch $name" else "🛷 Hitch $name to the sleigh", if (inTeam) Pal.brown else Pal.green, 16, Modifier.fillMaxWidth()) { game.toggleTeam(name) }
                }
            }
        }
    }
}

@Composable
fun Bar(frac: Float, color: Color, width: Int = 260) {
    Box(Modifier.size(width.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(Color.White).border(2.dp, Pal.ink, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxHeight().width(width.dp * frac.coerceIn(0f, 1f)).background(color))
    }
}
