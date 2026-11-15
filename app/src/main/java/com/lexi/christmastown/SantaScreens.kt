package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.sin

// MARK: - Santa's naughty / nice list

@Composable
fun SantaListScreen(game: GameState) {
    var feedback by remember { mutableStateOf<String?>(null) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 900) { Px("santa", 150.dp) }
            Bubble(
                when {
                    game.level < 2 -> "Ho ho ho! You can help with my list at Level 2. Keep working hard!"
                    else -> "Ho ho ho! Read what each kid did. Naughty or nice?"
                }, 210.dp,
            )
        }
        JobChip(game, JobKind.SortList)
        Column(Modifier.fillMaxWidth().panel(hex(0xFFF6DD)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Txt("📜 Santa's List", 24)
            val job = game.s.jobs.firstOrNull { it.kind == JobKind.SortList }
            val kid = game.currentKid
            when {
                job == null -> Txt("Unlocks at Level 2! 🔒", 18)
                kid == null || job.finished -> Txt("Checked it twice! ✅", 20, Pal.darkGreen)
                else -> {
                    Txt(kid.name, 32, Pal.red)
                    Txt("“${kid.deed}”", 20, align = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Btn("😈 Naughty", Pal.ink, 19, Modifier.weight(1f)) {
                            feedback = if (game.sortKid(false)) "Correct! ${kid.name} gets coal 🪨" else "Hmm, ${kid.name} was actually nice! 😇"
                        }
                        Btn("😇 Nice", Pal.green, 19, Modifier.weight(1f)) {
                            feedback = if (game.sortKid(true)) "Correct! ${kid.name} gets a present 🎁" else "Hmm, ${kid.name} was actually naughty! 😈"
                        }
                    }
                }
            }
            feedback?.let { Txt(it, 16, Pal.darkGreen, align = TextAlign.Center) }
        }
    }
}

// MARK: - Sleigh (before take-off)

@Composable
fun SleighScreen(game: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(300.dp, 170.dp), contentAlignment = Alignment.BottomCenter) {
            Bob(5f, 900) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Px("sleigh", 80.dp)
                    Box(Modifier.offset(x = (-12).dp, y = (-30).dp)) { Px("santa", 80.dp) }
                }
            }
        }
        JobChip(game, JobKind.Deliver)
        Column(Modifier.fillMaxWidth().panel(hex(0xDDE7FF)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val job = game.s.jobs.firstOrNull { it.kind == JobKind.Deliver }
            when {
                job == null -> Txt("The sleigh unlocks at Level 3! 🔒", 19, align = TextAlign.Center)
                job.finished -> Txt("All of today's presents are delivered! ✅", 19, Pal.darkGreen, align = TextAlign.Center)
                else -> {
                    Txt("Steer around the clouds and drop presents down the chimneys of the nice houses!", 17, align = TextAlign.Center)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (r in game.sleighTeam) ReindeerPx(r, game.decosOn(r), 34.dp, flip = true)
                    }
                    Txt("Sleigh team: ${game.teamPower}/${game.sleighTeam.size} happy (fed & brushed). Change the team in the Stable 🦌", 13, Pal.brown, align = TextAlign.Center)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Px("gift", 30.dp); Txt("You have ${game.presents} presents to give" + if (game.s.wrapped > 0) " (${game.s.wrapped} wrapped)" else "", 16)
                    }
                    if (game.presents == 0) Txt("Make toys in the Workshop first! 🧸", 15, Pal.red)
                    Btn("🛷 Fly! (⚡10)", Pal.red, 22, Modifier.width(220.dp)) { game.startDelivery() }
                }
            }
        }
    }
}

