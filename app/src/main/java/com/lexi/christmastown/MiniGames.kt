package com.lexi.christmastown

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ======================================================================
// MARK: - Bakery: mix the dough, bake it just right, then decorate it
// ======================================================================

private enum class BakePhase { Pick, Mix, Bake, Decorate, Done }
private enum class Topping(val label: String) { Sprinkles("✨ Sprinkles"), Candy("🍬 Candy"), Star("⭐ Stars"), Heart("💖 Hearts") }
private data class Top(val kind: Topping, val x: Float, val y: Float, val seed: Int)

private const val STIRS = 12
private val icings = listOf(hex(0xFFFFFF), hex(0xFF9DB8), hex(0xA6E06A), hex(0x9BD3FF), hex(0x8A5A3A))

@Composable
fun BakeryScreen(game: GameState) {
    var phase by remember { mutableStateOf(BakePhase.Pick) }
    var cookie by remember { mutableStateOf(Cookie.Sugar) }
    var stirs by remember { mutableIntStateOf(0) }
    var perfect by remember { mutableStateOf(false) }
    var icing by remember { mutableStateOf<Color?>(null) }
    var tool by remember { mutableStateOf(Topping.Sprinkles) }
    val tops = remember { mutableStateListOf<Top>() }
    val scope = rememberCoroutineScope()
    var house by remember { mutableStateOf(false) }

    if (house) { GingerbreadGame(game) { house = false }; return }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WorkmateBar(game, Place.Bakery)
        JobChip(game, JobKind.Bake)
        when (phase) {
            BakePhase.Pick -> Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RecipeList(game) { c ->
                    if (!game.canBake(c)) { game.say("Not enough ingredients! Grow some in the Greenhouse 🌱"); Audio.play(Sfx.Oops) }
                    else if (game.hasEnergy(5)) { cookie = c; stirs = 0; tops.clear(); icing = null; perfect = false; phase = BakePhase.Mix }
                }
                Btn(hex(0x8A5A3A), Modifier.fillMaxWidth(), onClick = {
                    if (!game.canBuildHouse()) { game.say("A gingerbread house needs 3 wheat! Grow some in the Greenhouse 🌾"); Audio.play(Sfx.Oops) }
                    else if (game.hasEnergy(5)) house = true
                }) { Px("ginghouse", 32.dp); Txt("Build a Gingerbread House (3 wheat, ⚡5)", 15, Color.White) }
            }
            BakePhase.Mix -> MixBowl(stirs) {
                stirs++
                Audio.play(Sfx.Pop)
                if (stirs >= STIRS) phase = BakePhase.Bake
            }
            BakePhase.Bake -> BakeTiming(cookie) { gotIt ->
                perfect = gotIt
                Audio.play(if (gotIt) Sfx.Ding else Sfx.Pop)
                game.say(if (gotIt) "⭐ PERFECT bake! Golden and yummy!" else "A little ${if (Random.nextBoolean()) "pale" else "toasty"}, but still yummy! 😋")
                phase = BakePhase.Decorate
            }
            BakePhase.Decorate -> {
                Txt("Decorate your ${cookie.title}!", 22)
                CookieCanvas(icing, tops, perfect, Modifier.size(250.dp).pointerInput(Unit) {
                    detectTapGestures { o ->
                        val x = o.x / size.width; val y = o.y / size.height
                        if ((x - 0.5f) * (x - 0.5f) + (y - 0.5f) * (y - 0.5f) < 0.42f * 0.42f) {
                            tops.add(Top(tool, x, y, Random.nextInt()))
                            Audio.play(Sfx.Pop)
                        }
                    }
                })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Txt("Icing:", 16)
                    for (c in icings) Box(
                        Modifier.size(36.dp).squish { icing = c; Audio.play(Sfx.Tap) }
                            .border(3.dp, if (icing == c) Pal.red else Color.Transparent, CircleShape).padding(4.dp)
                            .background(c, CircleShape).border(2.dp, Pal.ink, CircleShape)
                    )
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in Topping.entries) Txt(
                        t.label, 15, if (tool == t) Color.White else Pal.ink,
                        modifier = Modifier.squish { tool = t }.panel(if (tool == t) Pal.red else Color.White).padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
                Txt(if (tops.size < 3) "Tap the cookie to add toppings! (${tops.size}/3)" else "Looks delicious! Add more or tap Done.", 15, align = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Btn("↺ Clear", hex(0x8D96A8), 16, Modifier.width(110.dp)) { tops.clear(); icing = null }
                    Btn("✅ Done!", Pal.green, 18, Modifier.width(160.dp), enabled = tops.size >= 3) {
                        if (game.bake(cookie)) {
                            phase = BakePhase.Done
                            scope.launch { delay(1800); phase = BakePhase.Pick }
                        } else phase = BakePhase.Pick
                    }
                }
            }
            BakePhase.Done -> {
                Txt("Ta-da! 🎉", 30, Pal.red)
                Bob(6f, 300) { CookieCanvas(icing, tops, perfect, Modifier.size(220.dp)) }
                Txt(if (perfect) "⭐ A perfect ${cookie.title}!" else "A yummy ${cookie.title}!", 20)
            }
        }
    }
}

