package com.lexi.christmastown

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Photo booth: you and your friends, silly props, snap!
// ======================================================================

/** A framed photo [width] wide: the backdrop, you and your friends, each with the silly prop on top. */
@Composable
fun PhotoView(game: GameState, photo: Photo, width: Dp) {
    val looks = listOf(game.s.look) + photo.people.mapNotNull { n -> npcs.firstOrNull { it.name == n }?.look }
    val inner = width - 8.dp
    Column(Modifier.width(width).background(Color.White, RoundedCornerShape(3.dp)).border(2.dp, Pal.ink, RoundedCornerShape(3.dp)).padding(4.dp)) {
        Box(Modifier.size(inner, inner * 0.72f).clip(RoundedCornerShape(2.dp)).background(hex(photoBackdrops[photo.bg % photoBackdrops.size])), contentAlignment = Alignment.BottomCenter) {
            Row(verticalAlignment = Alignment.Bottom) {
                for (look in looks) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Txt(photoProps[photo.prop % photoProps.size], (inner.value * 0.1f).toInt().coerceAtLeast(8))
                    ElfPx(look, inner * (if (looks.size > 3) 0.42f else 0.5f))
                }
            }
        }
    }
}

@Composable
fun PhotoBooth(game: GameState) {
    val friends = remember { mutableStateListOf<String>() }
    var prop by remember { mutableIntStateOf(0) }
    var bg by remember { mutableIntStateOf(0) }
    var snaps by remember { mutableIntStateOf(0) }
    val flash = remember { Animatable(0f) }
    LaunchedEffect(snaps) { if (snaps > 0) { flash.snapTo(1f); flash.animateTo(0f, tween(600)) } }
    val met = npcs.filter { game.hearts(it) > 0 }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            PhotoView(game, Photo(friends.toList(), prop, bg, game.s.day), 300.dp)
            Box(Modifier.size(300.dp, 230.dp).background(Color.White.copy(alpha = flash.value)))
        }
        Column(Modifier.fillMaxWidth().panel().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt("Who's in the photo? (up to 3 friends)", 14, Pal.brown)
            if (met.isEmpty()) Txt("Make some friends first! Chat with elves in Friends 💬", 13)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (n in met) {
                    val on = n.name in friends
                    Column(
                        Modifier.width(64.dp).squish { if (on) friends.remove(n.name) else if (friends.size < 3) friends.add(n.name) else game.say("Only 3 friends fit in the photo booth! 📸") }
                            .panel(if (on) hex(0xFFE1EE) else Color.White, if (on) Pal.pink else Pal.ink, 8.dp).padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { ElfPx(n.look, 44.dp); Txt(n.name, 10, maxLines = 1) }
                }
            }
            Txt("Silly props:", 14, Pal.brown)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                photoProps.forEachIndexed { i, p ->
                    Box(Modifier.size(48.dp).squish { prop = i }.panel(if (prop == i) hex(0xFFE1EE) else Color.White, if (prop == i) Pal.pink else Pal.ink, 8.dp), contentAlignment = Alignment.Center) { Txt(p, 22) }
                }
            }
            Txt("Backdrop:", 14, Pal.brown)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photoBackdrops.forEachIndexed { i, c ->
                    Box(Modifier.size(40.dp).squish { bg = i }.border(3.dp, if (bg == i) Pal.red else Color.Transparent, CircleShape).padding(5.dp).background(hex(c), CircleShape).border(2.dp, Pal.ink, CircleShape))
                }
            }
            Btn("📸 Say cheese! Snap!", Pal.pink, 18, Modifier.fillMaxWidth()) {
                game.takePhoto(Photo(friends.toList(), prop, bg, game.s.day)); snaps++
            }
        }
        if (game.s.photos.isNotEmpty()) {
            Txt("📒 Your photos (the newest one hangs in your living room!)", 14, Color.White)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (p in game.s.photos) PhotoView(game, p, 130.dp)
            }
            Box(Modifier.height(8.dp))
        }
    }
}

