package com.lexi.christmastown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - The Town: shops, bank, party place, park, town square and friends' houses
// ======================================================================

fun npcNamed(name: String) = npcs.first { it.name == name }

private fun darker(c: Color, k: Float = 0.8f) = Color(c.red * k, c.green * k, c.blue * k, 1f)

/** The little house picture, painted in this house's colours. */
fun houseImage(walls: Color, roof: Color): ImageBitmap =
    Sprites.bitmap("house|$walls|$roof", Sprites.rows("house"), mapOf('O' to walls, 'Q' to darker(walls), 'T' to roof))

@Composable
fun TownScreen(game: GameState) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt("Welcome to Christmas Town! ❄️ Tap a building to go inside.", 16, Color.White, align = TextAlign.Center)

        StreetSign("🏪 Main Street")
        Column(Modifier.fillMaxWidth().panel(hex(0xF4F8FF)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val buildings = listOf(
                Triple(Place.Shop, hex(0xF6D7E3), hex(0x9B6BE0)) to "Tinsel",
                Triple(Place.Bank, hex(0xFFF1C9), hex(0x2F6B4F)) to BANKER,
                Triple(Place.Party, hex(0xE8D5F2), hex(0xFF5FA2)) to "Sprout",
                Triple(Place.Park, hex(0xD6E6F5), hex(0x3D6FD6)) to "Snowflake",
                Triple(Place.Friends, hex(0xF4E3C3), hex(0xE5394B)) to "Gingersnap",
            )
            for (row in buildings.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((b, keeper) in row) {
                    val (place, walls, roof) = b
                    val badge = when {
                        place == Place.Party && !game.partyOpen -> "🔒"
                        place == Place.Party || place == Place.Friends -> null
                        else -> badge(game, place)
                    }
                    Building(place.title, houseImage(walls, roof), place.sprite.ifEmpty { "cocoa_mug" }, npcNamed(keeper).look, badge) { game.go(place) }
                }
            }
        }

        StreetSign("🏡 Friends' Houses")
        Txt("Knock on a door to visit! Invite friends back to YOUR house too.", 14, Color.White, align = TextAlign.Center)
        Column(Modifier.fillMaxWidth().panel(hex(0xF4F8FF)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in npcs.chunked(4)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (npc in row) FriendHouseCard(game, npc)
            }
        }
        if (game.s.homeGuests.isNotEmpty()) Txt("🏠 At your house today: ${game.s.homeGuests.joinToString(", ")}", 14, Color.White, align = TextAlign.Center)
    }
}

@Composable
private fun StreetSign(text: String) {
    Txt(text, 17, Color.White, modifier = Modifier.panel(hex(0x9A5B34), Pal.ink, 6.dp).padding(horizontal = 12.dp, vertical = 3.dp))
}