@Composable
private fun RecipeList(game: GameState, onBake: (Cookie) -> Unit) {
    val s = game.s
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Bob { ElfPx(s.look, 120.dp) }
        Bubble("Pick a cookie to make! You'll mix it, bake it and decorate it. 🍪", 230.dp)
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
            else Btn("Make!", Pal.red, 16, Modifier.width(90.dp)) { onBake(c) }
        }
    }
}

@Composable
private fun MixBowl(stirs: Int, onStir: () -> Unit) {
    val frac = stirs / STIRS.toFloat()
    val angle by animateFloatAsState(stirs * 50f, label = "stir")
    Txt("Tap the bowl to stir! 🥄", 22)
    Box(Modifier.size(300.dp, 240.dp).squish(onClick = onStir)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            val c = Offset(size.width / 2, size.height * 0.62f)
            // spoon behind the batter
            rotate(-20f + angle / 3, Offset(c.x, c.y - 20 * u)) {
                drawRoundRect(hex(0xC98B5E), Offset(c.x - 7 * u, c.y - 150 * u), Size(14 * u, 130 * u), CornerRadius(7 * u))
                drawOval(hex(0xC98B5E), Offset(c.x - 16 * u, c.y - 40 * u), Size(32 * u, 40 * u))
            }
            drawOval(Pal.ink, Offset(c.x - 134 * u, c.y - 64 * u), Size(268 * u, 150 * u))
            drawOval(Brush.verticalGradient(listOf(hex(0xD5DBE6), hex(0x8D96A8)), startY = c.y - 60 * u, endY = c.y + 86 * u), Offset(c.x - 130 * u, c.y - 60 * u), Size(260 * u, 142 * u))
            val batter = lerp(hex(0xF6EEDC), hex(0xE3A35A), frac)
            drawOval(batter, Offset(c.x - 112 * u, c.y - 52 * u), Size(224 * u, 70 * u))
            rotate(angle, Offset(c.x, c.y - 17 * u)) {
                drawArc(lerp(batter, hex(0x8A5A3A), 0.35f), 0f, 260f, false, Offset(c.x - 60 * u, c.y - 35 * u), Size(120 * u, 36 * u), style = Stroke(4 * u))
                drawArc(lerp(batter, hex(0x8A5A3A), 0.35f), 180f, 220f, false, Offset(c.x - 30 * u, c.y - 26 * u), Size(60 * u, 18 * u), style = Stroke(3 * u))
            }
        }
    }
    Box(Modifier.size(260.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White).border(2.dp, Pal.ink, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxHeight().width(260.dp * frac).background(hex(0xE3A35A)))
    }
    Txt("$stirs / $STIRS stirs", 16)
}

@Composable
private fun BakeTiming(cookie: Cookie, onDone: (Boolean) -> Unit) {
    val t = rememberInfiniteTransition(label = "heat")
    val heat by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse), label = "heat")
    Txt("Take it out when the bar is in the ⭐ golden zone!", 18, align = TextAlign.Center)
    Box(Modifier.size(200.dp, 190.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRect(Pal.ink, Offset(0f, 0f), Size(200 * u, 190 * u))
            drawRect(hex(0xC9CED8), Offset(4 * u, 4 * u), Size(192 * u, 182 * u))
            drawRect(Pal.ink, Offset(22 * u, 52 * u), Size(156 * u, 110 * u))
            drawRect(lerp(hex(0xFF9A3C), hex(0xFF5A2A), heat), Offset(26 * u, 56 * u), Size(148 * u, 102 * u))
            for (i in 0..3) drawRect(Pal.red, Offset((30 + i * 40) * u, 16 * u), Size(18 * u, 18 * u))
        }
        Bob(3f, 300) { Px(cookie.sprite, 64.dp, alpha = 1f) }
    }
    Box(Modifier.size(320.dp, 30.dp).clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(listOf(hex(0xF6EEDC), hex(0xE3A35A), hex(0x6B3A1E)))).border(3.dp, Pal.ink, RoundedCornerShape(6.dp))) {
        Box(Modifier.offset(x = 320.dp * 0.55f).width(320.dp * 0.25f).fillMaxHeight().background(hex(0xFFCC4D).copy(alpha = 0.75f)))
        Txt("⭐", 16, modifier = Modifier.offset(x = 320.dp * 0.64f, y = 3.dp))
        Box(Modifier.offset(x = 320.dp * heat - 3.dp).width(6.dp).fillMaxHeight().background(Pal.ink))
    }
    Btn("🧤 Take it out!", Pal.red, 22, Modifier.width(240.dp)) { onDone(heat in 0.55f..0.8f) }
}

