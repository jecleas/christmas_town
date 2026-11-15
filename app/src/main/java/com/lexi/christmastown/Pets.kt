package com.lexi.christmastown

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

// ======================================================================
// MARK: - Pets: a little pet shop and hands-on pet care
// ======================================================================

private enum class PetTab(val title: String) { Care("🐾 My Pets"), Shop("🏪 Pet Shop") }
private enum class PetTool(val label: String) { Feed("🍽️ Feed"), Wash("🛁 Wash"), Cuddle("🤗 Cuddle") }

@Composable
fun PetsScreen(game: GameState) {
    var tab by remember { mutableStateOf(if (game.s.pets.isEmpty()) PetTab.Shop else PetTab.Care) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Tabs(PetTab.entries, tab, { it.title }, hex(0xE0843A)) { tab = it }
        when (tab) {
            PetTab.Shop -> PetShop(game) { tab = PetTab.Care }
            PetTab.Care -> if (game.s.pets.isEmpty()) {
                Bubble("You don't have a pet yet! Visit the 🏪 Pet Shop. Pets live in your house and LOVE you.", 320.dp)
            } else PetCare(game)
        }
    }
}

@Composable
private fun PetShop(game: GameState, onBought: () -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt("Every pet costs in-game money from working for Santa. 💰", 14, Pal.brown, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        for (row in PetKind.entries.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (k in row) Column(
                Modifier.width(198.dp).panel().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Bob(4f, 500 + k.ordinal * 90) { Px(k.sprite, 80.dp) }
                Txt(k.title, 17)
                Txt("“${k.noise}”", 13, Pal.brown)
                val owned = game.s.pets.count { it.kind == k }
                if (owned > 0) Txt("You have $owned 💖", 12, Pal.darkGreen)
                Btn("Adopt ${money(k.price)}", hex(0xE0843A), 15, Modifier.width(170.dp)) {
                    val before = game.s.pets.size
                    game.buyPet(k)
                    if (game.s.pets.size > before) onBought()
                }
            }
        }
    }
}

private const val PET_RUB = 380f
private const val PET_BUBBLES = 12

