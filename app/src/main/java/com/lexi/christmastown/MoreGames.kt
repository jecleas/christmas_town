package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

private val confetti = listOf(hex(0xE5394B), hex(0x3FAF5A), hex(0x3D6FD6), hex(0xFFCC4D), hex(0xFF8FB0), hex(0x9B6BE0))

/** Little tabs at the top of a place that has more than one thing to do. */
@Composable
fun <T> Tabs(options: List<T>, selected: T, label: (T) -> String, color: Color = Pal.red, onPick: (T) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (o in options) Txt(
            label(o), 15, if (o == selected) Color.White else Pal.ink, align = TextAlign.Center,
            modifier = Modifier.weight(1f).squish { onPick(o); Audio.play(Sfx.Tap) }.panel(if (o == selected) color else Color.White).padding(vertical = 7.dp),
        )
    }
}

// ======================================================================
// MARK: - Wrapping presents: pick a toy and paper, fold, tape, bow
// ======================================================================

private enum class WrapPhase { Pick, Paper, Fold, Tape, Bow, Done }
private enum class Paper(val label: String, val base: Color, val ink: Color) {
    Stripes("Candy stripes", hex(0xE5394B), Color.White), Dots("Green dots", hex(0x3FAF5A), hex(0xFFCC4D)),
    Stars("Blue stars", hex(0x3D6FD6), Color.White), Hearts("Pink hearts", hex(0xFF8FB0), hex(0xE5394B)),
}
private val bowColors = listOf(hex(0xFFCC4D), hex(0xE5394B), hex(0xF4F8FF), hex(0x9B6BE0))
private val foldDirs = listOf(Offset(1f, 0f), Offset(-1f, 0f), Offset(0f, 1f))   // left flap →, right flap ←, top flap ↓
private val tapeSpots = listOf(0.3f to 0.5f, 0.7f to 0.5f, 0.5f to 0.22f)

@Composable
fun WrapGame(game: GameState) {
    var phase by remember { mutableStateOf(WrapPhase.Pick) }
    var toy by remember { mutableStateOf(Toy.Teddy) }
    var paper by remember { mutableStateOf(Paper.Stripes) }
    var folds by remember { mutableIntStateOf(0) }
    var pull by remember { mutableStateOf(Offset.Zero) }
    val tapes = remember { mutableStateListOf<Int>() }
    var bow by remember { mutableStateOf<Color?>(null) }
    val scope = rememberCoroutineScope()

    val onDrag = rememberUpdatedState { d: Offset ->
        if (phase == WrapPhase.Fold && folds < 3) {
            pull += d
            val dir = foldDirs[folds]
            if (pull.x * dir.x + pull.y * dir.y > 80f) {
                folds++; pull = Offset.Zero; Audio.play(Sfx.Pop)
                if (folds == 3) { phase = WrapPhase.Tape; game.say("Neat folding! Now tape it down 📎") }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JobChip(game, JobKind.Wrap)
        if (phase == WrapPhase.Pick) {
            Txt("Wrapped presents: ${game.s.wrapped} 🎁", 17)
            val owned = Toy.entries.filter { game.s.toys.n(it) > 0 }
            if (owned.isEmpty()) {
                Bubble("You need a toy to wrap! Make one in the 🔨 Make tab first.", 300.dp)
            } else {
                Txt("Pick a toy to wrap!", 19)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in owned) Column(
                        Modifier.width(78.dp).squish { if (game.startWrap(t)) { toy = t; folds = 0; pull = Offset.Zero; tapes.clear(); bow = null; phase = WrapPhase.Paper } }
                            .panel(Color.White).padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { Px(t.sprite, 40.dp); Txt("×${game.s.toys.n(t)}", 12) }
                }
                Txt("Wrapping uses ⚡3. Wrapped presents go on the sleigh first!", 14, Pal.brown, align = TextAlign.Center)
            }
            return@Column
        }

        Box(
            Modifier.size(320.dp, 280.dp).panel(hex(0xC98B5E))
                .pointerInput(Unit) { detectDragGestures { c, d -> c.consume(); onDrag.value(d / density) } },
        ) {
            Canvas(Modifier.fillMaxSize()) { drawWrap(paper, folds, pull, tapes, bow, phase) }
            if (folds < 3) Box(Modifier.align(Alignment.Center).offset(y = 8.dp)) { Px(toy.sprite, 70.dp) }
            if (phase == WrapPhase.Fold && folds < 3) {
                val arrow = listOf("👉", "👈", "👇")[folds]
                val at = listOf(Modifier.align(Alignment.CenterStart).padding(start = 10.dp), Modifier.align(Alignment.CenterEnd).padding(end = 10.dp), Modifier.align(Alignment.TopCenter).padding(top = 6.dp))[folds]
                Box(at) { Bob(6f, 400) { Txt(arrow, 34) } }
            }
            if (phase == WrapPhase.Tape) for ((i, spot) in tapeSpots.withIndex()) if (i !in tapes) {
                Box(
                    Modifier.offset((spot.first * 314 - 22).dp, (spot.second * 274 - 22).dp).size(44.dp)
                        .squish { tapes.add(i); Audio.play(Sfx.Tap); if (tapes.size == 3) { phase = WrapPhase.Bow; game.say("Now pick a bow! 🎀") } }
                        .background(hex(0xFFCC4D), CircleShape).border(3.dp, Pal.ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Txt("📎", 18) }
            }
            if (phase == WrapPhase.Done) Bob(10f, 300) { Txt("✨🎁✨", 34, modifier = Modifier.padding(top = 4.dp)) }
        }

        when (phase) {
            WrapPhase.Paper -> {
                Txt("Pick your wrapping paper!", 18)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (p in Paper.entries) Column(
                        Modifier.weight(1f).squish { paper = p; phase = WrapPhase.Fold; Audio.play(Sfx.Pop) }.panel(Color.White, if (paper == p) Pal.red else Pal.ink).padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Canvas(Modifier.size(56.dp)) { drawPaper(p, Rect(Offset.Zero, size)) }
                        Txt(p.label, 11, align = TextAlign.Center)
                    }
                }
            }
            WrapPhase.Fold -> Txt("Fold ${folds + 1} of 3: swipe the paper ${listOf("to the right ➡️", "to the left ⬅️", "down ⬇️")[folds.coerceAtMost(2)]}", 17, align = TextAlign.Center)
            WrapPhase.Tape -> Txt("Tap the 📎 spots to tape it shut! (${tapes.size}/3)", 17, align = TextAlign.Center)
            WrapPhase.Bow -> {
                Txt("Pick a bow color! 🎀", 18)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in bowColors) Box(
                        Modifier.size(46.dp).squish {
                            bow = c; phase = WrapPhase.Done
                            game.finishWrap(toy)
                            scope.launch { delay(1700); phase = WrapPhase.Pick }
                        }.background(c, CircleShape).border(3.dp, Pal.ink, CircleShape),
                    )
                }
            }
            WrapPhase.Done -> Txt("Ta-da! A ${paper.label.lowercase()} present! 🎁", 19, Pal.red, align = TextAlign.Center)
            WrapPhase.Pick -> {}
        }
        if (phase != WrapPhase.Done) Btn("✖ Stop", hex(0x8D96A8), 14, Modifier.width(110.dp)) { phase = WrapPhase.Pick }
    }
}