/** A plain cookie with your icing and toppings drawn on top. */
@Composable
private fun CookieCanvas(icing: Color?, tops: List<Top>, perfect: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        val r = size.minDimension * 0.46f
        val c = Offset(size.width / 2, size.height / 2)
        drawCircle(Pal.ink, r + 4.dp.toPx(), c)
        drawCircle(Brush.radialGradient(listOf(if (perfect) hex(0xEBB86A) else hex(0xDDA45E), if (perfect) hex(0xC98B4A) else hex(0xB8763A)), c, r), r, c)
        val rnd = Random(7)
        repeat(14) { drawCircle(hex(0xA9693A).copy(alpha = 0.5f), 3.dp.toPx(), c + Offset((rnd.nextFloat() - 0.5f) * r * 1.5f, (rnd.nextFloat() - 0.5f) * r * 1.5f)) }
        if (icing != null) {
            drawCircle(icing, r * 0.84f, c)
            drawCircle(Color.White.copy(alpha = 0.35f), r * 0.2f, c + Offset(-r * 0.35f, -r * 0.35f))
        }
        for (t in tops) drawTopping(t)
    }
}

private val sprinkleColors = listOf(hex(0xE5394B), hex(0x3FAF5A), hex(0x3D6FD6), hex(0xFFCC4D), hex(0xFF8FB0), hex(0x9B6BE0))

private fun DrawScope.drawTopping(t: Top) {
    val p = Offset(t.x * size.width, t.y * size.height)
    val rnd = Random(t.seed)
    val u = density
    when (t.kind) {
        Topping.Sprinkles -> repeat(7) {
            val o = p + Offset((rnd.nextFloat() - 0.5f) * 30 * u, (rnd.nextFloat() - 0.5f) * 30 * u)
            rotate(rnd.nextFloat() * 180f, o) { drawRoundRect(sprinkleColors[rnd.nextInt(sprinkleColors.size)], o - Offset(5 * u, 1.5f * u), Size(10 * u, 3 * u), CornerRadius(1.5f * u)) }
        }
        Topping.Candy -> {
            drawCircle(Pal.ink, 11 * u, p)
            drawCircle(if (rnd.nextBoolean()) hex(0xE5394B) else hex(0x3FAF5A), 9 * u, p)
            drawCircle(Color.White.copy(alpha = 0.7f), 3 * u, p + Offset(-3 * u, -3 * u))
        }
        Topping.Star -> drawPath(star(p, 14 * u, 6 * u), hex(0xFFCC4D)).also { drawPath(star(p, 14 * u, 6 * u), Pal.ink, style = Stroke(1.5f * u)) }
        Topping.Heart -> {
            val path = Path().apply {
                moveTo(p.x, p.y + 10 * u)
                cubicTo(p.x - 16 * u, p.y - 2 * u, p.x - 8 * u, p.y - 14 * u, p.x, p.y - 5 * u)
                cubicTo(p.x + 8 * u, p.y - 14 * u, p.x + 16 * u, p.y - 2 * u, p.x, p.y + 10 * u)
                close()
            }
            drawPath(path, hex(0xFF5C8A)); drawPath(path, Pal.ink, style = Stroke(1.5f * u))
        }
    }
}

