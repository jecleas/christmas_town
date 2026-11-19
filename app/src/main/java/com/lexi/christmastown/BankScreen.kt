package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.sin

// ======================================================================
// MARK: - Bank: Mr. Silverbell keeps your savings safe (and adds a little every night)
// ======================================================================

private enum class BankTalk { Menu, PutIn, TakeOut }
private val bankGold = hex(0xD9A21B)
private val amounts = listOf(100, 500, 1000, 5000)

@Composable
fun BankScreen(game: GameState) {
    val s = game.s
    var talk by remember { mutableStateOf(BankTalk.Menu) }
    var says by remember { mutableStateOf(bankerHellos.random()) }
    var chat by remember { mutableIntStateOf(0) }
    var coinsAt by remember { mutableIntStateOf(0) }      // bumps to start a coin animation
    var coinsToYou by remember { mutableStateOf(true) }

    fun putIn(amount: Int) {
        val n = game.deposit(amount)
        if (n > 0) {
            says = "Thank you! I've put ${money(n)} safely in the bank. You now have ${money(game.s.bankSaved)} saved. 🏦"
            coinsToYou = false; coinsAt++
        }
        talk = BankTalk.Menu
    }

    fun takeOut(amount: Int) {
        val n = game.withdraw(amount)
        if (n > 0) {
            says = "Here you are: ${money(n)}! Let me count it for you... all there! Spend it wisely. 💵"
            coinsToYou = true; coinsAt++
        }
        talk = BankTalk.Menu
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BankCounter(game, says, coinsAt, coinsToYou)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f).panel(Color.White).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("👛 In your pocket", 14, Pal.brown); Txt(money(s.money), 22)
            }
            Column(Modifier.weight(1f).panel(hex(0xFFF1B8), bankGold).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("🏦 Saved in the bank", 14, Pal.brown); Txt(money(s.bankSaved), 22, Pal.darkGreen)
            }
        }

        Column(Modifier.fillMaxWidth().panel().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (talk) {
                BankTalk.Menu -> {
                    Txt("What would you like to say?", 17)
                    Btn("💰 I'd like to put money in, please", bankGold, 16, Modifier.fillMaxWidth()) {
                        talk = BankTalk.PutIn; says = "Splendid! Saving is very clever. How much would you like to put in?"
                    }
                    Btn("💵 I'd like to take money out, please", Pal.green, 16, Modifier.fillMaxWidth()) {
                        talk = BankTalk.TakeOut
                        says = if (s.bankSaved > 0) "Of course! You have ${money(s.bankSaved)} saved. How much would you like?" else "Hmm, let me check my big book... you haven't saved anything yet! Put some money in first."
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Btn("👋 Hello!", Pal.blue, 15, Modifier.weight(1f)) { says = bankerHellos.random(); Audio.play(Sfx.Message) }
                        Btn("💬 Chat", Pal.purple, 15, Modifier.weight(1f)) { says = bankerChats[chat % bankerChats.size]; chat++; Audio.play(Sfx.Message) }
                        Btn("📈 Tonight?", Pal.red, 15, Modifier.weight(1f)) {
                            val i = bankInterest(s.bankSaved)
                            says = if (i > 0) "Tonight while you sleep, I'll add ${money(i)} to your savings. Money that grows. Isn't that magic? ✨"
                            else "Save some money with me, and every night I'll add \$5 for every \$100 you have saved!"
                        }
                    }
                }
                BankTalk.PutIn, BankTalk.TakeOut -> {
                    val putting = talk == BankTalk.PutIn
                    val have = if (putting) s.money else s.bankSaved
                    Txt(if (putting) "How much do you want to put in?" else "How much do you want to take out?", 17)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (a in amounts) Btn(money(a), if (putting) bankGold else Pal.green, 15, Modifier.weight(1f), enabled = have >= a) {
                            if (putting) putIn(a) else takeOut(a)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Btn("All of it (${money(have)})", if (putting) bankGold else Pal.green, 15, Modifier.weight(1f), enabled = have > 0) {
                            if (putting) putIn(have) else takeOut(have)
                        }
                        Btn("Never mind", Pal.night, 15, Modifier.width(130.dp)) { talk = BankTalk.Menu; says = "No problem at all! Anything else?" }
                    }
                }
            }
        }
        Txt("Every night the bank adds \$5 for every \$100 you save (up to \$500)!", 13, Pal.brown, align = TextAlign.Center)
    }
}