private fun DrawScope.drawPaper(p: Paper, r: Rect) {
    val u = density
    drawRect(p.base, r.topLeft, r.size)
    clipRect(r.left, r.top, r.right, r.bottom) {
        when (p) {
            Paper.Stripes -> { var x = r.left - r.height; while (x < r.right) { drawPath(Path().apply { moveTo(x, r.bottom); lineTo(x + 10 * u, r.bottom); lineTo(x + 10 * u + r.height, r.top); lineTo(x + r.height, r.top); close() }, p.ink); x += 22 * u } }
            Paper.Dots -> { var y = r.top + 6 * u; var row = 0; while (y < r.bottom) { var x = r.left + (if (row % 2 == 0) 6f else 16f) * u; while (x < r.right) { drawCircle(p.ink, 3.5f * u, Offset(x, y)); x += 20 * u }; y += 14 * u; row++ } }
            Paper.Stars -> { var y = r.top + 8 * u; var row = 0; while (y < r.bottom) { var x = r.left + (if (row % 2 == 0) 8f else 20f) * u; while (x < r.right) { drawPath(starPath(Offset(x, y), 6 * u, 2.5f * u), p.ink); x += 24 * u }; y += 18 * u; row++ } }
            Paper.Hearts -> { var y = r.top + 8 * u; var row = 0; while (y < r.bottom) { var x = r.left + (if (row % 2 == 0) 8f else 20f) * u; while (x < r.right) { drawPath(heartPath(Offset(x, y), 5 * u), p.ink); x += 24 * u }; y += 18 * u; row++ } }
        }
    }
}

private fun DrawScope.drawWrap(p: Paper, folds: Int, pull: Offset, tapes: List<Int>, bow: Color?, phase: WrapPhase) {
    val u = density
    val c = Offset(size.width / 2, size.height / 2 + 10 * u)
    val box = Rect(c.x - 80 * u, c.y - 55 * u, c.x + 80 * u, c.y + 55 * u)
    val sheet = Rect(box.left - 55 * u, box.top - 50 * u, box.right + 55 * u, box.bottom + 20 * u)
    if (phase == WrapPhase.Pick) return
    if (folds < 3) {
        // the flat sheet with the toy's box on it
        drawRect(Pal.ink, sheet.topLeft - Offset(3 * u, 3 * u), Size(sheet.width + 6 * u, sheet.height + 6 * u))
        drawPaper(p, sheet)
        drawRect(hex(0xE8D2A8), box.topLeft, box.size)
        drawRect(Pal.ink, box.topLeft, box.size, style = Stroke(3 * u))
    }
    // flaps folded so far (with a little wiggle following your finger)
    val wiggle = (pull.getDistance() * u * 0.4f).coerceAtMost(40 * u)
    if (folds >= 1) { drawPaper(p, Rect(box.left, box.top, c.x + 4 * u, box.bottom)); drawRect(Pal.ink, Offset(box.left, box.top), Size(box.width / 2 + 4 * u, box.height), style = Stroke(3 * u)) }
    else if (phase == WrapPhase.Fold) drawPaper(p, Rect(box.left - 55 * u + wiggle, box.top, box.left + wiggle, box.bottom))
    if (folds >= 2) { drawPaper(p, Rect(c.x - 4 * u, box.top, box.right, box.bottom)); drawRect(Pal.ink, Offset(c.x - 4 * u, box.top), Size(box.width / 2 + 4 * u, box.height), style = Stroke(3 * u)) }
    else if (folds == 1) drawPaper(p, Rect(box.right - wiggle, box.top, box.right + 55 * u - wiggle, box.bottom))
    if (folds >= 3) {
        drawPaper(p, box)
        drawRect(Pal.ink, box.topLeft, box.size, style = Stroke(3 * u))
        drawRect(Color.Black.copy(alpha = 0.12f), Offset(box.left, box.bottom - 12 * u), Size(box.width, 12 * u))
    } else if (folds == 2) drawPaper(p, Rect(box.left, box.top - 50 * u + wiggle, box.right, box.top + wiggle))
    for (i in tapes) {
        val (fx, fy) = tapeSpots[i]
        drawRect(Color.White.copy(alpha = 0.75f), Offset(fx * size.width - 16 * u, fy * size.height - 6 * u + 20 * u), Size(32 * u, 12 * u))
    }
    if (bow != null) {
        drawRect(bow, Offset(c.x - 8 * u, box.top), Size(16 * u, box.height))
        drawRect(bow, Offset(box.left, c.y - 8 * u), Size(box.width, 16 * u))
        val top = Offset(c.x, box.top - 4 * u)
        for (sx in listOf(-1f, 1f)) {
            drawOval(Pal.ink, Offset(top.x + sx * 26 * u - 18 * u, top.y - 24 * u), Size(36 * u, 26 * u))
            drawOval(bow, Offset(top.x + sx * 26 * u - 15 * u, top.y - 21 * u), Size(30 * u, 20 * u))
        }
        drawCircle(Pal.ink, 11 * u, top); drawCircle(bow, 8 * u, top)
        val rnd = Random(3)
        repeat(18) { drawRect(confetti[it % confetti.size], Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height * 0.4f), Size(6 * u, 6 * u)) }
    }
}