private fun star(c: Offset, outer: Float, inner: Float) = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = -PI / 2 + i * PI / 5
        val x = (c.x + r * cos(a)).toFloat(); val y = (c.y + r * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

// ======================================================================
// MARK: - Workshop: every toy step is a little game
// ======================================================================

private enum class StepKind { Fill, Dots, Paint }

private fun kindsFor(t: Toy) = when (t) {
    Toy.Teddy -> listOf(StepKind.Fill, StepKind.Dots, StepKind.Paint)
    Toy.CandyCane -> listOf(StepKind.Fill, StepKind.Paint, StepKind.Dots)
    Toy.Train -> listOf(StepKind.Dots, StepKind.Fill, StepKind.Paint)
    Toy.Doll -> listOf(StepKind.Dots, StepKind.Fill, StepKind.Paint)
    Toy.Drum -> listOf(StepKind.Fill, StepKind.Paint, StepKind.Dots)
    Toy.Robot -> listOf(StepKind.Dots, StepKind.Fill, StepKind.Paint)
    Toy.RockingHorse -> listOf(StepKind.Dots, StepKind.Fill, StepKind.Paint)
    Toy.YoYo -> listOf(StepKind.Fill, StepKind.Dots, StepKind.Paint)
    Toy.SnowGlobe -> listOf(StepKind.Fill, StepKind.Dots, StepKind.Paint)
}

/** Which pixel letter in the toy's sprite gets your chosen color. */
private fun paintChar(t: Toy) = when (t) {
    Toy.Teddy -> 'b'
    Toy.CandyCane, Toy.Train, Toy.Drum -> 'r'
    Toy.Doll -> 'p'
    Toy.Robot -> 'y'
    Toy.RockingHorse -> 'r'
    Toy.YoYo -> 'r'
    Toy.SnowGlobe -> 'd'
}

private fun fillHint(t: Toy) = when (t) {
    Toy.Teddy -> "Tap the teddy to stuff it with fluffy cotton! ☁️"
    Toy.CandyCane -> "Tap to pull and stretch the sugar! 🍬"
    Toy.Train -> "Tap to screw on the wheels! 🔩"
    Toy.Doll -> "Tap to brush her hair! 💇"
    Toy.Drum -> "Tap to stretch the drum skin tight! 🥁"
    Toy.Robot -> "Tap to charge the battery! ⚡"
    Toy.RockingHorse -> "Tap to brush the mane! 🐴"
    Toy.YoYo -> "Tap to wind the string round and round! 🌀"
    Toy.SnowGlobe -> "Tap to shake in the snow! ❄️"
}

private fun fillPuff(t: Toy) = when (t) { Toy.Teddy -> "☁️"; Toy.CandyCane -> "✨"; Toy.Train -> "🔩"; Toy.Doll -> "💫"; Toy.Drum -> "💥"; Toy.Robot -> "⚡"; Toy.RockingHorse -> "✨"; Toy.YoYo -> "🌀"; Toy.SnowGlobe -> "❄️" }

private const val FILL_TAPS = 10
private val toyPaints = listOf(hex(0x9A5B34), hex(0xE5394B), hex(0x3D6FD6), hex(0x3FAF5A), hex(0x9B6BE0), hex(0xFF8FB0), hex(0xFFCC4D))
private val dotSpots = listOf(0.2f to 0.3f, 0.5f to 0.12f, 0.8f to 0.3f, 0.76f to 0.78f, 0.24f to 0.78f)

/** The Workshop: make toys like always. Wrapping a present opens from the button underneath. */
@Composable
fun WorkshopScreen(game: GameState) {
    var wrapping by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WorkmateBar(game, Place.Workshop)
        if (wrapping) {
            Btn("🔨 Back to making toys", Pal.red, 16, Modifier.width(260.dp)) { wrapping = false }
            WrapGame(game)
        } else {
            ToyMaker(game)
            Btn(Pal.purple, Modifier.width(300.dp), onClick = { wrapping = true }) {
                Px("gift", 26.dp); Txt("Wrap a present", 17, Color.White)
            }
        }
    }
}

