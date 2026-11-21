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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
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
    Box(modifier.squish(enabled) { Audio.play(Sfx.Tap); onClick() }) {
        Box(Modifier.matchParentSize().offset(y = 4.dp).background(Pal.ink, RoundedCornerShape(10.dp)))
        Row(
            Modifier.fillMaxWidth().panel(color).padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun Btn(text: String, color: Color, size: Int = 18, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    Btn(color, modifier, enabled, onClick) { Txt(text, size, Color.White, align = TextAlign.Center) }

@Composable
fun Tag(text: String, color: Color = Pal.red, size: Int = 14) {
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

fun backgroundFor(p: Place?): Brush = when (p) {
    null, Place.Decorate, Place.Snacks, Place.Phone -> Brush.verticalGradient(listOf(hex(0x1D2B53), hex(0x3B4A8A)))
    Place.Greenhouse -> Brush.verticalGradient(listOf(hex(0xBFE9F5), hex(0xE7F7EC)))
    Place.Bakery -> Brush.verticalGradient(listOf(hex(0xFFE3C2), hex(0xF6C08E)))
    Place.Stable -> Brush.verticalGradient(listOf(hex(0x2F3F73), hex(0x6C5B8E)))
    Place.Workshop -> Brush.verticalGradient(listOf(hex(0xFFD9D2), hex(0xF2A8A0)))
    Place.SantaList, Place.Sleigh -> Brush.verticalGradient(listOf(hex(0x7A1E2E), hex(0xB8364A)))
    Place.Friends, Place.Closet -> Brush.verticalGradient(listOf(hex(0x2C4F8C), hex(0x8FB9E8)))
    Place.Shop -> Brush.verticalGradient(listOf(hex(0xE6DAFF), hex(0xC6B1F2)))
    Place.Park -> Brush.verticalGradient(listOf(hex(0x2C4F8C), hex(0x9BC8EE)))
    Place.Pets -> Brush.verticalGradient(listOf(hex(0xFFE3B0), hex(0xF7C27A)))
    Place.Advent -> Brush.verticalGradient(listOf(hex(0x1D2B53), hex(0x7A1E2E)))
    Place.Stickers -> Brush.verticalGradient(listOf(hex(0x5B3A8C), hex(0x9B6BE0)))
    Place.Party -> Brush.verticalGradient(listOf(hex(0x3A1F5C), hex(0xC2407E)))
    Place.Bank -> Brush.verticalGradient(listOf(hex(0xFFF1C9), hex(0xE9C77B)))
    Place.Town -> Brush.verticalGradient(listOf(hex(0x2C4F8C), hex(0x9BC8EE)))
    Place.House -> Brush.verticalGradient(listOf(hex(0x3A2A4A), hex(0x7A5A6A)))
    Place.Mail -> Brush.verticalGradient(listOf(hex(0x7A1E2E), hex(0xD9A066)))
}

private val darkPlaces = setOf(null, Place.Decorate, Place.Snacks, Place.Phone, Place.Stable, Place.SantaList, Place.Sleigh, Place.Friends, Place.Closet, Place.Park, Place.Advent, Place.Stickers, Place.Party, Place.Town, Place.House, Place.Mail)
fun textOn(p: Place?) = if (p in darkPlaces) Color.White else Pal.ink

// MARK: - The whole app

/** Every screen is laid out on a stage 430 wide (and at least 860 tall), then scaled to fit the phone. */
val STAGE_W = 430.dp
private val MIN_STAGE_H = 860.dp

@Composable
fun GameApp(game: GameState) {
    LaunchedEffect(Unit) { while (true) { delay(100); game.tick() } }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, 1f)) {
        val creating = !game.s.hasElf || game.showCreator
        val bgPlace = if (creating) Place.Closet else game.place
        val song = when {
            game.overlay == Overlay.Won || game.overlay == Overlay.ChristmasEve -> Music.JoyToTheWorld
            game.delivering -> if (game.christmasEveRide) Music.JoyToTheWorld else Music.UpOnTheHousetop
            game.gameSong != null && game.place != null -> game.gameSong!!
            creating -> Music.WeWish
            else -> when (game.place) {
                null -> Music.JingleBells
                Place.Greenhouse, Place.Bakery, Place.Stable, Place.Workshop, Place.SantaList -> Music.DeckTheHalls
                Place.Sleigh -> Music.SleighRide
                Place.Shop, Place.Friends, Place.Phone, Place.Closet -> Music.WeWish
                Place.Decorate, Place.Snacks -> Music.SilentNight
                Place.Park -> Music.OChristmasTree
                Place.Pets -> Music.JollyOldStNicholas
                Place.Advent -> Music.SilentNight
                Place.Stickers -> Music.JingleBells
                Place.Party -> Music.WeWish
                Place.Bank -> Music.JollyOldStNicholas
                Place.Town -> Music.GoodKingWenceslas
                Place.House -> Music.SilentNight
                Place.Mail -> Music.JollyOldStNicholas
            }
        }
        LaunchedEffect(song) { Audio.music(song) }
        Box(Modifier.fillMaxSize().background(backgroundFor(bgPlace))) {
            Snow(game.now, count = if (bgPlace in darkPlaces) 50 else 20)
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val scale = minOf(maxWidth / STAGE_W, maxHeight / MIN_STAGE_H)
                val stage = Modifier.align(Alignment.Center).requiredSize(maxWidth / scale, maxHeight / scale)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                Box(stage) {
                    when {
                        creating -> Activity(game, "👗 My Elf", onBack = if (game.s.hasElf) ({ game.showCreator = false }) else null) { CreatorScreen(game) }
                        game.delivering -> DeliveryScreen(game)
                        else -> Crossfade(game.place, label = "place") { p ->
                            if (p == null) HubScreen(game) else Activity(game, if (p == Place.House) "${game.visiting}'s House" else p.title, onBack = { game.back() }) { PlaceScreen(game, p) }
                        }
                    }
                    game.chatWith?.let { TalkSheet(game, it) }
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
private fun PlaceScreen(game: GameState, p: Place) = when (p) {
    Place.Greenhouse -> GreenhouseScreen(game)
    Place.Bakery -> BakeryScreen(game)
    Place.Stable -> StableScreen(game)
    Place.Workshop -> WorkshopScreen(game)
    Place.SantaList -> SantaListScreen(game)
    Place.Sleigh -> SleighScreen(game)
    Place.Decorate -> DecorateScreen(game)
    Place.Shop -> ShopScreen(game)
    Place.Closet -> {}   // opened via showCreator instead
    Place.Friends -> FriendsScreen(game)
    Place.Phone -> PhoneScreen(game)
    Place.Snacks -> SnacksScreen(game)
    Place.Park -> ParkScreen(game)
    Place.Pets -> PetsScreen(game)
    Place.Advent -> AdventScreen(game)
    Place.Stickers -> StickerScreen(game)
    Place.Party -> PartyScreen(game)
    Place.Bank -> BankScreen(game)
    Place.Town -> TownScreen(game)
    Place.House -> FriendHouseScreen(game)
    Place.Mail -> MailScreen(game)
}

/** A full-screen activity: big Back button, title, money and energy, then the content. */
@Composable
fun Activity(game: GameState, title: String, onBack: (() -> Unit)?, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onBack != null) Btn("← Back", Pal.red, 17, Modifier.width(100.dp), onClick = onBack)
            Txt(title, 22, textOn(if (game.showCreator || !game.s.hasElf) Place.Closet else game.place), modifier = Modifier.weight(1f), maxLines = 1)
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                MoneyChip(game)
                EnergyChip(game)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) { content() }
    }
}

@Composable
fun MoneyChip(game: GameState) = Txt("💰 ${money(game.s.money)}", 16, modifier = Modifier.panel(hex(0xFFF1B8)).padding(horizontal = 8.dp, vertical = 3.dp))

@Composable
fun EnergyChip(game: GameState) {
    Row(Modifier.panel().padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Txt("⚡", 12)
        Box(Modifier.size(54.dp, 9.dp).clip(RoundedCornerShape(3.dp)).background(hex(0xD5DBE6))) {
            Box(Modifier.fillMaxHeight().width(54.dp * (game.s.energy / 100f)).background(if (game.s.energy > 25) hex(0xFFCC4D) else Pal.red))
        }
        Txt("${game.s.energy}", 12)
    }
}

@Composable
private fun ToastView(game: GameState) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(game.toast != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
            Txt(
                game.toast ?: "", 17, Color.White, align = TextAlign.Center,
                modifier = Modifier.padding(top = 90.dp, start = 16.dp, end = 16.dp).widthIn(max = 400.dp)
                    .panel(Pal.ink.copy(alpha = 0.92f), Color.White).padding(horizontal = 14.dp, vertical = 9.dp),
            )
        }
    }
}

// MARK: - Pop-ups

@Composable
private fun JobsCard(game: GameState) {
    Column(Modifier.width(400.dp).panel().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt("📋 Santa's Jobs Today", 24)
        for (j in game.s.jobs) {
            Row(Modifier.fillMaxWidth().panel(if (j.finished) hex(0xD8F5DD) else Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Txt(j.kind.emoji, 20)
                Txt(j.text, 16, modifier = Modifier.weight(1f))
                Txt(if (j.finished) "✅" else "${j.done}/${j.goal}", 16)
            }
        }
        Txt(
            if (game.s.paidToday) "All done! You earned ${money(game.s.earnedToday)} today. Tap 💤 Sleep to start a new day."
            else "Every 2 jobs = ${money(PAY_PER_TWO_JOBS)}! 💰  Sleigh deliveries = ${money(SLEIGH_BONUS)} extra! 🛷\nEarned today: ${money(game.s.earnedToday)}",
            15, align = TextAlign.Center,
        )
        Btn("OK!", Pal.green, modifier = Modifier.width(140.dp)) { game.showJobs = false }
    }
}

@Composable
private fun OverlayCard(game: GameState) {
    Column(Modifier.width(400.dp).panel().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val o = game.overlay) {
            Overlay.None -> {}
            is Overlay.Paid -> {
                Px("santa", 110.dp)
                Txt("Ho ho ho! Great work!", 26, Pal.red, align = TextAlign.Center)
                Txt("You finished ALL of Santa's jobs!", 18, align = TextAlign.Center)
                if (o.earned > 0) Txt("Santa paid you ${money(o.earned)}! 💰", 20, align = TextAlign.Center)
                Txt("Today you earned ${money(game.s.earnedToday)} in total.", 16, Pal.darkGreen, align = TextAlign.Center)
                if (o.newLevel != null) {
                    Txt("⭐ LEVEL UP! You're now a ${levelTitles[o.newLevel - 1]}! ⭐", 19, Pal.green, align = TextAlign.Center)
                    Txt(unlocksFor(o.newLevel), 15, align = TextAlign.Center)
                }
                Btn("Yay! 🎉", Pal.green, modifier = Modifier.width(160.dp)) { game.afterPaid() }
            }
            is Overlay.Morning -> {
                Txt("☀️ Good morning!", 28)
                Txt("It's Day ${o.day} in Christmas Town.", 18, align = TextAlign.Center)
                if (o.worked) Txt("You worked hard yesterday. Santa is proud! 🎅", 16, align = TextAlign.Center)
                else Txt("You didn't finish ALL your jobs yesterday, so it didn't count toward your next level.\nEvery 2 jobs pays ${money(PAY_PER_TWO_JOBS)}!", 16, Pal.red, align = TextAlign.Center)
                if (o.interest > 0) Txt("🏦 The bank added ${money(o.interest)} to your savings overnight!", 16, Pal.darkGreen, align = TextAlign.Center)
                ElfPx(game.s.look, 100.dp)
                Btn("Let's go! ❄️", Pal.green, modifier = Modifier.width(180.dp)) { game.overlay = Overlay.None }
            }
            Overlay.ChristmasEve -> {
                Txt("🌟 CHRISTMAS EVE! 🌟", 28, Pal.red)
                Row(verticalAlignment = Alignment.Bottom) { Px("santa", 100.dp); Px("sleigh", 60.dp) }
                Txt("Santa needs his best elf on the sleigh tonight. Deliver 8 presents to the nice kids!", 17, align = TextAlign.Center)
                Btn("Hop on the sleigh! 🛷", Pal.red, modifier = Modifier.width(260.dp)) { game.startChristmasEve() }
            }
            Overlay.Won -> {
                Txt("🎄 YOU SAVED CHRISTMAS! 🎄", 24, Pal.red, align = TextAlign.Center)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElfPx(game.s.look, 100.dp); Px("santa", 110.dp)
                }
                Txt("Every nice kid got a present. Santa says you're the best elf EVER, ${game.s.name}!\nKeep playing as long as you like.", 16, align = TextAlign.Center)
                Btn("Merry Christmas! 🎁", Pal.green, modifier = Modifier.width(240.dp)) { game.overlay = Overlay.None }
            }
        }
    }
}

fun unlocksFor(level: Int) = when (level) {
    2 -> "New: Toy trains 🚂, berry cookies, Santa's naughty/nice list, more reindeer!"
    3 -> "New: Dolls, choc chip cookies, sleigh deliveries 🛷, a bigger greenhouse, more reindeer!"
    4 -> "New: Drums 🥁, more reindeer and trickier sleigh rides!"
    5 -> "New: Robots 🤖, another reindeer and windier sleigh rides! ☁️"
    6 -> "New: Rocking horses 🐴, gingerbread people, ginger seeds, a reindeer, and Santa wants presents WRAPPED 🎁"
    7 -> "New: Yo-yos, candy cane cookies, peppermint seeds, a reindeer, the Town Tree needs decorating 🎄, and the PARTY PLACE opens! 🎉🧃🎬"
    8 -> "New: Snow globes, reindeer cookies, foggy sleigh nights... and RUDOLPH joins the stable! His nose glows! 🔴"
    else -> ""
}
