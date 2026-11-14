@file:OptIn(ExperimentalTextApi::class)

package com.lexi.christmastown

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

// MARK: - Text & buttons

val Pixelify = FontFamily(
    listOf(400, 500, 600, 700).map { w -> Font(R.font.pixelify, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w))) }
)

@Composable
fun Txt(text: String, size: Int, color: Color = Pal.ink, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Start, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) {
    BasicText(
        text, modifier, maxLines = maxLines,
        style = TextStyle(fontFamily = Pixelify, fontWeight = weight, fontSize = size.sp, color = color, textAlign = align, lineHeight = (size * 1.2f).sp),
    )
}

@Composable
fun Modifier.squish(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium), label = "squish")
    return graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (enabled) 1f else 0.5f }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

/** Pixel-style panel: square-ish corners and a thick dark border. */
fun Modifier.panel(color: Color = Pal.cream, border: Color = Pal.ink, radius: Dp = 10.dp) =
    this.background(border, RoundedCornerShape(radius))
        .padding(3.dp)
        .background(color, RoundedCornerShape(radius - 3.dp))

@Composable
fun Btn(color: Color, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Box(modifier.squish(enabled, onClick)) {
        Box(Modifier.matchParentSize().offset(y = 4.dp).background(Pal.ink, RoundedCornerShape(10.dp)))
        Row(
            Modifier.panel(color).padding(horizontal = 18.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun Btn(text: String, color: Color, size: Int = 20, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    Btn(color, modifier, enabled, onClick) { Txt(text, size, Color.White) }

@Composable
fun Tag(text: String, color: Color = Pal.red, size: Int = 15) {
    Txt(text, size, Color.White, modifier = Modifier.background(color, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
}

@Composable
fun Bubble(text: String, width: Dp = 220.dp) {
    Txt(text, 15, align = TextAlign.Center, modifier = Modifier.width(width).panel(Color.White).padding(10.dp))
}

fun money(n: Int) = "\$" + "%,d".format(n)

// MARK: - Snow & backgrounds

@Composable
fun Snow(now: Long, modifier: Modifier = Modifier, count: Int = 50) {
    Canvas(modifier.fillMaxSize()) {
        val t = now / 1000f
        for (i in 0 until count) {
            val speed = 18f + (i * 7) % 23
            val x = ((i * 97) % 1000) / 1000f * size.width + sin(t * 0.8f + i) * 12f
            val y = ((i * 53 % 1000) / 1000f * size.height + t * speed * density) % size.height
            val r = (2 + i % 3) * density
            drawRect(Color.White.copy(alpha = 0.85f), Offset(x, y), Size(r, r))
        }
    }
}

fun backgroundFor(p: Place): Brush = when (p) {
    Place.Home -> Brush.verticalGradient(listOf(hex(0x1D2B53), hex(0x3B4A8A)))
    Place.Greenhouse -> Brush.verticalGradient(listOf(hex(0xBFE9F5), hex(0xE7F7EC)))
    Place.Bakery -> Brush.verticalGradient(listOf(hex(0xFFE3C2), hex(0xF6C08E)))
    Place.Stable -> Brush.verticalGradient(listOf(hex(0x2F3F73), hex(0x6C5B8E)))
    Place.Workshop -> Brush.verticalGradient(listOf(hex(0xFFD9D2), hex(0xF2A8A0)))
    Place.Santa -> Brush.verticalGradient(listOf(hex(0x7A1E2E), hex(0xB8364A)))
    Place.Town -> Brush.verticalGradient(listOf(hex(0x2C4F8C), hex(0x8FB9E8)))
    Place.Shop -> Brush.verticalGradient(listOf(hex(0xE6DAFF), hex(0xC6B1F2)))
}

private val darkPlaces = setOf(Place.Home, Place.Stable, Place.Santa, Place.Town)
fun textOn(p: Place) = if (p in darkPlaces) Color.White else Pal.ink

// MARK: - The whole app

private val STAGE_H = 780.dp
private val MIN_STAGE_W = 1100.dp

@Composable
fun GameApp(game: GameState) {
    LaunchedEffect(Unit) { while (true) { delay(100); game.tick() } }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, 1f)) {
        val creating = !game.s.hasElf || game.showCreator
        val bgPlace = if (creating) Place.Town else game.place
        Box(Modifier.fillMaxSize().background(backgroundFor(bgPlace))) {
            Snow(game.now, count = if (bgPlace in darkPlaces) 60 else 25)
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout)) {
                val scale = minOf(maxHeight / STAGE_H, maxWidth / MIN_STAGE_W)
                val stage = Modifier.align(Alignment.Center).requiredSize(maxWidth / scale, maxHeight / scale)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                Box(stage) {
                    when {
                        creating -> CreatorScreen(game)
                        game.delivering -> DeliveryScreen(game)
                        else -> MainScreens(game)
                    }
                    ToastView(game)
                }
                if (game.overlay != Overlay.None || game.showJobs) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(remember { MutableInteractionSource() }, null) { game.showJobs = false })
                    Box(stage, contentAlignment = Alignment.Center) {
                        if (game.overlay != Overlay.None) OverlayCard(game) else JobsCard(game)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastView(game: GameState) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(game.toast != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
            Txt(
                game.toast ?: "", 19, Color.White, align = TextAlign.Center,
                modifier = Modifier.padding(top = 96.dp).widthIn(max = 760.dp).panel(Pal.ink.copy(alpha = 0.92f), Color.White).padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun MainScreens(game: GameState) {
    Column(Modifier.fillMaxSize()) {
        HUD(game, Modifier.padding(horizontal = 20.dp).padding(top = 8.dp))
        Crossfade(game.place, Modifier.weight(1f).fillMaxWidth(), label = "place") { p ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (p) {
                    Place.Home -> HomeScreen(game)
                    Place.Greenhouse -> GreenhouseScreen(game)
                    Place.Bakery -> BakeryScreen(game)
                    Place.Stable -> StableScreen(game)
                    Place.Workshop -> WorkshopScreen(game)
                    Place.Santa -> SantaScreen(game)
                    Place.Town -> TownScreen(game)
                    Place.Shop -> ShopScreen(game)
                }
            }
        }
        NavBar(game, Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
    }
}

// MARK: - Top bar

@Composable
private fun HUD(game: GameState, modifier: Modifier) {
    val s = game.s
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.squish { game.showCreator = true }.panel().padding(start = 4.dp, end = 10.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ElfPx(s.look, 40.dp)
            Txt(s.name, 20)
        }
        Txt("Day ${s.day}", 18, modifier = Modifier.panel().padding(horizontal = 10.dp, vertical = 6.dp))
        Txt("⭐ Lv ${game.level} ${game.levelTitle}", 18, modifier = Modifier.panel().padding(horizontal = 10.dp, vertical = 6.dp))
        Row(Modifier.panel().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt("⚡", 16)
            Box(Modifier.size(100.dp, 12.dp).clip(RoundedCornerShape(3.dp)).background(hex(0xD5DBE6))) {
                Box(Modifier.fillMaxHeight().width(100.dp * (s.energy / 100f)).background(if (s.energy > 25) hex(0xFFCC4D) else Pal.red))
            }
            Txt("${s.energy}", 16)
        }
        Spacer(Modifier.weight(1f))
        Txt(
            if (s.paidToday) "📋 Done! ✅" else "📋 Jobs ${game.jobsDone}/${s.jobs.size}", 18, Color.White,
            modifier = Modifier.squish { game.showJobs = true }.panel(if (s.paidToday) Pal.green else Pal.red).padding(horizontal = 12.dp, vertical = 6.dp),
        )
        Txt("💰 ${money(s.money)}", 20, modifier = Modifier.panel(hex(0xFFF1B8)).padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
private fun NavBar(game: GameState, modifier: Modifier) {
    Row(modifier.panel(Pal.cream).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (p in Place.entries) {
            val selected = game.place == p
            Column(
                Modifier.squish { game.go(p) }.width(78.dp)
                    .background(if (selected) p.color else Color.Transparent, RoundedCornerShape(8.dp)).padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Txt(p.emoji, 24)
                Txt(p.title, 13, if (selected) Color.White else Pal.ink)
            }
        }
    }
}

// MARK: - Pop-ups

@Composable
private fun JobsCard(game: GameState) {
    Column(Modifier.width(520.dp).panel().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt("📋 Santa's Jobs for Today", 26)
        for (j in game.s.jobs) {
            Row(Modifier.fillMaxWidth().panel(if (j.finished) hex(0xD8F5DD) else Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Txt(j.kind.emoji, 22)
                Txt(j.text, 18, modifier = Modifier.weight(1f))
                Txt(if (j.finished) "✅" else "${j.done}/${j.goal}", 18)
            }
        }
        Txt(
            if (game.s.paidToday) "You finished today! Santa paid you ${money(PAY)}. Go home and sleep 💤"
            else "Finish ALL the jobs and Santa pays you ${money(PAY)}!",
            16, align = TextAlign.Center,
        )
        Btn("OK!", Pal.green) { game.showJobs = false }
    }
}

@Composable
private fun OverlayCard(game: GameState) {
    Column(Modifier.width(560.dp).panel().padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val o = game.overlay) {
            Overlay.None -> {}
            is Overlay.Paid -> {
                Px("santa", 120.dp)
                Txt("Ho ho ho! Great work!", 30, Pal.red)
                Txt("Santa paid you ${money(PAY)}! 💰", 22)
                if (o.newLevel != null) {
                    Txt("⭐ LEVEL UP! You're now a ${levelTitles[o.newLevel - 1]}! ⭐", 22, Pal.green, align = TextAlign.Center)
                    Txt(unlocksFor(o.newLevel), 17, align = TextAlign.Center)
                }
                Btn("Yay! 🎉", Pal.green) { game.afterPaid() }
            }
            is Overlay.Morning -> {
                Txt("☀️ Good morning!", 32)
                Txt("It's Day ${o.day} in Christmas Town.", 20)
                if (o.worked) Txt("You worked hard yesterday. Santa is proud! 🎅", 18, align = TextAlign.Center)
                else Txt("You didn't finish your jobs yesterday, so no pay. 💸\nWork today to earn ${money(PAY)}!", 18, Pal.red, align = TextAlign.Center)
                ElfPx(game.s.look, 110.dp)
                Btn("Let's go! ❄️", Pal.green) { game.overlay = Overlay.None }
            }
            Overlay.ChristmasEve -> {
                Txt("🌟 It's CHRISTMAS EVE! 🌟", 32, Pal.red)
                Row(verticalAlignment = Alignment.Bottom) { Px("santa", 110.dp); Px("sleigh", 70.dp) }
                Txt("Santa needs his best elf on the sleigh tonight. Deliver 8 presents to the nice kids!", 19, align = TextAlign.Center)
                Btn("Hop on the sleigh! 🛷", Pal.red) { game.startChristmasEve() }
            }
            Overlay.Won -> {
                Txt("🎄 YOU SAVED CHRISTMAS! 🎄", 30, Pal.red)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ElfPx(game.s.look, 110.dp); Px("santa", 120.dp); Px("reindeer", 70.dp)
                }
                Txt("Every nice kid got a present. Santa says you're the best elf EVER, ${game.s.name}!\nKeep playing as long as you like.", 18, align = TextAlign.Center)
                Btn("Merry Christmas! 🎁", Pal.green) { game.overlay = Overlay.None }
            }
        }
    }
}

fun unlocksFor(level: Int) = when (level) {
    2 -> "New: Toy trains 🚂, berry cookies, Santa's naughty/nice list, more reindeer!"
    3 -> "New: Dolls, choc chip cookies, present deliveries 🛷, a bigger greenhouse, more reindeer!"
    4 -> "New: Drums 🥁 and more reindeer!"
    5 -> "New: Robots 🤖 and RUDOLPH joins the stable! 🔴"
    else -> ""
}