fun starPath(c: Offset, outer: Float, inner: Float) = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = -Math.PI / 2 + i * Math.PI / 5
        val x = (c.x + r * kotlin.math.cos(a)).toFloat(); val y = (c.y + r * kotlin.math.sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

fun heartPath(p: Offset, s: Float) = Path().apply {
    moveTo(p.x, p.y + s)
    cubicTo(p.x - 1.6f * s, p.y - 0.2f * s, p.x - 0.8f * s, p.y - 1.4f * s, p.x, p.y - 0.5f * s)
    cubicTo(p.x + 0.8f * s, p.y - 1.4f * s, p.x + 1.6f * s, p.y - 0.2f * s, p.x, p.y + s)
    close()
}

// ======================================================================
// MARK: - Gingerbread house: drag the pieces together, pipe icing, add candy
// ======================================================================

private enum class GingerPhase { Build, Ice, Candy, Done }
private enum class Piece(val label: String, val target: Rect) {
    Base("Plate", Rect(40f, 200f, 360f, 226f)),
    Walls("Walls", Rect(90f, 110f, 310f, 200f)),
    Roof("Roof", Rect(70f, 30f, 330f, 116f)),
    Door("Door", Rect(176f, 140f, 224f, 200f)),
}
private val trayStart = mapOf(Piece.Base to Offset(10f, 300f), Piece.Walls to Offset(10f, 250f), Piece.Roof to Offset(150f, 250f), Piece.Door to Offset(330f, 280f))
private const val ICE_RUB = 700f

@Composable
fun GingerbreadGame(game: GameState, onExit: () -> Unit) {
    var phase by remember { mutableStateOf(GingerPhase.Build) }
    val pos = remember { mutableStateOf(trayStart) }
    val placed = remember { mutableStateListOf<Piece>() }
    var iced by remember { mutableFloatStateOf(0f) }
    val candies = remember { mutableStateListOf<Triple<Float, Float, Int>>() }
    val scope = rememberCoroutineScope()
    val icing = rememberUpdatedState { d: Float ->
        if (phase == GingerPhase.Ice) {
            val before = iced; iced += d
            if ((iced / 120).toInt() > (before / 120).toInt()) Audio.play(Sfx.Pop)
            if (iced >= ICE_RUB) { phase = GingerPhase.Candy; game.say("Snowy icing! Now tap to stick on candy 🍬") }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt(
            when (phase) {
                GingerPhase.Build -> "Drag each piece onto its shadow! (${placed.size}/4)"
                GingerPhase.Ice -> "Rub along the roof to pipe the icing! ❄️"
                GingerPhase.Candy -> "Tap the house to stick on candy! (${candies.size}/6)"
                GingerPhase.Done -> "Ta-da! A gingerbread house! 🏠"
            }, 18, align = TextAlign.Center,
        )
        Box(
            Modifier.size(400.dp, 370.dp).panel(hex(0xFFE3C2))
                .pointerInput(Unit) { detectDragGestures { c, d -> c.consume(); icing.value(d.getDistance() / density) } }
                .pointerInput(Unit) {
                    detectTapGestures { o ->
                        val x = o.x / density; val y = o.y / density
                        if (phase == GingerPhase.Candy && x in 60f..340f && y in 30f..215f) {
                            candies.add(Triple(x, y, Random.nextInt(4))); Audio.play(Sfx.Pop)
                            if (candies.size >= 6) {
                                phase = GingerPhase.Done
                                game.finishGingerHouse()
                                scope.launch { delay(2200); onExit() }
                            }
                        }
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                // tray line + ghosts of where pieces go
                drawRect(hex(0xE7C49A), Offset(0f, 240 * u), Size(size.width, 4 * u))
                for (p in Piece.entries) if (p !in placed) drawPiece(p, p.target.topLeft * u, ghost = true)
                for (p in Piece.entries) if (p in placed) drawPiece(p, p.target.topLeft * u)
                if (phase >= GingerPhase.Ice) drawIcing(Piece.Roof.target, (iced / ICE_RUB).coerceAtMost(1f))
                for ((x, y, k) in candies) {
                    val c = Offset(x * u, y * u)
                    when (k) {
                        0 -> { drawCircle(Pal.ink, 9 * u, c); drawCircle(hex(0xE5394B), 7 * u, c); drawCircle(Color.White, 2.5f * u, c + Offset(-2 * u, -2 * u)) }
                        1 -> { drawCircle(Pal.ink, 9 * u, c); drawCircle(hex(0x3FAF5A), 7 * u, c); drawCircle(Color.White, 2.5f * u, c + Offset(-2 * u, -2 * u)) }
                        2 -> { drawRoundRect(Pal.ink, c - Offset(10 * u, 5 * u), Size(20 * u, 10 * u), CornerRadius(4 * u)); drawRoundRect(hex(0xFFCC4D), c - Offset(8 * u, 3 * u), Size(16 * u, 6 * u), CornerRadius(3 * u)) }
                        else -> drawPath(starPath(c, 9 * u, 4 * u), hex(0xFF8FB0))
                    }
                }
            }
            // pieces still in the tray can be dragged
            if (phase == GingerPhase.Build) for (p in Piece.entries) if (p !in placed) {
                val at = pos.value[p] ?: Offset.Zero
                val w = p.target.width; val h = p.target.height
                val scale = 0.5f
                Box(
                    Modifier.offset(at.x.dp, at.y.dp).size((w * scale).dp, (h * scale).dp)
                        .pointerInput(p) {
                            detectDragGestures(
                                onDragEnd = {
                                    val cur = pos.value[p] ?: Offset.Zero
                                    if ((cur - p.target.topLeft).getDistance() < 70f) {
                                        placed.add(p); Audio.play(Sfx.Ding)
                                        if (placed.size == 4) { phase = GingerPhase.Ice; game.say("It's standing! Now the icing ❄️") }
                                    } else { pos.value = pos.value + (p to trayStart.getValue(p)); Audio.play(Sfx.Oops) }
                                },
                            ) { c, d -> c.consume(); pos.value = pos.value + (p to ((pos.value[p] ?: Offset.Zero) + d / density)) }
                        },
                ) {
                    Canvas(Modifier.fillMaxSize()) { drawPieceScaled(p, scale) }
                }
            }
            if (phase == GingerPhase.Done) Box(Modifier.align(Alignment.TopCenter).padding(top = 4.dp)) { Bob(8f, 300) { Txt("✨🏠✨", 30) } }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Txt("Uses 3 wheat & ⚡5. It goes in your house!", 13, Pal.brown)
            if (phase != GingerPhase.Done) Btn("✖ Stop", hex(0x8D96A8), 14, Modifier.width(100.dp), onClick = onExit)
        }
    }
}

private fun DrawScope.drawPieceScaled(p: Piece, k: Float) = drawPiece(p, Offset.Zero, ghost = false, k = k)

private fun DrawScope.drawPiece(p: Piece, at: Offset, ghost: Boolean = false, k: Float = 1f) {
    val u = density * k
    val w = p.target.width * u; val h = p.target.height * u
    val dough = if (ghost) Color.Black.copy(alpha = 0.12f) else hex(0xB8763A)
    val edge = if (ghost) Color.Black.copy(alpha = 0.2f) else Pal.ink
    when (p) {
        Piece.Base -> { drawRoundRect(edge, at, Size(w, h), CornerRadius(8 * u)); drawRoundRect(if (ghost) dough else Color.White, at + Offset(3 * u, 3 * u), Size(w - 6 * u, h - 6 * u), CornerRadius(6 * u)) }
        Piece.Walls -> {
            drawRect(edge, at, Size(w, h)); drawRect(dough, at + Offset(3 * u, 3 * u), Size(w - 6 * u, h - 6 * u))
            if (!ghost) for (wx in listOf(0.15f, 0.7f)) { drawRect(Pal.ink, at + Offset(w * wx, h * 0.25f), Size(w * 0.15f, h * 0.3f)); drawRect(hex(0xFFCC4D), at + Offset(w * wx + 3 * u, h * 0.25f + 3 * u), Size(w * 0.15f - 6 * u, h * 0.3f - 6 * u)) }
        }
        Piece.Roof -> {
            val path = Path().apply { moveTo(at.x, at.y + h); lineTo(at.x + w / 2, at.y); lineTo(at.x + w, at.y + h); close() }
            drawPath(path, dough); drawPath(path, edge, style = Stroke(3 * u))
        }
        Piece.Door -> { drawRoundRect(edge, at, Size(w, h), CornerRadius(w / 2, w / 2)); drawRoundRect(if (ghost) dough else hex(0x8A5A3A), at + Offset(3 * u, 3 * u), Size(w - 6 * u, h - 3 * u), CornerRadius(w / 2, w / 2)) }
    }
}

private fun DrawScope.drawIcing(roof: Rect, frac: Float) {
    val u = density
    val l = Offset(roof.left * u, roof.bottom * u); val top = Offset(roof.center.x * u, roof.top * u); val r = Offset(roof.right * u, roof.bottom * u)
    val n = 24
    val shown = (n * frac).toInt()
    for (i in 0 until shown) {
        val t = i / (n - 1f)
        val p = if (t < 0.5f) l + (top - l) * (t * 2) else top + (r - top) * ((t - 0.5f) * 2)
        drawCircle(Color.White, 7 * u, p)
        if (i % 3 == 1) drawRoundRect(Color.White, p + Offset(-2.5f * u, 0f), Size(5 * u, 12 * u), CornerRadius(2.5f * u))
    }
}

// ======================================================================
// MARK: - Snow Park: the Town Tree and a snowball fight
// ======================================================================

private enum class ParkGame(val title: String) { Tree("🎄 Town Tree"), Snowball("❄️ Snowball Fight") }

@Composable
fun ParkScreen(game: GameState) {
    var tab by remember { mutableStateOf(if (game.s.jobs.any { it.kind == JobKind.Tree && !it.finished }) ParkGame.Tree else ParkGame.Snowball) }
    var playing by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!playing) Tabs(ParkGame.entries, tab, { it.title }, Pal.blue) { tab = it }
        when (tab) {
            ParkGame.Tree -> TreeGame(game) { playing = it }
            ParkGame.Snowball -> SnowballGame(game) { playing = it }
        }
    }
}

// MARK: Town Tree

private enum class TreePhase { Look, Hang, Tinsel, Star, Lights, Done }
private val ornamentColors = listOf(hex(0xE5394B), hex(0xFFCC4D), hex(0x3D6FD6), hex(0x9B6BE0), hex(0xFF8FB0), hex(0xF4F8FF))
private const val TREE_W = 400f
private const val TREE_H = 400f
private val treeTop = Offset(200f, 40f)
private fun insideTree(x: Float, y: Float) = y in 60f..330f && abs(x - 200f) < (y - 40f) * 0.55f
private const val TINSEL_RUB = 900f

@Composable
private fun TreeGame(game: GameState, setPlaying: (Boolean) -> Unit) {
    var phase by remember { mutableStateOf(TreePhase.Look) }
    val ornaments = remember { mutableStateListOf<Ornament>().apply { addAll(game.s.townTree) } }
    var dragging by remember { mutableStateOf<Ornament?>(null) }
    var tinsel by remember { mutableFloatStateOf(0f) }
    var star by remember { mutableStateOf<Offset?>(null) }
    var lightsOn by remember { mutableStateOf(game.s.townTree.isNotEmpty()) }
    val scope = rememberCoroutineScope()
    val job = game.s.jobs.firstOrNull { it.kind == JobKind.Tree }

    val drag = rememberUpdatedState { start: Boolean, at: Offset, d: Offset, end: Boolean ->
        when (phase) {
            TreePhase.Hang -> {
                if (start) {
                    // grab an ornament from the tray along the bottom
                    if (at.y > TREE_H - 60) {
                        val slot = ((at.x - 20) / 60).toInt().coerceIn(0, 5)
                        dragging = Ornament(slot % 3, slot, at.x, at.y)
                    }
                }
                dragging?.let { dragging = it.copy(x = it.x + d.x, y = it.y + d.y) }
                if (end) dragging?.let {
                    if (insideTree(it.x, it.y)) {
                        ornaments.add(it.copy(x = it.x / TREE_W, y = it.y / TREE_H)); Audio.play(Sfx.Pop)
                        if (ornaments.size >= 8) { phase = TreePhase.Tinsel; game.say("So pretty! Now rub across the tree for tinsel ✨") }
                    }
                    dragging = null
                }
            }
            TreePhase.Tinsel -> {
                val before = tinsel; tinsel += d.getDistance()
                if ((tinsel / 150).toInt() > (before / 150).toInt()) Audio.play(Sfx.Tap)
                if (tinsel >= TINSEL_RUB) { phase = TreePhase.Star; game.say("Sparkly! Now drag the ⭐ to the very top!") }
            }
            TreePhase.Star -> {
                if (start && star == null && at.y > TREE_H - 70 && at.x > TREE_W - 90) star = at
                star?.let { star = it + d }
                if (end) star?.let {
                    if ((it - treeTop).getDistance() < 50f) { star = treeTop; phase = TreePhase.Lights; Audio.play(Sfx.Ding); game.say("Perfect! Flip the switch for the lights 💡") }
                    else star = null
                }
            }
            else -> {}
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (job != null) JobChip(game, JobKind.Tree)
        Txt(
            when (phase) {
                TreePhase.Look -> if (game.s.townTree.isEmpty()) "The Town Tree is bare! Let's decorate it 🎄" else "The Town Tree! Want to redecorate it?"
                TreePhase.Hang -> "Drag ornaments from the box onto the tree! (${ornaments.size}/8)"
                TreePhase.Tinsel -> "Rub back and forth across the tree for tinsel!"
                TreePhase.Star -> "Drag the ⭐ to the top of the tree!"
                TreePhase.Lights -> "Tap the switch to turn on the lights! 💡"
                TreePhase.Done -> "WOW! The whole town is cheering! 🎉"
            }, 17, Color.White, align = TextAlign.Center,
        )
        val t = game.now
        Box(
            Modifier.size(TREE_W.dp, TREE_H.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { o -> drag.value(true, o / density, Offset.Zero, false) },
                        onDragEnd = { drag.value(false, Offset.Zero, Offset.Zero, true) },
                    ) { c, d -> c.consume(); drag.value(false, c.position / density, d / density, false) }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(Brush.verticalGradient(listOf(hex(0x1D2B53), hex(0x3B4A8A))))
                drawRect(Color.White, Offset(0f, 340 * u), Size(size.width, size.height))
                // the tree: three layers
                for ((i, layer) in listOf(Triple(40f, 150f, 70f), Triple(100f, 240f, 110f), Triple(170f, 335f, 150f)).withIndex()) {
                    val (top, bottom, half) = layer
                    val path = Path().apply { moveTo(200 * u, top * u); lineTo((200 + half) * u, bottom * u); lineTo((200 - half) * u, bottom * u); close() }
                    drawPath(path, listOf(hex(0x3FAF5A), hex(0x349A4E), hex(0x2E7D4A))[i]); drawPath(path, Pal.ink, style = Stroke(3 * u))
                }
                drawRect(Pal.ink, Offset(182 * u, 333 * u), Size(36 * u, 30 * u)); drawRect(hex(0x8A5A3A), Offset(185 * u, 335 * u), Size(30 * u, 26 * u))
                // tinsel swags
                val swags = if (phase >= TreePhase.Star || (phase == TreePhase.Look && game.s.townTree.isNotEmpty())) 4 else (tinsel / TINSEL_RUB * 4).toInt()
                for (i in 0 until swags) {
                    val y = (110 + i * 55f) * u; val half = (40 + i * 30f) * u
                    drawArc(hex(0xFFCC4D), 20f, 140f, false, Offset(200 * u - half, y - 20 * u), Size(half * 2, 40 * u), style = Stroke(5 * u))
                }
                if (phase == TreePhase.Tinsel) for (i in 0 until (tinsel / 40).toInt().coerceAtMost(20)) drawRect(hex(0xFFE27A), Offset(((i * 73) % 220 + 90) * u, ((i * 41) % 250 + 70) * u), Size(4 * u, 4 * u))
                // lights
                if (lightsOn) for (i in 0 until 26) {
                    val y = 70f + (i * 37) % 260; val half = (y - 40f) * 0.5f
                    val x = 200f + ((i * 53) % 100 / 100f * 2 - 1) * half
                    val on = ((t / 300 + i) % 3).toInt() != 0
                    drawCircle(if (on) confetti[i % confetti.size] else Color.Black.copy(alpha = 0.3f), 5 * u, Offset(x * u, y * u))
                    if (on) drawCircle(confetti[i % confetti.size].copy(alpha = 0.3f), 10 * u, Offset(x * u, y * u))
                }
                for (o in ornaments) drawOrnament(o.kind, ornamentColors[o.color % ornamentColors.size], Offset(o.x * TREE_W * u, o.y * TREE_H * u))
                dragging?.let { drawOrnament(it.kind, ornamentColors[it.color], Offset(it.x * u, it.y * u)) }
                // star on top
                val starAt = star ?: if (phase == TreePhase.Look && game.s.townTree.isNotEmpty()) treeTop else null
                starAt?.let { drawPath(starPath(it * u, 26 * u, 11 * u), hex(0xFFCC4D)); drawPath(starPath(it * u, 26 * u, 11 * u), Pal.ink, style = Stroke(3 * u)) }
                // trays
                if (phase == TreePhase.Hang) {
                    drawRect(hex(0x9A5B34), Offset(0f, (TREE_H - 60) * u), Size(size.width, 60 * u))
                    for (s in 0..5) drawOrnament(s % 3, ornamentColors[s], Offset((50 + s * 60f) * u, (TREE_H - 30) * u))
                }
                if (phase == TreePhase.Star && star == null) { drawPath(starPath(Offset(355 * u, 360 * u), 26 * u, 11 * u), hex(0xFFCC4D)); drawPath(starPath(Offset(355 * u, 360 * u), 26 * u, 11 * u), Pal.ink, style = Stroke(3 * u)) }
            }
            if (phase == TreePhase.Lights) Box(Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                Btn("💡 Switch!", Pal.red, 18, Modifier.width(140.dp)) {
                    lightsOn = true; phase = TreePhase.Done
                    game.finishTree(ornaments.toList())
                    scope.launch { delay(2500); phase = TreePhase.Look; setPlaying(false) }
                }
            }
            if (phase == TreePhase.Done) Box(Modifier.align(Alignment.TopCenter).padding(top = 6.dp)) { Bob(8f, 300) { Txt("🎉 Hooray! 🎉", 26, Color.White) } }
        }
        when (phase) {
            TreePhase.Look -> Btn("🎄 Decorate! (⚡5)", Pal.green, 20, Modifier.width(240.dp)) {
                if (game.spend(5)) { ornaments.clear(); tinsel = 0f; star = null; lightsOn = false; phase = TreePhase.Hang; setPlaying(true) }
            }
            TreePhase.Done -> {}
            else -> Btn("✖ Stop", hex(0x8D96A8), 14, Modifier.width(110.dp)) {
                ornaments.clear(); ornaments.addAll(game.s.townTree); star = null; lightsOn = game.s.townTree.isNotEmpty(); phase = TreePhase.Look; setPlaying(false)
            }
        }
    }
}

private fun DrawScope.drawOrnament(kind: Int, color: Color, c: Offset) {
    val u = density
    when (kind) {
        0 -> { // ball
            drawRect(hex(0xC9CED8), c + Offset(-3 * u, -15 * u), Size(6 * u, 5 * u))
            drawCircle(Pal.ink, 12 * u, c); drawCircle(color, 10 * u, c); drawCircle(Color.White.copy(alpha = 0.6f), 3 * u, c + Offset(-4 * u, -4 * u))
        }
        1 -> { // bell
            val p = Path().apply { moveTo(c.x - 11 * u, c.y + 8 * u); cubicTo(c.x - 9 * u, c.y - 14 * u, c.x + 9 * u, c.y - 14 * u, c.x + 11 * u, c.y + 8 * u); close() }
            drawPath(p, color); drawPath(p, Pal.ink, style = Stroke(2 * u)); drawCircle(Pal.ink, 3 * u, c + Offset(0f, 10 * u))
        }
        else -> { // candy cane
            drawArc(Pal.ink, 180f, 180f, false, c + Offset(-8 * u, -16 * u), Size(16 * u, 14 * u), style = Stroke(7 * u))
            drawLine(Pal.ink, c + Offset(8 * u, -9 * u), c + Offset(8 * u, 14 * u), 7 * u)
            drawArc(color, 180f, 180f, false, c + Offset(-8 * u, -16 * u), Size(16 * u, 14 * u), style = Stroke(4 * u))
            drawLine(color, c + Offset(8 * u, -9 * u), c + Offset(8 * u, 13 * u), 4 * u)
            for (k in 0..2) drawLine(Color.White, c + Offset(6 * u, (-4 + k * 6) * u), c + Offset(10 * u, (-6 + k * 6) * u), 2 * u)
        }
    }
}

// MARK: Snowball fight

private const val ROUND_SECONDS = 30f
private val splatLines = listOf("SPLAT! 😂", "Right in the hat! 🎩", "Brrr, cold! 🥶", "No fair, I wasn't ready!", "You got me! ❄️", "Snow in my ears! 😆", "My nose is a snowcone!")

private class Pop(var upAt: Float = 0f, var downAt: Float = 0f, var hitAt: Float = -9f, var line: String = "")
private class Incoming(val x: Float, val y: Float, val born: Float)
private class Throw(val to: Offset, val born: Float, val fort: Int?)

@Composable
private fun SnowballGame(game: GameState, setPlaying: (Boolean) -> Unit) {
    var running by remember { mutableStateOf(false) }
    var over by remember { mutableStateOf(false) }
    var t by remember { mutableFloatStateOf(0f) }
    var hits by remember { mutableIntStateOf(0) }
    var blocked by remember { mutableIntStateOf(0) }
    var splatUntil by remember { mutableFloatStateOf(-1f) }
    val friends = remember { mutableStateOf(npcs.shuffled().take(3)) }
    val pops = remember { List(3) { Pop() } }
    val incoming = remember { mutableStateListOf<Incoming>() }
    val throws = remember { mutableStateListOf<Throw>() }
    var nextIncoming by remember { mutableFloatStateOf(3f) }
    var frame by remember { mutableIntStateOf(0) }
    val fortX = listOf(70f, 203f, 336f)
    val fortY = 250f

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (running) {
            withFrameMillis { now ->
                t = (now - start) / 1000f
                for ((i, p) in pops.withIndex()) if (t > p.downAt + 0.4f + i * 0.3f && Random.nextFloat() < 0.02f) { p.upAt = t; p.downAt = t + 1.1f + Random.nextFloat() * 0.9f - (t / ROUND_SECONDS) * 0.4f }
                if (t > nextIncoming) { incoming.add(Incoming(60f + Random.nextFloat() * 280f, 120f + Random.nextFloat() * 300f, t)); nextIncoming = t + 2.2f + Random.nextFloat() * 1.5f }
                incoming.removeAll { if (t - it.born > 1.5f) { splatUntil = t + 1.0f; Audio.play(Sfx.Oops); true } else false }
                throws.removeAll { th ->
                    if (t - th.born > 0.28f) {
                        val f = th.fort
                        if (f != null && t < pops[f].downAt && t > pops[f].upAt) {
                            hits++; pops[f].hitAt = t; pops[f].line = splatLines.random(); pops[f].downAt = t + 0.6f; Audio.play(Sfx.Pop)
                        }
                        true
                    } else false
                }
                frame++
                if (t >= ROUND_SECONDS) { running = false; over = true; game.snowballDone(hits, friends.value); setPlaying(false) }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tag("❄️ Hits: $hits", Pal.blue, 16)
            Tag("🛡️ Blocked: $blocked", Pal.green, 16)
            if (running) Tag("⏱ ${(ROUND_SECONDS - t).toInt().coerceAtLeast(0)}", Pal.red, 16)
        }
        Box(
            Modifier.size(406.dp, 470.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { o ->
                        if (!running) return@detectTapGestures
                        val x = o.x / density; val y = o.y / density
                        // swat an incoming snowball first
                        val hit = incoming.firstOrNull { (Offset(it.x, it.y) - Offset(x, y)).getDistance() < 30f + (t - it.born) * 30f }
                        if (hit != null) { incoming.remove(hit); blocked++; Audio.play(Sfx.Tap); return@detectTapGestures }
                        val fort = fortX.indices.firstOrNull { abs(fortX[it] - x) < 55f && y in fortY - 110f..fortY + 10f }
                        throws.add(Throw(Offset(x, y), t, fort)); Audio.play(Sfx.Tap)
                    }
                },
        ) {
            frame.let { }   // redraw every frame
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(Brush.verticalGradient(listOf(hex(0xBFE3F7), hex(0xEAF6FF))))
                drawRect(Color.White, Offset(0f, 200 * u), Size(size.width, size.height))
                for (i in 0 until 6) drawCircle(hex(0xDDEBF5), (40 + i * 7) * u, Offset((i * 83f) * u, 205 * u))
            }
            // friends popping up behind forts
            for ((i, f) in friends.value.withIndex()) {
                val p = pops[i]
                val up = running && t > p.upAt && t < p.downAt
                val rise = if (up) ((t - p.upAt) / 0.2f).coerceAtMost(1f) else 0f
                Box(Modifier.offset((fortX[i] - 45).dp, (fortY - 40 - 70 * rise).dp)) { ElfPx(f.look, 90.dp) }
                if (t - p.hitAt < 0.9f) Txt(p.line, 13, align = TextAlign.Center, modifier = Modifier.offset((fortX[i] - 60).dp, (fortY - 150).dp).width(120.dp).panel(Color.White).padding(3.dp))
                if (t - p.hitAt < 0.6f) Txt("💥", 34, modifier = Modifier.offset((fortX[i] - 18).dp, (fortY - 95).dp))
            }
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                for (x in fortX) {
                    drawRoundRect(Pal.ink, Offset((x - 62) * u, (fortY + 10) * u), Size(124 * u, 64 * u), CornerRadius(26 * u))
                    drawRoundRect(Color.White, Offset((x - 59) * u, (fortY + 13) * u), Size(118 * u, 58 * u), CornerRadius(24 * u))
                    drawRoundRect(hex(0xDDEBF5), Offset((x - 59) * u, (fortY + 50) * u), Size(118 * u, 21 * u), CornerRadius(10 * u))
                }
                // your snowballs flying from the bottom
                for (th in throws) {
                    val k = ((t - th.born) / 0.28f).coerceIn(0f, 1f)
                    val from = Offset(203f, 470f)
                    val p = from + (th.to - from) * k - Offset(0f, sin(k * Math.PI.toFloat()) * 60f)
                    drawCircle(Pal.ink, (14 - 6 * k) * u, p * u); drawCircle(Color.White, (12 - 6 * k) * u, p * u)
                }
                // snowballs coming at you: they grow, tap them!
                for (s in incoming) {
                    val k = ((t - s.born) / 1.5f).coerceIn(0f, 1f)
                    val r = (10 + 40 * k) * u
                    drawCircle(Pal.red.copy(alpha = 0.35f), r + 6 * u, Offset(s.x * u, s.y * u))
                    drawCircle(Pal.ink, r, Offset(s.x * u, s.y * u)); drawCircle(Color.White, r - 3 * u, Offset(s.x * u, s.y * u))
                }
                if (t < splatUntil) {
                    val a = ((splatUntil - t) / 1f).coerceIn(0f, 1f)
                    for (i in 0 until 9) drawCircle(Color.White.copy(alpha = 0.92f * a), (50 + (i * 23) % 50) * u, Offset((120 + (i * 61) % 180) * u, (180 + (i * 47) % 160) * u))
                }
            }
            if (t < splatUntil) Txt("SPLAT! Got you! 😆", 24, Pal.blue, modifier = Modifier.align(Alignment.Center))
            if (!running) Column(Modifier.align(Alignment.Center).width(320.dp).panel().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (over) {
                    Txt(if (hits >= 10) "SNOWBALL CHAMPION! 🏆" else if (hits >= 3) "Great throwing! ❄️" else "Snow way! Try again! 😄", 21, Pal.red, align = TextAlign.Center)
                    Txt("You hit friends $hits times and blocked $blocked snowballs.", 15, align = TextAlign.Center)
                } else {
                    Row { for (f in friends.value) ElfPx(f.look, 60.dp) }
                    Txt("${friends.value.joinToString(", ") { it.name }} want a snowball fight!", 16, align = TextAlign.Center)
                    Txt("Tap elves to throw. Tap snowballs flying at you to block them!", 14, Pal.brown, align = TextAlign.Center)
                }
                Btn(if (over) "Play again! (⚡3)" else "Let's go! (⚡3)", Pal.blue, 18, Modifier.width(220.dp)) {
                    if (game.spend(3)) {
                        hits = 0; blocked = 0; t = 0f; nextIncoming = 3f; splatUntil = -1f; incoming.clear(); throws.clear()
                        pops.forEach { it.upAt = 0f; it.downAt = 0f; it.hitAt = -9f }
                        if (over) friends.value = npcs.shuffled().take(3)
                        over = false; running = true; setPlaying(true)
                    }
                }
            }
        }
    }
}