/** The bank counter: Mr. Silverbell behind it, you in front (seen from behind), and coins flying across. */
@Composable
private fun BankCounter(game: GameState, says: String, coinsAt: Int, toYou: Boolean) {
    var coinT by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(coinsAt) {
        if (coinsAt == 0) return@LaunchedEffect
        val start = withFrameMillis { it }
        coinT = 0f
        while (coinT < 1f) withFrameMillis { coinT = ((it - start) / 900f).coerceAtMost(1f) }
    }
    val coin = remember { Sprites.named("coin") }
    Box(Modifier.size(406.dp, 290.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            // green wall with gold trim, a big round vault door
            drawRect(hex(0x2F6B4F))
            for (i in 0 until 9) drawRect(hex(0x2A6047), Offset((i * 48f) * u, 0f), Size(24 * u, 200 * u))
            drawRect(bankGold, Offset(0f, 196 * u), Size(size.width, 6 * u))
            drawCircle(Pal.ink, 46 * u, Offset(345 * u, 80 * u)); drawCircle(hex(0xB8BFCC), 43 * u, Offset(345 * u, 80 * u))
            drawCircle(hex(0x8D96A8), 30 * u, Offset(345 * u, 80 * u)); drawCircle(hex(0xB8BFCC), 8 * u, Offset(345 * u, 80 * u))
            for (k in 0 until 4) { val a = k * Math.PI.toFloat() / 2; drawLine(Pal.ink, Offset(345 * u, 80 * u), Offset((345 + kotlin.math.cos(a) * 26) * u, (80 + sin(a) * 26) * u), 3 * u) }
            // floor
            drawRect(hex(0xC9A97A), Offset(0f, 202 * u), Size(size.width, size.height))
            for (i in 0 until 10) drawRect(hex(0xB8966A), Offset((i * 44f) * u, 202 * u), Size(2 * u, size.height))
        }
        // the sign
        Txt("🏦 CHRISTMAS TOWN BANK", 14, Pal.ink, modifier = Modifier.offset(12.dp, 10.dp).panel(hex(0xFFF1B8), bankGold, 6.dp).padding(horizontal = 8.dp, vertical = 2.dp))
        // Mr. Silverbell behind the counter
        Box(Modifier.offset(30.dp, 50.dp)) { Bob(2f, 1200) { ElfPx(bankerLook, 150.dp) } }
        Txt(BANKER, 12, Color.White, modifier = Modifier.offset(40.dp, 38.dp))
        // the counter
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRoundRect(Pal.ink, Offset(6 * u, 168 * u), Size(250 * u, 64 * u), CornerRadius(6 * u))
            drawRoundRect(hex(0x9A5B34), Offset(9 * u, 171 * u), Size(244 * u, 58 * u), CornerRadius(5 * u))
            drawRect(hex(0xC98B5E), Offset(9 * u, 171 * u), Size(244 * u, 10 * u))
            for (i in 0 until 4) drawRect(hex(0x7E4A2A), Offset((30 + i * 58) * u, 190 * u), Size(40 * u, 30 * u))
        }
        // what Mr. Silverbell says
        Txt(says, 14, align = TextAlign.Center, modifier = Modifier.offset(204.dp, 118.dp).width(192.dp).panel(Color.White).padding(8.dp))
        // you, standing at the counter (we see your back)
        Box(Modifier.offset(60.dp, 162.dp)) { Px(Sprites.elfBack(game.s.look), 124.dp) }
        // coins flying across the counter
        if (coinT < 1f) for (i in 0 until 5) {
            val k = (coinT * 1.4f - i * 0.1f).coerceIn(0f, 1f)
            val from = Offset(150f, 150f); val to = Offset(100f, 215f)
            val a = if (toYou) from else to; val b = if (toYou) to else from
            val p = a + (b - a) * k - Offset(0f, sin(k * Math.PI.toFloat()) * 50f)
            Px(coin, 20.dp, Modifier.offset(p.x.dp, p.y.dp))
        }
    }
}