@Composable
private fun PetCare(game: GameState) {
    val pets = game.s.pets
    var selected by remember { mutableStateOf(pets.first().id) }
    val pet = pets.firstOrNull { it.id == selected } ?: pets.first()
    var tool by remember(pet.id) { mutableStateOf(PetTool.Feed) }
    var food by remember(pet.id) { mutableStateOf<Offset?>(null) }    // the snack you're dragging
    var bites by remember(pet.id) { mutableIntStateOf(0) }
    var rub by remember(pet.id) { mutableFloatStateOf(0f) }
    var rinsing by remember(pet.id) { mutableStateOf(false) }
    val bubbles = remember(pet.id) { mutableStateListOf<Offset>() }
    val hearts = remember(pet.id) { mutableStateListOf<Offset>() }
    var bubble by remember(pet.id) { mutableStateOf<String?>(null) }
    var jump by remember { mutableIntStateOf(0) }
    val now = game.now
    val petCenter = Offset(203f, 170f)
    val foodHome = Offset(330f, 240f)

    LaunchedEffect(bubble) { if (bubble != null) { delay(2200); bubble = null } }

    val drag = rememberUpdatedState { at: Offset, d: Offset, end: Boolean ->
        when (tool) {
            PetTool.Feed -> {
                if (food != null) food = food!! + d
                if (end) food?.let {
                    if ((it - petCenter).getDistance() < 80f) {
                        bites++; Audio.play(Sfx.Munch); jump++
                        if (bites >= 3) { bites = 0; game.feedPet(pet.id); bubble = "${pet.kind.noise} Yum yum! ${pet.kind.food}" }
                    }
                    food = null
                }
            }
            PetTool.Wash -> if (!rinsing) {
                if (bubbles.isEmpty() || (bubbles.last() - at).getDistance() > 24f) {
                    bubbles.add(at); if (bubbles.size % 3 == 0) Audio.play(Sfx.Pop)
                    if (bubbles.size >= PET_BUBBLES) { rinsing = true; bubble = "Bubbly! Now rinse 🚿" }
                }
            } else {
                val before = bubbles.size
                bubbles.removeAll { (it - at).getDistance() < 44f }
                if (bubbles.size < before) Audio.play(Sfx.Tap)
                if (bubbles.isEmpty()) { rinsing = false; game.washPet(pet.id); bubble = "*shakes off* Fresh and fluffy! ✨"; jump++ }
            }
            PetTool.Cuddle -> {
                val before = rub; rub += d.getDistance()
                if ((rub / 70).toInt() > (before / 70).toInt()) { hearts.add(at); if (hearts.size > 8) hearts.removeAt(0); Audio.play(Sfx.Tap) }
                if (rub >= PET_RUB) { rub = 0f; game.cuddlePet(pet.id); bubble = pet.kind.jokes.random(); jump++ }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (p in pets) Column(
                Modifier.width(76.dp).squish { selected = p.id }.panel(if (p.id == pet.id) hex(0xFFF1B8) else Color.White, if (p.id == pet.id) Pal.red else Pal.ink).padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { Px(p.kind.sprite, 36.dp); Txt(p.name, 12, maxLines = 1) }
        }

        val jumpAnim = remember { androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(jump) { if (jump > 0) { jumpAnim.snapTo(1f); jumpAnim.animateTo(0f, tween(450)) } }

        Box(
            Modifier.size(406.dp, 300.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))
                .pointerInput(pet.id) {
                    detectDragGestures(
                        onDragStart = { o ->
                            val at = o / density
                            if (tool == PetTool.Feed && (at - foodHome).getDistance() < 50f) food = foodHome
                        },
                        onDragEnd = { drag.value(Offset.Zero, Offset.Zero, true) },
                    ) { c, d -> c.consume(); drag.value(c.position / density, d / density, false) }
                }
                .pointerInput(pet.id) { detectTapGestures { bubble = pet.kind.jokes.random(); Audio.play(Sfx.Pop); jump++ } },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(hex(0xFBE7C6))
                var x = 0f
                while (x < size.width) { drawRect(hex(0xF4D9AE), Offset(x, 0f), Size(14 * u, size.height * 0.7f)); x += 40 * u }
                drawRect(hex(0xC98B5E), Offset(0f, size.height * 0.7f), Size(size.width, size.height))
                if (tool == PetTool.Wash) {
                    drawRoundRect(Pal.ink, Offset(90 * u, 190 * u), Size(226 * u, 80 * u), androidx.compose.ui.geometry.CornerRadius(30 * u))
                    drawRoundRect(hex(0xEAF6FF), Offset(94 * u, 194 * u), Size(218 * u, 72 * u), androidx.compose.ui.geometry.CornerRadius(28 * u))
                    drawRect(hex(0x9BD3FF), Offset(100 * u, 200 * u), Size(206 * u, 18 * u))
                }
            }
            // the pet
            val t = rememberInfiniteTransition(label = "pet")
            val breathe by t.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "breathe")
            Box(
                Modifier.align(Alignment.TopStart).offset((petCenter.x - 80).dp, (petCenter.y - 80).dp)
                    .graphicsLayer { translationY = -jumpAnim.value * 40 * density; scaleY = 1f + breathe * 0.04f; scaleX = 1f - breathe * 0.02f },
            ) { Px(pet.kind.sprite, 160.dp) }
            for (b in bubbles) Box(Modifier.offset((b.x - 15).dp, (b.y - 15).dp).size(30.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(15.dp)).border(2.dp, hex(0x9BD3FF), RoundedCornerShape(15.dp)))
            for ((i, h) in hearts.withIndex()) Txt("💖", 18 + i, modifier = Modifier.offset((h.x - 10).dp, (h.y - 30 - i * 3).dp))
            if (tool == PetTool.Feed) {
                val at = food ?: foodHome
                Box(Modifier.offset((at.x - 30).dp, (at.y - 30).dp).size(60.dp).background(Color.White, RoundedCornerShape(30.dp)).border(3.dp, Pal.ink, RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                    Txt(pet.kind.food, 30)
                }
            }
            bubble?.let { Txt(it, 15, align = TextAlign.Center, modifier = Modifier.align(Alignment.TopCenter).padding(8.dp).panel(Color.White).padding(horizontal = 10.dp, vertical = 6.dp)) }
        }

        // meters
        Row(Modifier.fillMaxWidth().panel().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Meter("🍽️ Full", pet.fullness(now), hex(0xE0843A), Modifier.weight(1f))
            Meter("🛁 Clean", pet.cleanness(now), hex(0x3D6FD6), Modifier.weight(1f))
            Meter("😊 Happy", pet.happiness(now), Pal.pink, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (tl in PetTool.entries) Txt(
                tl.label, 15, if (tool == tl) Color.White else Pal.ink, align = TextAlign.Center,
                modifier = Modifier.weight(1f).squish { tool = tl; bubbles.clear(); rinsing = false; Audio.play(Sfx.Tap) }.panel(if (tool == tl) hex(0xE0843A) else Color.White).padding(vertical = 8.dp),
            )
        }
        Txt(
            when (tool) {
                PetTool.Feed -> "Drag the ${pet.kind.food} to ${pet.name}! (${bites}/3 bites)"
                PetTool.Wash -> if (rinsing) "Drag over the bubbles to rinse them off! 🚿" else "Rub ${pet.name} to make bubbles! (${bubbles.size}/$PET_BUBBLES)"
                PetTool.Cuddle -> "Rub ${pet.name} for cuddles! Tap for a silly surprise."
            }, 15, align = TextAlign.Center,
        )
        Txt("Keep every meter high and ${pet.name} finds you a little gift each day! 🎁", 13, Pal.brown, align = TextAlign.Center)
    }
}

@Composable
private fun Meter(label: String, frac: Float, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(label, 13)
        Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(4.dp)).background(hex(0xD5DBE6))) {
            Box(Modifier.fillMaxWidth(frac.coerceIn(0.02f, 1f)).height(12.dp).background(if (frac < 0.3f) Pal.red else color))
        }
    }
}