// ======================================================================
// MARK: - Sleigh flight: steer around clouds, drop presents down chimneys
// ======================================================================

private const val HOUSE_GAP = 230f
private const val SLEIGH_X = 40f
private const val GRAVITY = 520f

private class Falling(var x: Float, var y: Float, var vy: Float)   // x in world units
private class Cloud(val x: Float, val y: Float, val w: Float, var bonked: Boolean = false)

@Composable
fun DeliveryScreen(game: GameState) {
    val christmas = game.christmasEveRide
    val flight = remember { flightFor(game.level, christmas) }
    // Fixed when the ride starts (job.done goes up as you deliver, so don't recompute it).
    val target = remember { if (christmas) 8 else (game.s.jobs.firstOrNull { it.kind == JobKind.Deliver }?.let { it.goal - it.done } ?: 0) }
    val team = remember { game.sleighTeam.sortedBy { if (it == "Rudolph") 0 else 1 }.reversed() }
    val power = game.teamPower
    val seed = remember { Random.nextInt(1000) }

    var elapsed by remember { mutableFloatStateOf(0f) }
    var world by remember { mutableFloatStateOf(0f) }
    var sleighY by remember { mutableFloatStateOf(200f) }
    var aimY by remember { mutableFloatStateOf(200f) }
    var bonkUntil by remember { mutableFloatStateOf(-1f) }
    var delivered by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Drag up & down to steer. Tap to drop a present!") }
    val visited = remember { mutableStateOf(setOf<Int>()) }
    val falling = remember { mutableStateListOf<Falling>() }
    val clouds = remember { mutableStateListOf<Cloud>() }
    var nextCloud by remember { mutableFloatStateOf(500f) }
    val finished = delivered >= target

    fun nice(i: Int) = Random(i * 7919 + seed).nextFloat() >= flight.naughtyChance || i < 1

    LaunchedEffect(finished) {
        if (finished) { message = "All presents delivered! 🎉"; Audio.play(Sfx.LevelUp); delay(2000); game.endDelivery(true) }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(hex(0x0E1633), hex(0x2B3A75))))) {
        val h = maxHeight.value
        val w = maxWidth.value
        val roofY = h - 230f          // top of the houses (their chimneys)
        val minY = 150f; val maxY = roofY - 120f

        LaunchedEffect(Unit) {
            var last = withFrameMillis { it }
            while (true) {
                withFrameMillis { now ->
                    val dt = ((now - last) / 1000f).coerceAtMost(0.05f); last = now
                    elapsed += dt
                    val bonked = elapsed < bonkUntil
                    world += dt * flight.speed * (if (bonked) 0.4f else 1f)
                    val follow = 3f + power * 1.2f
                    sleighY += (aimY - sleighY) * (dt * follow).coerceAtMost(1f)
                    // new clouds ahead
                    if (world + w > nextCloud) {
                        clouds.add(Cloud(nextCloud + 40f, minY - 20 + Random.nextFloat() * (maxY - minY + 40), 90f + Random.nextFloat() * 60f))
                        nextCloud += flight.cloudEvery * (0.7f + Random.nextFloat() * 0.6f)
                    }
                    clouds.removeAll { it.x - world < -300 }
                    // bonk!
                    for (c in clouds) if (!c.bonked) {
                        val cx = c.x - world
                        if (cx < SLEIGH_X + 170 && cx + c.w > SLEIGH_X + 10 && abs((c.y + 20) - (sleighY + 30)) < 42) {
                            c.bonked = true
                            bonkUntil = elapsed + (0.9f - power * 0.15f)
                            message = if (power >= TEAM_SIZE) "Whoosh! Your happy team zooms through the cloud! 🦌✨" else listOf("Bonk! ☁️ Brrr, cloudy!", "Ack! A cloud in Santa's beard! ☁️", "Pfft! Cloud up the reindeer's nose! 🤧").random()
                            Audio.play(Sfx.Oops)
                        }
                    }
                    // presents falling
                    falling.removeAll { f ->
                        f.vy += GRAVITY * dt; f.y += f.vy * dt; f.x += flight.speed * 0.45f * dt
                        val i = floor((f.x - 300f) / HOUSE_GAP).toInt()
                        val houseX = 300f + i * HOUSE_GAP
                        val chimney = houseX + 103f
                        if (f.y >= roofY + 4 && f.y < roofY + 30 && abs(f.x - chimney) < 26f && i !in visited.value) {
                            visited.value = visited.value + i
                            if (nice(i)) {
                                if (game.deliver()) { delivered++; message = listOf("Down the chimney! 🎁", "Bullseye! Ho ho ho! 🎅", "A happy kid tomorrow! 😊", "Perfect drop! ✨").random() }
                            } else { message = "Oops, a naughty house! They get coal instead 🪨"; Audio.play(Sfx.Oops) }
                            true
                        } else if (f.y > h) { message = listOf("Plop! A snowman got a present 😂", "Missed! A very lucky squirrel 🐿️", "Into the snow! Aim for the chimney 🎯").random(); true }
                        else false
                    }
                }
            }
        }

        val canDrop = rememberUpdatedState(!finished && falling.isEmpty())
        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) { detectDragGestures { c, d -> c.consume(); aimY = (aimY + d.y / density).coerceIn(minY, maxY) } }
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (!canDrop.value) return@detectTapGestures
                        if (!christmas && game.presents == 0) { game.say("No presents left! Make toys in the Workshop 🧸"); Audio.play(Sfx.Oops); return@detectTapGestures }
                        falling.add(Falling(world + SLEIGH_X + 40f, sleighY + 40f, 0f)); Audio.play(Sfx.Tap)
                    }
                },
        ) {
            val rd = remember(team) { team.associateWith { Sprites.reindeer(it, game.decosOn(it)) } }
            val sleigh = Sprites.named("sleigh"); val santa = Sprites.named("santa")
            val shake = if (elapsed < bonkUntil) sin(elapsed * 60f) * 4f else 0f
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawCircle(hex(0xFFF3B0), 36 * u, Offset(size.width * 0.82f, 150 * u))
                for (i in 0 until 40) drawRect(Color.White, Offset(((i * 131) % 1000) / 1000f * size.width, ((i * 71) % 500) / 1000f * size.height), Size(3 * u, 3 * u))
                // houses
                val first = floor((world - 300f) / HOUSE_GAP).toInt() - 1
                for (i in first..first + 4) {
                    val x = 300f + i * HOUSE_GAP - world
                    drawHouse(Offset(x * u, roofY * u), nice(i), i in visited.value, i)
                }
                drawRect(Color.White, Offset(0f, (roofY + 160) * u), Size(size.width, size.height))
                // clouds
                for (c in clouds) {
                    val cx = (c.x - world) * u; val cy = c.y * u
                    val col = if (c.bonked) hex(0xC9CED8) else Color.White
                    drawCircle(col.copy(alpha = 0.9f), 26 * u, Offset(cx + c.w * 0.25f * u, cy + 26 * u))
                    drawCircle(col.copy(alpha = 0.9f), 34 * u, Offset(cx + c.w * 0.5f * u, cy + 16 * u))
                    drawCircle(col.copy(alpha = 0.9f), 24 * u, Offset(cx + c.w * 0.78f * u, cy + 28 * u))
                    drawRoundRect(col.copy(alpha = 0.9f), Offset(cx, cy + 26 * u), Size(c.w * u, 26 * u), CornerRadius(13 * u))
                }
                // presents in the air
                for (f in falling) {
                    val p = Offset((f.x - world) * u, f.y * u)
                    drawRect(Pal.ink, p - Offset(11 * u, 11 * u), Size(22 * u, 22 * u)); drawRect(Pal.red, p - Offset(9 * u, 9 * u), Size(18 * u, 18 * u))
                    drawRect(hex(0xFFCC4D), p - Offset(2 * u, 9 * u), Size(4 * u, 18 * u))
                }
                // sleigh, Santa and the reindeer team (flipped to face right)
                val sy = (sleighY + shake) * u
                sprite(santa, Offset((SLEIGH_X + 14) * u, sy - 26 * u), 52 * u)
                sprite(sleigh, Offset(SLEIGH_X * u, sy + 6 * u), 46 * u)
                team.forEachIndexed { k, name ->
                    val rx = (SLEIGH_X + 104 + k * 40) * u; val ry = sy - 6 * u + sin(elapsed * 6 + k) * 3 * u
                    drawLine(hex(0x8A5A3A), Offset((SLEIGH_X + 96) * u, sy + 20 * u), Offset(rx + 20 * u, ry + 28 * u), 2 * u)
                    scale(-1f, 1f, Offset(rx + 20 * u, ry)) { sprite(rd.getValue(name), Offset(rx, ry), 36 * u) }
                }
                // Rudolph lights the way on foggy nights
                if (flight.foggy) {
                    val lead = team.lastOrNull()
                    val nose = Offset((SLEIGH_X + 104 + (team.size - 1) * 40 + 38) * u, sy + 9 * u)
                    val glow = if (lead == "Rudolph" || "Rudolph" in game.reindeer) 1f else 0.6f
                    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Transparent, hex(0x0E1633).copy(alpha = 0.82f)), nose, 260 * u * glow), Offset.Zero, size)
                    if ("Rudolph" in team) drawCircle(Brush.radialGradient(listOf(hex(0xFF4050).copy(alpha = 0.55f), Color.Transparent), nose, 40 * u), 40 * u, nose)
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt(if (christmas) "🌟 Christmas Eve 🌟" else "🛷 Present Delivery", 26, Color.White)
            Txt("Delivered $delivered / $target", 22, hex(0xFFCC4D))
            Txt(message, 17, Color.White, align = TextAlign.Center, modifier = Modifier.padding(horizontal = 12.dp))
            if (flight.foggy) Txt(if ("Rudolph" in team) "🔴 Rudolph's nose lights the way!" else "🌫️ Foggy night! Hitch Rudolph to light the way", 14, hex(0xFF8FB0))
        }
        if (!christmas) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                Btn("🏠 Fly home", Pal.blue, 18, Modifier.width(180.dp)) { game.endDelivery(false) }
            }
        }
    }
}

