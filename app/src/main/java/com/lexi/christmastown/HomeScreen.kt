package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val HOUSE_W = 800f
private const val ROOF_H = 90f
private const val ROOM_H = 230f
private const val ROOM_X = 16f
private const val ROOM_W = HOUSE_W - 32f
private const val PIXEL = 5f   // each sprite pixel is 5 x 5 in the house

private fun roomTop(floor: Int) = ROOF_H + (1 - floor) * ROOM_H   // floor 1 (upstairs) is on top
private fun floorLine(floor: Int) = roomTop(floor) + ROOM_H - 14f

@Composable
fun HomeScreen(game: GameState) {
    var decorating by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Long?>(null) }
    var targetFloor by remember { mutableIntStateOf(0) }

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        House(game, decorating, selected) { selected = it }

        Column(
            Modifier.width(240.dp).height(560.dp).panel().padding(14.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!decorating) {
                Txt("🏠 Home sweet home", 20, align = TextAlign.Center)
                Btn("✏️ Decorate", Pal.blue, 18, Modifier.fillMaxWidth()) { decorating = true }
                Btn("👗 My look", Pal.purple, 18, Modifier.fillMaxWidth()) { game.showCreator = true }
                Txt("😋 Snacks", 18)
                val snacks = Food.entries.filter { game.s.food.n(it) > 0 }
                val cookies = Cookie.entries.filter { game.s.cookies.n(it) > 0 }
                if (snacks.isEmpty() && cookies.isEmpty()) Txt("No snacks. Bake cookies or buy food!", 14, align = TextAlign.Center)
                for (f in snacks) SnackRow(f.sprite, "${f.title} ×${game.s.food.n(f)}", "+${f.energy}⚡") { game.eat(f) }
                for (c in cookies) SnackRow(c.sprite, "${c.title} ×${game.s.cookies.n(c)}", "+${c.energy}⚡") { game.eat(c) }
                Txt(
                    if (game.s.paidToday) "Work's done! Time for bed." else "Jobs not finished: no pay if you sleep now!",
                    14, if (game.s.paidToday) Pal.darkGreen else Pal.red, align = TextAlign.Center,
                )
                Btn("💤 Sleep", Pal.night, 20, Modifier.fillMaxWidth()) { game.sleep() }
            } else {
                Txt("✏️ Decorating", 20)
                Txt("Drag things to move them. Tap one to pick it.", 14, align = TextAlign.Center)
                val sel = game.s.placed.firstOrNull { it.id == selected }
                if (sel != null) {
                    Column(Modifier.fillMaxWidth().panel(hex(0xFFF1B8)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Txt(sel.item.title, 17)
                        Btn(if (sel.floor == 0) "⬆ Move upstairs" else "⬇ Move downstairs", Pal.blue, 15, Modifier.fillMaxWidth()) { game.switchFloor(sel.id) }
                        Btn("📦 Put away", Pal.brown, 15, Modifier.fillMaxWidth()) { game.putAway(sel.id); selected = null }
                    }
                }
                Txt("Your storage", 17)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((f, label) in listOf(0 to "Downstairs", 1 to "Upstairs")) {
                        Txt(label, 13, if (targetFloor == f) Color.White else Pal.ink,
                            modifier = Modifier.squish { targetFloor = f }.panel(if (targetFloor == f) Pal.green else Color.White).padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
                val stored = Item.entries.filter { game.available(it) > 0 }
                if (stored.isEmpty()) Txt("Everything is out! Buy more at the Shop 🛍️", 14, align = TextAlign.Center)
                for (i in stored) {
                    Row(Modifier.fillMaxWidth().panel(Color.White).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Px(i.sprite, 34.dp) }
                        Txt("${i.title} ×${game.available(i)}", 13, modifier = Modifier.weight(1f))
                        Txt("Place", 13, Color.White, modifier = Modifier.squish { selected = game.place(i, targetFloor) }.background(Pal.green, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
                Btn("✅ Done", Pal.green, 18, Modifier.fillMaxWidth()) { decorating = false; selected = null }
            }
        }
    }
}

@Composable
private fun SnackRow(sprite: String, title: String, energy: String, onEat: () -> Unit) {
    Row(Modifier.fillMaxWidth().panel(Color.White).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Px(sprite, 30.dp)
        Column(Modifier.weight(1f)) { Txt(title, 13); Txt(energy, 12, Pal.darkGreen) }
        Txt("Eat", 13, Color.White, modifier = Modifier.squish(onClick = onEat).background(Pal.red, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun House(game: GameState, decorating: Boolean, selected: Long?, onSelect: (Long?) -> Unit) {
    Box(Modifier.size(HOUSE_W.dp, (ROOF_H + ROOM_H * 2 + 16).dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            fun r(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, Offset(x * u, y * u), Size(w * u, h * u))
            // roof with snow
            drawPath(Path().apply { moveTo(0f, ROOF_H * u); lineTo(HOUSE_W / 2 * u, 0f); lineTo(HOUSE_W * u, ROOF_H * u); close() }, hex(0xA02338))
            drawPath(Path().apply { moveTo(40 * u, (ROOF_H - 18) * u); lineTo(HOUSE_W / 2 * u, 6 * u); lineTo((HOUSE_W - 40) * u, (ROOF_H - 18) * u); lineTo((HOUSE_W - 40) * u, (ROOF_H - 8) * u); lineTo(HOUSE_W / 2 * u, 20 * u); lineTo(40 * u, (ROOF_H - 8) * u); close() }, hex(0xF4F8FF))
            r(hex(0x6B4A3A), HOUSE_W - 190, 10f, 40f, 60f)   // chimney
            r(hex(0xF4F8FF), HOUSE_W - 196, 6f, 52f, 10f)
            // house body
            r(Pal.ink, 0f, ROOF_H, HOUSE_W, ROOM_H * 2 + 16)
            for (f in 0..1) {
                val top = roomTop(f)
                // wallpaper with stripes
                val wall = if (f == 1) hex(0xF6D7E3) else hex(0xE3F0D8)
                val stripe = if (f == 1) hex(0xEFC3D3) else hex(0xD3E6C4)
                r(wall, ROOM_X, top + 8, ROOM_W, ROOM_H - 8)
                var x = ROOM_X
                while (x < ROOM_X + ROOM_W) { r(stripe, x, top + 8, 10f, ROOM_H - 22); x += 40f }
                // window with snowy night
                val wx = if (f == 1) ROOM_X + ROOM_W * 0.42f else ROOM_X + ROOM_W * 0.58f
                r(Pal.ink, wx - 4, top + 40, 88f, 78f)
                r(hex(0x1D2B53), wx, top + 44, 80f, 70f)
                for (i in 0 until 8) r(Color.White, wx + (i * 23) % 76, top + 48 + (i * 31) % 62, 4f, 4f)
                r(hex(0xF4F8FF), wx, top + 106, 80f, 8f)
                r(Pal.ink, wx + 38, top + 44, 4f, 70f)
                // floorboards
                r(hex(0x9A5B34), ROOM_X, top + ROOM_H - 14, ROOM_W, 14f)
                r(hex(0x7A4428), ROOM_X, top + ROOM_H - 14, ROOM_W, 3f)
            }
            // snowy ground
            r(hex(0xF4F8FF), -20f, ROOF_H + ROOM_H * 2 + 8, HOUSE_W + 40, 12f)
        }

        val placed = game.s.placed.sortedWith(compareBy({ it.item.spot != Spot.Rug }, { it.id == selected }))
        for (p in placed) PlacedItem(game, p, decorating, p.id == selected, onSelect)

        if (!decorating) {
            Box(Modifier.offset((ROOM_X + ROOM_W * 0.58f).dp, (floorLine(0) - 22 * 4.5f).dp)) { ElfPx(game.s.look, (24 * 4.5f).dp) }
        }
    }
}

@Composable
private fun PlacedItem(game: GameState, p: Placed, decorating: Boolean, isSelected: Boolean, onSelect: (Long?) -> Unit) {
    val img = Sprites.named(p.item.sprite)
    val w = img.width * PIXEL
    val h = img.height * PIXEL
    val cx = ROOM_X + p.x * ROOM_W
    val top = when (p.item.spot) {
        Spot.Floor -> floorLine(p.floor) - h
        Spot.Rug -> floorLine(p.floor) - h + 10
        Spot.Wall -> roomTop(p.floor) + 16 + p.y * (ROOM_H - 60 - h).coerceAtLeast(10f)
    }
    var mod = Modifier.offset((cx - w / 2).dp, top.dp)
    if (decorating) {
        mod = mod
            .then(if (isSelected) Modifier.border(3.dp, hex(0xFFCC4D), RoundedCornerShape(4.dp)) else Modifier)
            .squish { onSelect(p.id) }
            .pointerInput(p.id) {
                detectDragGestures(onDragStart = { onSelect(p.id) }) { change, drag ->
                    change.consume()
                    val cur = game.s.placed.firstOrNull { it.id == p.id } ?: return@detectDragGestures
                    val wallRange = (ROOM_H - 60 - h).coerceAtLeast(10f)
                    game.move(p.id, cur.x + drag.x / density / ROOM_W, cur.y + drag.y / density / wallRange)
                }
            }
    }
    Box(mod) { Px(img, h.dp) }
}
