package com.lexi.christmastown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - Friends: talk to other elves

@Composable
fun FriendsScreen(game: GameState) {
    var talking by remember { mutableStateOf(npcs.first()) }
    var line by remember { mutableIntStateOf(0) }
    var party by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt("Tap an elf to talk to them!", 18, Color.White)
        FavourList(game)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (npc in npcs) {
                val sel = npc == talking
                Column(
                    Modifier.width(98.dp).squish { talking = npc; line = 0; party = false; game.chat(npc) }
                        .panel(if (sel) hex(0xFFF1B8) else Pal.cream, if (sel) Pal.red else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Bob(2f, 800 + npc.name.length * 60) { ElfPx(npc.look, 84.dp) }
                    Txt(npc.name, 15)
                    Txt(hearts(game.hearts(npc)), 9)
                }
            }
        }

        val npc = talking
        Column(Modifier.fillMaxWidth().panel().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ElfPx(npc.look, 120.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Txt(npc.name, 26, Pal.red)
                    Txt(npc.job, 15, Pal.brown)
                    Txt(hearts(game.hearts(npc)), 15)
                    if (npc.name in game.s.bestGifts) Tag("Best friends! 💖", Pal.pink, 13)
                }
            }
            if (party) {
                Txt("Yay! Help me pick a theme for my party:", 18, align = TextAlign.Center)
                for ((theme, reward) in partyThemes) Btn(theme, Pal.purple, 17, Modifier.fillMaxWidth()) { game.throwParty(reward); party = false }
            } else {
                Txt("“${npc.lines[line % npc.lines.size]}”", 19, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().panel(Color.White).padding(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("💬 Chat", Pal.blue, 16, Modifier.weight(1f)) { line++; game.chat(npc) }
                Btn("🍪 Give cookie", hex(0xE0843A), 16, Modifier.weight(1f)) { game.giveCookie(npc) }
            }
            if (npc.name == "Twinkle" && !game.s.partyDone) {
                Btn("🎉 Plan a party", Pal.pink, 17, Modifier.fillMaxWidth(), enabled = game.hearts(npc) >= 3) { party = true }
                if (game.hearts(npc) < 3) Txt("Become better friends (3 💖) to plan a party!", 13, Pal.brown)
            }
            Txt("Chat every day and give cookies to make friends. Best friends (5 💖) give you a gift!", 13, Pal.brown, align = TextAlign.Center)
        }
    }
}

private fun hearts(n: Int) = "💖".repeat(n) + "🤍".repeat((5 - n).coerceAtLeast(0))

// MARK: - Shop

private enum class ShopTab(val title: String) { Clothes("👗 Clothes"), Hair("💇 Hair"), Furniture("🛋️ Furniture"), Decor("🎄 Decor"), Food("🍎 Food & Seeds") }

@Composable
fun ShopScreen(game: GameState) {
    var tab by remember { mutableStateOf(ShopTab.Clothes) }
    val s = game.s
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ShopKeeper(game, "Tinsel")
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (t in ShopTab.entries) Txt(
                t.title, 15, if (tab == t) Color.White else Pal.ink,
                modifier = Modifier.squish { tab = t }.panel(if (tab == t) Pal.purple else Color.White).padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        val cards: List<@Composable () -> Unit> = when (tab) {
            ShopTab.Clothes ->
                Outfit.entries.filter { it.price > 0 }.map { o -> @Composable { WearCard(o.title, o.price, Sprites.elf(s.look.copy(outfit = o)), o in s.outfits, s.look.outfit == o, { game.buyOutfit(o) }) { game.setLook(s.look.copy(outfit = o)) } } } +
                    Hat.entries.filter { it.price > 0 }.map { h -> @Composable { WearCard(h.title, h.price, Sprites.elf(s.look.copy(hat = h)), h in s.hats, s.look.hat == h, { game.buyHat(h) }) { game.setLook(s.look.copy(hat = h)) } } } +
                    Accessory.entries.filter { it.price > 0 }.map { a -> @Composable { WearCard(a.title, a.price, Sprites.elf(s.look.copy(accessory = a)), a in s.accessories, s.look.accessory == a, { game.buyAccessory(a) }) { game.setLook(s.look.copy(accessory = a)) } } }
            ShopTab.Hair -> HairStyle.entries.filter { it.price > 0 }.map { h ->
                @Composable { WearCard(h.title, h.price, Sprites.elf(s.look.copy(hairStyle = h, hat = Hat.None)), h in s.hair, s.look.hairStyle == h, { game.buyHair(h) }) { game.setLook(s.look.copy(hairStyle = h)) } }
            }
            ShopTab.Furniture -> Item.entries.filter { it.kind == ItemKind.Furniture }.map { i -> @Composable { ItemCard(game, i) } }
            ShopTab.Decor -> Item.entries.filter { it.kind == ItemKind.Decoration }.map { i -> @Composable { ItemCard(game, i) } }
            ShopTab.Food ->
                Food.entries.map { f -> @Composable { SimpleCard(f.title, f.sprite, "+${f.energy}⚡ · have ${s.food.n(f)}", f.price) { game.buyFood(f) } } } +
                    Crop.entries.map { c -> @Composable { SimpleCard("${c.title} seeds", c.sprite, if (c.unlockLevel > game.level) "🔒 Level ${c.unlockLevel}" else "have ${s.seeds.n(c)}", c.seedPrice) { game.buySeed(c) } } }
        }
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in cards.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { for (c in row) c() }
        }
    }
}

