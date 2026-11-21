package com.lexi.christmastown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Mailbox: Maple brings letters every morning, and you can write back
// ======================================================================

@Composable
fun MailScreen(game: GameState) {
    val letters = game.s.letters
    LaunchedEffect(Unit) { game.readLetters() }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 800) { ElfPx(npcNamed("Maple").look, 90.dp) }
            Bubble(if (letters.isEmpty()) "No letters yet! I'll bring some tomorrow morning ✉️" else "Special delivery! You've got mail! 📬", 240.dp)
        }
        for (l in letters) {
            val from = npcs.firstOrNull { it.name == l.from }
            Column(Modifier.fillMaxWidth().panel(if (l.day == game.s.day) Color.White else hex(0xF4EEE4)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    from?.let { ElfPx(it.look, 44.dp) }
                    Column(Modifier.weight(1f)) {
                        Txt("From ${l.from}", 15, Pal.red)
                        Txt("Day ${l.day}" + if (l.day == game.s.day) " · NEW ✨" else "", 12, Pal.brown)
                    }
                }
                Txt(l.text, 16)
                if (l.replied) Txt("✉️ You wrote back!", 13, Pal.darkGreen)
                else if (from != null) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (r in letterReplies) Btn(r, Pal.blue, 13, Modifier.weight(1f)) { game.reply(l) }
                }
            }
        }
    }
}

/** Today's favours, shown on the Town map and the Friends screen. Tap one to talk to that friend. */
@Composable
fun FavourList(game: GameState) {
    val open = game.s.favours.filter { !it.done }
    val birthdays = npcs.filter { game.birthdayToday(it) }
    if (open.isEmpty() && birthdays.isEmpty()) return
    Column(Modifier.fillMaxWidth().panel(hex(0xFFF1B8)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (b in birthdays) Row(Modifier.fillMaxWidth().squish { game.chatWith = b.name }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Px("cake", 26.dp); Txt("It's ${b.name}'s birthday today! Tap to say happy birthday 🎂", 13, modifier = Modifier.weight(1f))
        }
        for (f in open) Row(Modifier.fillMaxWidth().squish { game.chatWith = f.from }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ElfPx(npcNamed(f.from).look, 32.dp)
            Txt("${f.from} needs ${f.amount} ${if (f.amount == 1) f.want.one else f.want.many}! Tap to help 🙏", 13, modifier = Modifier.weight(1f))
            Px(f.want.sprite, 24.dp)
        }
    }
}