/** A rooftop with a chimney at (topLeft.x + 103, topLeft.y + 4). 140 x 160 units. */
private fun DrawScope.drawHouse(at: Offset, nice: Boolean, done: Boolean, seed: Int) {
    val u = density
    val wall = listOf(hex(0xE8C7A0), hex(0xC9E0F2), hex(0xF2C9D6), hex(0xD8EBC6))[((seed % 4) + 4) % 4]
    fun r(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, at + Offset(x * u, y * u), Size(w * u, h * u))
    r(Pal.ink, 89f, 5f, 28f, 40f); r(hex(0x6B4A3A), 92f, 8f, 22f, 34f)
    r(Color.White, 88f, 4f, 30f, 7f)
    if (!done) { r(Pal.ink, 95f, 10f, 16f, 4f) }   // the dark chimney hole
    drawPath(Path().apply { moveTo(at.x, at.y + 60 * u); lineTo(at.x + 70 * u, at.y + 14 * u); lineTo(at.x + 140 * u, at.y + 60 * u); close() }, if (nice) hex(0xA02338) else hex(0x4A4458))
    r(Color.White, 8f, 54f, 124f, 7f)
    r(Pal.ink, 12f, 60f, 116f, 100f)
    r(wall, 15f, 63f, 110f, 97f)
    val lit = if (done && nice) hex(0xFFE27A) else if (nice) hex(0xFFCC4D) else hex(0x3A3550)
    r(Pal.ink, 26f, 80f, 30f, 30f); r(lit, 29f, 83f, 24f, 24f)
    r(Pal.ink, 84f, 80f, 30f, 30f); r(lit, 87f, 83f, 24f, 24f)
    r(hex(0x6B4A3A), 57f, 118f, 26f, 42f)
    if (done) {
        val c = at + Offset(103 * u, -14 * u)
        if (nice) { drawRect(Pal.red, c - Offset(9 * u, 9 * u), Size(18 * u, 18 * u)); drawRect(hex(0xFFCC4D), c - Offset(2 * u, 9 * u), Size(4 * u, 18 * u)) }
        else drawCircle(Pal.ink, 9 * u, c)
    }
}
