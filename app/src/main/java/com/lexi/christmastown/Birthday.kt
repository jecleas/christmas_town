package com.lexi.christmastown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Your birthday party: invite friends, dinner at Elf's Restaurant,
// cake and presents, then dancing at the Party Place
// ======================================================================

private enum class BdayStep { Invite, Dinner, Cake, Presents }

@Composable
fun BirthdayScreen(game: GameState) {
    var step by remember { mutableStateOf(BdayStep.Invite) }
    val guests = remember { mutableStateListOf<Npc>() }
    var blown by remember { mutableStateOf(false) }
    var gifts by remember { mutableStateOf<List<Pair<Npc, Item>>>(emptyList()) }

    when (step) {
        BdayStep.Invite -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Bottom) { Bob(4f, 500) { ElfPx(game.s.look, 100.dp) }; Px("cake", 60.dp) }
            Txt("🎉 HAPPY BIRTHDAY, ${game.s.name}! 🎉", 22, Color.White, align = TextAlign.Center)
            Txt("Who do you want at your party? Invite up to $MAX_PARTY_GUESTS friends!", 15, Color.White, align = TextAlign.Center)
            Column(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState()).panel().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in npcs.chunked(4)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (n in row) {
                        val on = n in guests
                        Column(
                            Modifier.width(90.dp).squish {
                                if (on) guests.remove(n) else if (guests.size < MAX_PARTY_GUESTS) { guests.add(n); Audio.play(Sfx.Ding) } else game.say("Up to $MAX_PARTY_GUESTS friends at your party! 🎂")
                            }.panel(if (on) hex(0xFFE1EE) else Color.White, if (on) Pal.pink else Pal.ink, 8.dp).padding(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (on) Bob(4f, 450) { ElfPx(n.look, 58.dp) } else ElfPx(n.look, 58.dp)
                            Txt(n.name, 12, maxLines = 1)
                            if (on) Txt("Coming! 🎉", 11, Pal.pink)
                        }
                    }
                }
            }
            Btn("🍝 Off to Elf's Restaurant!", Pal.red, 18, Modifier.fillMaxWidth(), enabled = guests.isNotEmpty()) { step = BdayStep.Dinner }
        }

        BdayStep.Dinner -> Meal(game, guests.toList(), birthday = true) { step = BdayStep.Cake }

        BdayStep.Cake -> {
            GameSong(game, Music.HappyBirthday)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Txt(if (blown) "🎉 YAY! Make a wish come true! 🎉" else "🎶 Happy birthday to you! 🎶", 21, Color.White, align = TextAlign.Center)
                Column(Modifier.fillMaxWidth().panel(hex(0xFFE1EE)).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for ((i, g) in guests.take(2).withIndex()) Bob(5f, 400 + i * 90) { ElfPx(g.look, 90.dp) }
                        Bob(3f, 600) { ElfPx(game.s.look, 120.dp) }
                        for ((i, g) in guests.drop(2).take(3).withIndex()) Bob(5f, 450 + i * 90) { ElfPx(g.look, 80.dp) }
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Px(if (blown) Sprites.bitmap("cake|out", Sprites.rows("cake"), mapOf('a' to Color.Transparent, 'h' to Color.Transparent)) else Sprites.named("cake"), 170.dp)
                    }
                    Txt(if (blown) "Your friends are cheering! 👏👏👏" else "Everyone is singing to you! Make a wish... 🌟", 15, align = TextAlign.Center)
                }
                if (!blown) Btn("🌬️ Make a wish & BLOW!", Pal.blue, 20, Modifier.fillMaxWidth()) { blown = true; Audio.play(Sfx.Pop) }
                else Btn("🎁 Open your presents!", Pal.purple, 20, Modifier.fillMaxWidth()) { gifts = game.birthdayPresents(guests.toList()); step = BdayStep.Presents }
            }
        }

        BdayStep.Presents -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Txt("🎁 Presents from your friends! 🎁", 21, Color.White)
            for ((n, item) in gifts) Row(Modifier.fillMaxWidth().panel(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ElfPx(n.look, 54.dp)
                Column(Modifier.weight(1f)) { Txt("From ${n.name}", 14, Pal.red); Txt("A ${item.title}! Put it in your house 🏠", 14) }
                Px(item.sprite, 44.dp)
            }
            Btn("💃 To the Party Place! Let's dance!", Pal.pink, 18, Modifier.fillMaxWidth()) {
                game.partyWith = guests.map { it.name }
                game.birthdayDance = true
                game.go(Place.Party)
            }
        }
    }
}