@Composable
private fun Building(title: String, house: ImageBitmap, emblem: String, keeper: ElfLook, badge: String?, onClick: () -> Unit) {
    Column(Modifier.width(124.dp).squish(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(124.dp, 112.dp)) {
            Px(house, 104.dp, Modifier.align(Alignment.BottomCenter))
            Px(emblem, 28.dp, Modifier.align(Alignment.TopCenter).offset(y = 26.dp))
            ElfPx(keeper, 48.dp, Modifier.align(Alignment.BottomEnd))   // the elf who works here, waving outside
            if (badge != null) Txt(
                badge, 12, Color.White,
                modifier = Modifier.align(Alignment.TopEnd).background(if (badge == "🔒") hex(0x8D96A8) else Pal.red, RoundedCornerShape(8.dp)).padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        Txt(title, 13, Color.White, align = TextAlign.Center, maxLines = 1, modifier = Modifier.background(Pal.ink.copy(alpha = 0.8f), RoundedCornerShape(5.dp)).padding(horizontal = 5.dp))
    }
}

@Composable
private fun FriendHouseCard(game: GameState, npc: Npc) {
    val home = remember(npc) { homeOf(npc) }
    val roof = ElfColors.clothes[npc.look.outfitColor % ElfColors.clothes.size]
    Column(Modifier.width(92.dp).squish { game.visit(npc) }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(92.dp, 80.dp)) {
            Px(houseImage(hex(home.outside), roof), 74.dp, Modifier.align(Alignment.BottomCenter))
            Px(Sprites.elf(npc.look), 30.dp, Modifier.align(Alignment.BottomEnd))
            val tag = when {
                npc.name in game.s.homeGuests -> "🏠"
                npc.name in game.s.visitedToday -> "✓"
                else -> null
            }
            if (tag != null) Txt(tag, 11, Color.White, modifier = Modifier.align(Alignment.TopEnd).background(Pal.green, RoundedCornerShape(8.dp)).padding(horizontal = 4.dp))
        }
        Txt(npc.name, 12, Pal.ink, align = TextAlign.Center, maxLines = 1)
        Txt(hearts(game.hearts(npc)), 7)
    }
}

private fun hearts(n: Int) = "💖".repeat(n) + "🤍".repeat((5 - n).coerceAtLeast(0))

// MARK: Visiting a friend

@Composable
fun FriendHouseScreen(game: GameState) {
    val npc = npcs.firstOrNull { it.name == game.visiting }
    if (npc == null) { Txt("Nobody's home! 🏠", 20, Color.White); return }
    val home = remember(npc) { homeOf(npc) }
    var says by remember(npc) { mutableStateOf(npc.hello.ifEmpty { npc.lines.first() }) }
    var line by remember(npc) { mutableIntStateOf(0) }
    LaunchedEffect(npc) { says = game.arriveAt(npc) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(406.dp, 340.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
            RoomBase(home.wall, home.stripe, home.placed, game.now)
            // your friend, their pet, and you visiting
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ElfPx(game.s.look, 120.dp)
                Bob(4f, 800) { ElfPx(npc.look, 150.dp) }
                home.pet?.let { Bob(3f, 500) { Px(it.sprite, 48.dp) } }
            }
            Txt(says, 14, align = TextAlign.Center, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).width(300.dp).panel(Color.White).padding(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(npc.job, Pal.purple, 14)
            Txt(hearts(game.hearts(npc)), 14)
            if (npc.name in game.s.bestGifts) Tag("Best friends! 💖", Pal.pink, 13)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn("💬 Chat", Pal.blue, 16, Modifier.weight(1f)) { says = npc.lines[line % npc.lines.size]; line++; game.chat(npc); Audio.play(Sfx.Message) }
            Btn("🍪 Give a cookie", hex(0xE0843A), 16, Modifier.weight(1f)) { game.giveCookie(npc) }
        }
        val coming = npc.name in game.s.homeGuests
        Btn(if (coming) "🏠 ${npc.name} is coming to your house! ✓" else "🏠 Come over to my house!", Pal.green, 16, Modifier.fillMaxWidth(), enabled = !coming) {
            game.invite(npc); if (npc.name in game.s.homeGuests) says = "Yay! I'll come over right now! 🏃💨"
        }
        Btn("👋 Bye! Back to town", Pal.red, 16, Modifier.fillMaxWidth()) { game.back() }
    }
}

// MARK: Shopkeepers

/** The elf who works here: tap to chat (and become friends). */
@Composable
fun ShopKeeper(game: GameState, name: String) {
    val npc = npcNamed(name)
    var said by remember { mutableStateOf(npc.hello.ifEmpty { npc.lines.first() }) }
    var line by remember { mutableIntStateOf(0) }
    Row(
        Modifier.fillMaxWidth().squish { said = npc.lines[line % npc.lines.size]; line++; game.chat(npc); Audio.play(Sfx.Message) }.panel(Color.White).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Bob(2f, 900) { ElfPx(npc.look, 64.dp) }
        Column(Modifier.weight(1f)) {
            Txt("${npc.name} · ${npc.job}", 13, Pal.purple)
            Txt(said, 14)
        }
        Txt("💬", 20)
    }
}