// MARK: - Pets wandering around your living room

private val reactions = listOf("*boing!*", "Hehe!", "*sneeze*", "Hi hi!", "*spins*", "Snack?", "*happy wiggle*")

/** One pet strolling back and forth across the floor. Tap it for a funny reaction. */
@Composable
fun WanderingPet(pet: Pet, roomW: Float, floorY: Float, index: Int, now: Long) {
    val t = rememberInfiniteTransition(label = "walk${pet.id}")
    val period = 9000 + index * 2300
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(period, easing = LinearEasing)), label = "walk")
    val hop by t.animateFloat(0f, 1f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "hop")
    var said by remember { mutableStateOf<String?>(null) }
    var spin by remember { mutableIntStateOf(0) }
    LaunchedEffect(said) { if (said != null) { delay(1800); said = null } }

    // walk, pause, walk back, pause
    val p = (phase + index * 0.37f) % 1f
    val walkFrac = when {
        p < 0.4f -> p / 0.4f
        p < 0.5f -> 1f
        p < 0.9f -> 1f - (p - 0.5f) / 0.4f
        else -> 0f
    }
    val walking = p < 0.4f || (p in 0.5f..0.9f)
    val left = 30f + index * 40f
    val x = left + walkFrac * (roomW - 150f - left)
    val hungry = pet.fullness(now) < 0.3f
    val dirty = pet.cleanness(now) < 0.3f
    Box(Modifier.offset(x.dp, (floorY - 48 + index * 6).dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val line = said ?: if (hungry) "${pet.kind.food}?" else if (dirty) "🦨" else null
            if (line != null) Txt(line, 11, modifier = Modifier.panel(Color.White, radius = 6.dp).padding(horizontal = 4.dp, vertical = 1.dp))
            Box(
                Modifier.graphicsLayer {
                    translationY = if (walking) -abs(sin(hop * 3.14f)) * 5 * density else 0f
                    rotationZ = if (walking) (hop - 0.5f) * 10f else 0f
                    rotationY = spin * 360f
                    scaleX = if (p >= 0.5f && p < 0.9f) -1f else 1f
                }.squish { said = reactions.random() + " " + pet.kind.noise; spin++; Audio.play(Sfx.Pop) },
            ) { Px(pet.kind.sprite, 46.dp) }
        }
    }
}