@Composable
private fun ToyMaker(game: GameState) {
    var toy by remember { mutableStateOf(Toy.Teddy) }
    var step by remember { mutableIntStateOf(0) }        // 0 = not started, 1..3 = which step
    var taps by remember { mutableIntStateOf(0) }
    var dots by remember { mutableIntStateOf(0) }
    var paint by remember { mutableStateOf<Color?>(null) }
    var done by remember { mutableStateOf(false) }
    val puffs = remember { mutableStateListOf<Offset>() }
    val scope = rememberCoroutineScope()
    val kinds = kindsFor(toy)
    val kind = kinds.getOrNull(step - 1)

    fun nextStep() {
        taps = 0; dots = 0; puffs.clear()
        if (step == kinds.size) {
            done = true
            game.finishToy(toy)
            scope.launch { delay(1600); done = false; step = 0; paint = null }
        } else step++
    }

    val stepFrac = when (kind) { StepKind.Fill -> taps / FILL_TAPS.toFloat(); StepKind.Dots -> dots / 5f; else -> 0f }
    val reveal = if (done) 1f else ((step - 1).coerceAtLeast(0) + stepFrac) / kinds.size
    val image = paint?.let { Sprites.bitmap("toy|${toy.name}|$it", Sprites.rows(toy.sprite), mapOf(paintChar(toy) to it)) } ?: Sprites.named(toy.sprite)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JobChip(game, JobKind.Toys)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (t in Toy.entries) {
                val locked = t.unlockLevel > game.level
                Column(
                    Modifier.width(78.dp)
                        .squish(enabled = step == 0) { if (locked) game.say("${t.title}s unlock at Level ${t.unlockLevel} 🔒") else { toy = t; done = false; paint = null } }
                        .panel(if (toy == t) hex(0xFFF1B8) else Color.White, if (toy == t) Pal.red else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Px(t.sprite, 40.dp, alpha = if (locked) 0.3f else 1f)
                    Txt(if (locked) "🔒 Lv${t.unlockLevel}" else "×${game.s.toys.n(t)}", 12)
                }
            }
        }

        // The workbench
        val px = LocalDensity.current.density
        val tapFill = rememberUpdatedState { o: Offset ->
            if (kind == StepKind.Fill && taps < FILL_TAPS) {
                taps++; puffs.add(Offset(o.x / px, o.y / px)); Audio.play(Sfx.Pop)
                if (taps >= FILL_TAPS) nextStep()
            }
        }
        Box(
            Modifier.size(300.dp, 260.dp).panel(hex(0xC98B5E))
                .pointerInput(Unit) { detectTapGestures { tapFill.value(it) } },
            contentAlignment = Alignment.Center,
        ) {
            Px(toy.sprite, 200.dp, alpha = 0.15f)
            Bob(if (done) 8f else 0f, 300) { Px(image, 200.dp, reveal = reveal) }
            for (p in puffs) Txt(fillPuff(toy), 22, modifier = Modifier.align(Alignment.TopStart).offset(p.x.dp - 15.dp, p.y.dp - 17.dp))
            if (kind == StepKind.Dots) DotsLayer(dots) { i ->
                if (i == dots) {
                    dots++; Audio.play(Sfx.Pop)
                    if (dots >= 5) nextStep()
                } else { game.say("Tap number ${dots + 1} next!"); Audio.play(Sfx.Oops) }
            }
        }

        val label = when {
            done -> "Ta-da! A ${toy.title}! 🎁"
            step == 0 -> "Make a ${toy.title}!"
            else -> "Step $step of ${kinds.size}: ${toy.steps[step - 1]}"
        }
        Txt(label, 19, align = TextAlign.Center)
        when {
            done -> {}
            step == 0 -> Btn("🔨 Start (⚡5)", Pal.red, 20, Modifier.width(220.dp)) { if (game.startToy()) { step = 1; paint = null } }
            kind == StepKind.Fill -> {
                Txt(fillHint(toy), 16, align = TextAlign.Center)
                Box(Modifier.size(260.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(Color.White).border(2.dp, Pal.ink, RoundedCornerShape(4.dp))) {
                    Box(Modifier.fillMaxHeight().width(260.dp * stepFrac).background(Pal.green))
                }
            }
            kind == StepKind.Dots -> Txt("Tap the dots in order: 1, 2, 3, 4, 5! 🧵", 16, align = TextAlign.Center)
            kind == StepKind.Paint -> {
                Txt("Pick a color! 🎨", 16)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (c in toyPaints) Box(
                        Modifier.size(38.dp).squish { paint = c; Audio.play(Sfx.Ding); nextStep() }
                            .background(c, CircleShape).border(3.dp, Pal.ink, CircleShape)
                    )
                }
            }
        }
    }
}

/** Five numbered dots to tap in order, with stitches drawn between the ones you've done. */
@Composable
private fun DotsLayer(done: Int, onTap: (Int) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0 until done - 1) {
                val (ax, ay) = dotSpots[i]; val (bx, by) = dotSpots[i + 1]
                drawLine(Pal.ink, Offset(ax * size.width, ay * size.height), Offset(bx * size.width, by * size.height), 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
            }
            if (done == 5) {
                val (ax, ay) = dotSpots[4]; val (bx, by) = dotSpots[0]
                drawLine(Pal.ink, Offset(ax * size.width, ay * size.height), Offset(bx * size.width, by * size.height), 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
            }
        }
        for ((i, spot) in dotSpots.withIndex()) {
            val next = i == done
            Box(
                Modifier.offset((spot.first * 294 - 22).dp, (spot.second * 254 - 22).dp).size(44.dp)
                    .squish { onTap(i) }
                    .background(if (i < done) Pal.green else if (next) hex(0xFFCC4D) else Color.White, CircleShape)
                    .border(3.dp, Pal.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Txt(if (i < done) "✓" else "${i + 1}", 18) }
        }
    }
}

