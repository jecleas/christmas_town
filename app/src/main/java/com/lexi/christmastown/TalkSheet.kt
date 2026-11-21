package com.lexi.christmastown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Talking with a friend: pick what to say, serve treats, invite them over
// ======================================================================

private class Say(val label: String, val answer: (Npc, Int) -> String)

private val thingsToSay = listOf(
    Say("👋 How are you?") { _, _ -> howAreYouAnswers.random() },
    Say("⭐ What do you like?") { n, _ -> n.favorite.ifEmpty { "Christmas, of course! 🎄" } },
    Say("🧝 What's your job?") { n, _ -> n.about },
    Say("😆 Tell me a joke!") { _, _ -> jokes.random() },
    Say("💬 Tell me something!") { n, i -> n.lines[i % n.lines.size] },
    Say("💖 You're a great friend!") { _, _ -> friendAnswers.random() },
)

/** A pop-up chat with one friend, over whatever screen you're on. Tap outside or "Bye!" to close. */
@Composable
fun TalkSheet(game: GameState, name: String) {
    val npc = npcs.firstOrNull { it.name == name } ?: return
    var says by remember(name) { mutableStateOf(npc.hello.ifEmpty { "Hi ${game.s.name}! 👋" }) }
    var told by remember(name) { mutableIntStateOf(0) }
    val atYourHouse = game.place == null && name in game.s.homeGuests
    val atTheirHouse = game.place == Place.House && game.visiting == name
    fun close() { game.chatWith = null }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(remember { MutableInteractionSource() }, null) { close() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.width(400.dp).clickable(remember { MutableInteractionSource() }, null) {}.panel().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    Bob(4f, 700) { ElfPx(npc.look, 110.dp) }
                    game.holding[name]?.let { Px(it.sprite, 30.dp, Modifier.align(Alignment.BottomEnd)) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Txt(npc.name, 24, Pal.red)
                    Tag(npc.job, Pal.purple, 13)
                    Txt("💖".repeat(game.hearts(npc)) + "🤍".repeat((5 - game.hearts(npc)).coerceAtLeast(0)), 14)
                }
            }
            Txt(says, 17, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().panel(Color.White).padding(10.dp))

            // It's their birthday!
            if (game.birthdayToday(npc)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn(Pal.pink, Modifier.weight(1f), onClick = { says = game.happyBirthday(npc, withPresent = false) }) { Px("cake", 22.dp); Txt("Happy birthday!", 13, Color.White, maxLines = 1) }
                Btn("🎁 Give a toy", Pal.purple, 13, Modifier.weight(1f), enabled = name !in game.s.birthdayWished || game.s.toys.values.sum() > 0) { says = game.happyBirthday(npc, withPresent = true) }
            }
            // They asked you for a favour
            game.favourFrom(name)?.let { f ->
                Column(Modifier.fillMaxWidth().panel(hex(0xFFF1B8)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Txt("🙏 \"${f.ask}\"", 14, align = TextAlign.Center)
                    Btn(Pal.green, Modifier.fillMaxWidth(), onClick = { if (game.doFavour(f)) says = "You're a life saver! THANK YOU! 💖" }) {
                        Px(f.want.sprite, 22.dp)
                        Txt("Give ${f.amount} ${if (f.amount == 1) f.want.one else f.want.many} (you have ${game.have(f.want)}) · +${money(FAVOUR_REWARD)}", 13, Color.White, maxLines = 1)
                    }
                }
            }

            Txt("What do you want to say?", 14, Pal.brown)
            for (row in thingsToSay.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (t in row) Btn(t.label, Pal.blue, 14, Modifier.weight(1f)) {
                    says = t.answer(npc, told); told++
                    game.chat(npc)
                    Audio.play(Sfx.Message)
                }
            }

            if (atYourHouse || atTheirHouse) {
                Txt((if (atYourHouse) "Serve a treat" else "Share a treat") + " (you have 🍪 ${game.s.cookies.values.sum()} · ☕ ${game.s.food.n(Food.HotCocoa)} cocoa, tea is free):", 13, Pal.brown, align = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in Treat.entries) {
                        Btn(hex(0xE0843A), Modifier.weight(1f), onClick = { game.serve(npc, t)?.let { says = it } }) {
                            Px(t.sprite, 22.dp)
                            Txt(t.title, 13, Color.White, maxLines = 1)
                        }
                    }
                }
            }
            when {
                atYourHouse -> Btn("👋 Time to go home, ${npc.name}", Pal.night, 15, Modifier.fillMaxWidth()) { game.sendHome(name); close() }
                name !in game.s.homeGuests -> Btn("🏠 Come to my house later?", Pal.green, 15, Modifier.fillMaxWidth()) {
                    game.invite(npc); if (name in game.s.homeGuests) says = "Yes please! See you there! 🏃💨"
                }
            }
            Btn("Bye! 👋", Pal.red, 16, Modifier.width(160.dp)) { close() }
        }
    }
}