@Composable
private fun CardShell(content: @Composable () -> Unit) {
    Column(Modifier.width(198.dp).height(226.dp).panel().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) { content() }
}

@Composable
private fun WearCard(title: String, price: Int, preview: ImageBitmap, owned: Boolean, wearing: Boolean, buy: () -> Unit, wear: () -> Unit) = CardShell {
    Box(Modifier.height(110.dp), contentAlignment = Alignment.Center) { Px(preview, 104.dp) }
    Txt(title, 16, align = TextAlign.Center, maxLines = 1)
    when {
        wearing -> Tag("Wearing ✨", Pal.green)
        owned -> Btn("Wear", Pal.blue, 15, Modifier.width(120.dp)) { wear() }
        else -> Btn("Buy ${money(price)}", Pal.red, 15, Modifier.width(150.dp)) { buy() }
    }
}

@Composable
private fun ItemCard(game: GameState, i: Item) = CardShell {
    Box(Modifier.height(104.dp), contentAlignment = Alignment.Center) { Px(i.sprite, minOf(100, Sprites.rows(i.sprite).size * 5).dp) }
    Txt(i.title, 16, align = TextAlign.Center, maxLines = 1)
    Txt("You own ${game.s.owned.n(i)}", 13, Pal.brown)
    Btn("Buy ${money(i.price)}", Pal.red, 15, Modifier.width(150.dp)) { game.buyItem(i) }
}

@Composable
private fun SimpleCard(title: String, sprite: String, info: String, price: Int, buy: () -> Unit) = CardShell {
    Box(Modifier.height(104.dp), contentAlignment = Alignment.Center) { Px(sprite, 76.dp) }
    Txt(title, 16, align = TextAlign.Center, maxLines = 1)
    Txt(info, 13, Pal.brown)
    Btn("Buy ${money(price)}", Pal.red, 15, Modifier.width(150.dp)) { buy() }
}

// MARK: - Closet: make / change your elf

