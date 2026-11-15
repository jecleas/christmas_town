package com.lexi.christmastown

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Advent calendar: one surprise door a day, 1–24 December
// ======================================================================

@Composable
fun AdventScreen(game: GameState) {
    var opened by remember { mutableStateOf<Pair<Int, Surprise>?>(null) }
    val day = game.adventDay
    val done = game.adventOpenedThisYear
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt(
            when {
                day == 0 -> "The calendar opens on December 1st! 🎄 ${game.daysToAdvent} sleeps to go!"
                done.containsAll((1..day).toList()) -> "All caught up! Come back tomorrow for another door 🌙"
                else -> "Tap today's door for a surprise! Missed a day? Those doors still open. ✨"
            }, 16, Color.White, align = TextAlign.Center,
        )
        Column(Modifier.panel(hex(0x7A1E2E)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in adventOrder.chunked(4)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (d in row) Door(d, d in done, d <= day && d !in done, d == day) {
                    game.openAdvent(d)?.let { opened = d to it }
                }
            }
        }
        opened?.let { (d, gift) ->
            Column(Modifier.fillMaxWidth().panel(Color.White).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("Door $d: ${gift.title}! 🎁", 18, Pal.red, align = TextAlign.Center)
                Bob(6f, 300) { Px(gift.sprite, 60.dp) }
                Txt(if (gift.sprite == "gift") "It's in your Closet! 👗" else "It's yours! Check your house or snacks.", 14, Pal.brown)
            }
        }
    }
}

@Composable
private fun Door(day: Int, open: Boolean, ready: Boolean, today: Boolean, onOpen: () -> Unit) {
    val gift = adventSurprises[day - 1]
    Box(
        Modifier.size(92.dp, 70.dp).squish(enabled = ready) { onOpen() }
            .panel(if (open) Pal.cream else if (ready) hex(0x3FAF5A) else hex(0x2E7D4A), if (today && !open) hex(0xFFCC4D) else Pal.ink),
        contentAlignment = Alignment.Center,
    ) {
        when {
            open -> Px(gift.sprite, 40.dp)
            ready -> Bob(3f, 500) { Txt("$day", 26, Color.White) }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Txt("$day", 22, Color.White.copy(alpha = 0.6f)); Txt("🔒", 11) }
        }
    }
}

// ======================================================================
// MARK: - Sticker book: collect a sticker for everything you do
// ======================================================================

@Composable
fun StickerScreen(game: GameState) {
    val have = game.s.stickers
    var picked by remember { mutableStateOf<Sticker?>(null) }
    LaunchedEffect(have.size) { game.sawStickers() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt("${have.size} / ${stickers.size} stickers", 20, Color.White)
        // Prizes
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((at, prize) in stickerPrizes) {
                val claimed = at in game.s.prizesClaimed
                val ready = have.size >= at && !claimed
                Column(
                    Modifier.weight(1f).squish(enabled = ready) { game.claimPrize(at) }
                        .panel(if (ready) hex(0xFFF1B8) else Color.White, if (ready) Pal.red else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Px(prize.sprite, 28.dp, alpha = if (claimed) 0.35f else 1f)
                    Txt(if (claimed) "✅" else if (ready) "Claim!" else "$at ⭐", 12, if (ready) Pal.red else Pal.ink)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(Color.White)) {
            Box(Modifier.fillMaxWidth(have.size / stickers.size.toFloat()).height(14.dp).background(hex(0xFFCC4D)))
        }
        picked?.let { st ->
            Txt((if (st.id in have) "⭐ " else "🔒 ") + st.title, 15, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().panel(Color.White).padding(8.dp))
        }
        Column(Modifier.fillMaxWidth().weight(1f).panel(hex(0xFFF6DD)).verticalScroll(rememberScrollState()).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in stickers.chunked(6)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (st in row) {
                    val got = st.id in have
                    Box(
                        Modifier.size(58.dp).squish { picked = st; Audio.play(Sfx.Tap) }
                            .panel(if (got) Color.White else hex(0xE7DCC4), if (got) hex(0xFFCC4D) else hex(0xC9B994), 29.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (st.id.startsWith("deer:")) Box(Modifier.graphicsLayer { alpha = if (got) 1f else 0.18f }) { ReindeerPx(st.id.removePrefix("deer:"), emptySet(), 26.dp) }
                        else Px(st.sprite, 34.dp, alpha = if (got) 1f else 0.18f)
                        if (!got) Txt("?", 18, Pal.brown.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