@Composable
fun CreatorScreen(game: GameState) {
    val s = game.s
    var name by remember { mutableStateOf(if (s.hasElf) s.name else "") }
    var look by remember { mutableStateOf(s.look) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Bob(4f, 800) { ElfPx(look, 170.dp) }
            Column(Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicTextField(
                    name, { name = it.take(12) }, singleLine = true,
                    textStyle = TextStyle(fontFamily = Pixelify, fontSize = 22.sp, color = Pal.ink),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, autoCorrectEnabled = false),
                    modifier = Modifier.fillMaxWidth().panel(Color.White).padding(horizontal = 10.dp, vertical = 8.dp),
                    decorationBox = { inner -> Box { if (name.isEmpty()) Txt("Elf name", 22, Pal.ink.copy(alpha = 0.35f)); inner() } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (girl in listOf(true, false)) Chip(if (girl) "Girl" else "Boy", look.girl == girl) {
                        if (look.girl != girl) look = look.copy(girl = girl, hairStyle = if (girl) HairStyle.Long else HairStyle.Short, outfit = if (girl && Outfit.Dress in s.outfits) Outfit.Dress else Outfit.Tunic)
                    }
                }
                Btn(if (s.hasElf) "Done! ✨" else "Start! 🎅", Pal.green, 20, Modifier.fillMaxWidth()) { game.saveElf(name, look) }
            }
        }
        Column(
            Modifier.fillMaxWidth().weight(1f).panel().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Opt("Skin") { ElfColors.skins.forEachIndexed { i, c -> Swatch(c, look.skin == i) { look = look.copy(skin = i) } } }
            Opt("Hair") { for (h in HairStyle.entries) Preview(game, look.copy(hairStyle = h, hat = Hat.None), look.hairStyle == h, h !in s.hair) { look = look.copy(hairStyle = h) } }
            Opt("Hair color") { ElfColors.hairs.forEachIndexed { i, c -> Swatch(c, look.hairColor == i) { look = look.copy(hairColor = i) } } }
            Opt("Eyes") { ElfColors.eyes.forEachIndexed { i, c -> Swatch(c, look.eye == i) { look = look.copy(eye = i) } } }
            Opt("Outfit") { for (o in Outfit.entries) Preview(game, look.copy(outfit = o), look.outfit == o, o !in s.outfits) { look = look.copy(outfit = o) } }
            Opt("Outfit color") { ElfColors.clothes.forEachIndexed { i, c -> Swatch(c, look.outfitColor == i) { look = look.copy(outfitColor = i) } } }
            Opt("Hat") { for (h in Hat.entries) Preview(game, look.copy(hat = h), look.hat == h, h !in s.hats) { look = look.copy(hat = h) } }
            Opt("Hat color") { ElfColors.clothes.forEachIndexed { i, c -> Swatch(c, look.hatColor == i) { look = look.copy(hatColor = i) } } }
            Opt("Extras") { for (a in Accessory.entries) Preview(game, look.copy(accessory = a), look.accessory == a, a !in s.accessories) { look = look.copy(accessory = a) } }
            Opt("🎂 My birthday") {
                val m = s.myBirthMonth; val d = s.myBirthDay
                Btn("◀", Pal.pink, 15, Modifier.width(48.dp)) { game.setMyBirthday(if (m == 0) 1 else m - 1, maxOf(d, 1)) }
                Txt(if (m == 0) "Not set" else monthNames[m - 1], 16, modifier = Modifier.width(110.dp), align = TextAlign.Center)
                Btn("▶", Pal.pink, 15, Modifier.width(48.dp)) { game.setMyBirthday(m + 1, maxOf(d, 1)) }
                Btn("−", Pal.pink, 15, Modifier.width(44.dp), enabled = m > 0) { game.setMyBirthday(m, d - 1) }
                Txt(if (m == 0) "-" else "$d", 18, modifier = Modifier.width(34.dp), align = TextAlign.Center)
                Btn("+", Pal.pink, 15, Modifier.width(44.dp), enabled = m > 0) { game.setMyBirthday(m, d + 1) }
            }
            Txt("On your birthday you can throw a party with your friends! 🎉", 13, Pal.brown)
            Txt("🔒 Locked things can be bought in the Shop!", 14, Pal.brown)
        }
    }
}

@Composable
private fun Opt(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Txt(title, 16)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { content() }
    }
}

@Composable
private fun Swatch(c: Color, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).squish(onClick = onClick).border(3.dp, if (selected) Pal.red else Color.Transparent, CircleShape).padding(5.dp).background(c, CircleShape).border(2.dp, Pal.ink, CircleShape))
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Txt(text, 17, if (selected) Color.White else Pal.ink, modifier = Modifier.squish(onClick = onClick).panel(if (selected) Pal.red else Color.White).padding(horizontal = 12.dp, vertical = 5.dp))
}

@Composable
private fun Preview(game: GameState, sample: ElfLook, selected: Boolean, locked: Boolean, onClick: () -> Unit) {
    val tap = { if (locked) game.say("That's in the Shop! 🛍️ Work for Santa to earn money.") else onClick() }
    Box(Modifier.size(60.dp, 72.dp).squish(onClick = tap).panel(if (selected) hex(0xFFF1B8) else Color.White, if (selected) Pal.red else Pal.ink), contentAlignment = Alignment.Center) {
        Px(Sprites.elf(sample), 60.dp, alpha = if (locked) 0.35f else 1f)
        if (locked) Txt("🔒", 18)
    }
}
