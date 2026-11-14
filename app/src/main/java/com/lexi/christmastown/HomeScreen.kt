package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// MARK: - Main screen (your living room)

@Composable
fun HubScreen(game: GameState) {
    var confirmSleep by remember { mutableStateOf(false) }
    val s = game.s
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Profile card + money, like the reference game
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.weight(1f).squish { game.showCreator = true }.panel().padding(6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(hex(0xBFE9F5)), contentAlignment = Alignment.TopCenter) {
                    ElfPx(s.look, 66.dp, Modifier.offset(y = (-2).dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Txt("${s.name} ✏️", 18, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Tag("Day ${s.day}", Pal.night, 12)
                        Tag("Lv ${game.level}", Pal.green, 12)
                    }
                    val frac = if (s.paidToday) 1f else game.jobsDone / s.jobs.size.toFloat()
                    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(3.dp)).background(hex(0xD5DBE6))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(frac).background(if (s.paidToday) Pal.green else hex(0xFFCC4D)))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MoneyChip(game)
                EnergyChip(game)
            }
        }

        // Living room with the activity icons down both sides
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
            LivingRoom(game)
            Column(Modifier.align(Alignment.TopStart).padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (p in Place.entries.filter { it.side == Side.Left }) Icon(game, p)
            }
            Column(Modifier.align(Alignment.TopEnd).padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (p in Place.entries.filter { it.side == Side.Right }) Icon(game, p)
            }
        }

        // Two big buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(
                if (s.paidToday) "📋 Jobs ✅" else "📋 Jobs ${game.jobsDone}/${s.jobs.size}",
                if (s.paidToday) Pal.green else Pal.red, 20, Modifier.weight(1f),
            ) { game.showJobs = true }
            Btn("💤 Sleep", Pal.night, 20, Modifier.weight(1f)) { if (s.paidToday) game.sleep() else confirmSleep = true }
        }
    }

    if (confirmSleep) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Column(Modifier.width(360.dp).panel().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Txt("😴 Go to sleep?", 24)
                Txt("You haven't finished Santa's jobs yet, so you WON'T get paid today.", 16, Pal.red, align = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Btn("Keep working", Pal.green, 16, Modifier.weight(1f)) { confirmSleep = false }
                    Btn("Sleep anyway", Pal.night, 16, Modifier.weight(1f)) { confirmSleep = false; game.sleep() }
                }
            }
        }
    }
}

/** What the little badge on each icon says: "!" = something to do, "✓" = done, "🔒" = locked. */
private fun badge(game: GameState, p: Place): String? {
    val s = game.s
    fun job(k: JobKind) = s.jobs.firstOrNull { it.kind == k }
    fun jobBadge(k: JobKind, unlock: Int): String? = when {
        game.level < unlock -> "🔒"
        s.paidToday || job(k)?.finished == true -> "✓"
        else -> "!"
    }
    return when (p) {
        Place.Greenhouse -> if (s.plots.any { it.stage(game.now) == 3 || it.stage(game.now) == 1 }) "!" else null
        Place.Bakery -> jobBadge(JobKind.Bake, 1)
        Place.Stable -> jobBadge(JobKind.Reindeer, 1)
        Place.Workshop -> jobBadge(JobKind.Toys, 1)
        Place.SantaList -> jobBadge(JobKind.SortList, 2)
        Place.Sleigh -> jobBadge(JobKind.Deliver, 3)
        Place.Decorate -> if (Item.entries.any { game.available(it) > 0 }) "New" else null
        Place.Friends -> if (npcs.any { it.name !in s.talkedToday }) "!" else null
        else -> null
    }
}

@Composable
private fun Icon(game: GameState, p: Place) {
    val b = badge(game, p)
    Column(
        Modifier.width(76.dp).squish { if (p == Place.Closet) game.showCreator = true else game.go(p) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Box(Modifier.size(62.dp).panel(Color.White.copy(alpha = 0.92f), p.color, 14.dp), contentAlignment = Alignment.Center) {
                when (p) {
                    Place.Closet -> Box(Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.TopCenter) { ElfPx(game.s.look, 60.dp) }
                    Place.Friends -> Box(Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.TopCenter) { ElfPx(npcs[1].look, 60.dp) }
                    Place.Stable -> Px(p.sprite, 34.dp)
                    Place.Sleigh -> Px(p.sprite, 28.dp)
                    Place.SantaList -> Px(p.sprite, 50.dp)
                    else -> Px(p.sprite, 42.dp)
                }
            }
            if (b != null) {
                val color = when (b) { "✓" -> Pal.green; "🔒" -> hex(0x8D96A8); else -> Pal.red }
                Txt(b, 12, Color.White, modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp).background(color, RoundedCornerShape(8.dp)).padding(horizontal = 5.dp, vertical = 1.dp))
            }
        }
        Txt(p.title, 12, Color.White, align = TextAlign.Center, maxLines = 1,
            modifier = Modifier.padding(top = 2.dp).background(Pal.ink.copy(alpha = 0.75f), RoundedCornerShape(5.dp)).padding(horizontal = 4.dp))
    }
}

/** Your downstairs room, big: the decorations you placed, your elf, a reindeer buddy, and a table of goodies. */
@Composable
private fun LivingRoom(game: GameState) {
    val s = game.s
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = maxWidth.value
        val h = maxHeight.value
        val floorY = h * 0.66f
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRect(hex(0xE3F0D8))
            var x = 0f
            while (x < w) { drawRect(hex(0xD3E6C4), Offset(x * u, 0f), Size(12 * u, floorY * u)); x += 44f }
            // window with falling snow
            val wx = w / 2 - 60; val wy = 30f
            drawRect(Pal.ink, Offset((wx - 5) * u, (wy - 5) * u), Size(130 * u, 110 * u))
            drawRect(hex(0x1D2B53), Offset(wx * u, wy * u), Size(120 * u, 100 * u))
            for (i in 0 until 12) {
                val sy = (wy + ((i * 31 + game.now / 40) % 96)) * u
                drawRect(Color.White, Offset((wx + (i * 37) % 114) * u, sy), Size(4 * u, 4 * u))
            }
            drawRect(Color.White, Offset(wx * u, (wy + 88) * u), Size(120 * u, 12 * u))
            drawRect(Pal.ink, Offset((wx + 57) * u, wy * u), Size(6 * u, 100 * u))
            // wooden floor
            drawRect(hex(0x9A5B34), Offset(0f, floorY * u), Size(size.width, size.height))
            var y = floorY + 18
            while (y < h) { drawRect(hex(0x7A4428), Offset(0f, y * u), Size(size.width, 3 * u)); y += 22 }
        }

        val pixel = 3.2f
        val items = s.placed.filter { it.floor == 0 }.sortedBy { it.item.spot != Spot.Rug }
        for (p in items) {
            val img = Sprites.named(p.item.sprite)
            val iw = img.width * pixel
            val ih = img.height * pixel
            val cx = 14 + p.x * (w - 28)
            val top = when (p.item.spot) {
                Spot.Floor -> floorY - ih + 6
                Spot.Rug -> floorY + 8
                Spot.Wall -> 20 + p.y * (floorY - 150 - ih).coerceAtLeast(10f)
            }
            Box(Modifier.offset((cx - iw / 2).dp, top.dp)) { Px(img, ih.dp) }
        }

        // Your elf and a baby reindeer buddy
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = (h - floorY - 70).coerceAtLeast(0f).dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Bob(4f, 900) { ElfPx(s.look, 200.dp) }
                Bob(3f, 650) { Px(Sprites.bitmap("reindeer|buddy", Sprites.rows("reindeer"), mapOf('N' to Pal.red)), 54.dp) }
            }
        }

        // Table of today's goodies
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val goodies = Cookie.entries.flatMap { c -> List(minOf(s.cookies.n(c), 3)) { c.sprite } } +
                Toy.entries.flatMap { t -> List(minOf(s.toys.n(t), 2)) { t.sprite } }
            Row(Modifier.height(38.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (goodies.isEmpty()) Txt("Bake cookies & make toys!", 13, Color.White, modifier = Modifier.background(Pal.ink.copy(alpha = 0.6f), RoundedCornerShape(5.dp)).padding(horizontal = 6.dp))
                for (g in goodies.take(7)) Px(g, 34.dp)
            }
            Box(Modifier.size(250.dp, 16.dp).panel(hex(0xC98B5E), radius = 5.dp))
        }
    }
}

// MARK: - Decorate (the two-story house)

private const val HOUSE_W = 800f
private const val ROOF_H = 90f
private const val ROOM_H = 230f
private const val ROOM_X = 16f
private const val ROOM_W = HOUSE_W - 32f
private const val PIXEL = 5f
private const val HOUSE_H = ROOF_H + ROOM_H * 2 + 16

private fun roomTop(floor: Int) = ROOF_H + (1 - floor) * ROOM_H   // floor 1 (upstairs) is on top
private fun floorLine(floor: Int) = roomTop(floor) + ROOM_H - 14f

@Composable
fun DecorateScreen(game: GameState) {
    var selected by remember { mutableStateOf<Long?>(null) }
    var targetFloor by remember { mutableIntStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val k = 406f / HOUSE_W
        Box(Modifier.size((HOUSE_W * k).dp, (HOUSE_H * k).dp), contentAlignment = Alignment.Center) {
            Box(Modifier.requiredSize(HOUSE_W.dp, HOUSE_H.dp).graphicsLayer { scaleX = k; scaleY = k }) {
                House(game, selected) { selected = it }
            }
        }
        Txt("Drag things to move them. Tap one to pick it.", 14, Color.White)

        Column(Modifier.fillMaxWidth().weight(1f).panel().verticalScroll(rememberScrollState()).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val sel = game.s.placed.firstOrNull { it.id == selected }
            if (sel != null) {
                Row(Modifier.fillMaxWidth().panel(hex(0xFFF1B8)).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Txt(sel.item.title, 16, modifier = Modifier.weight(1f))
                    Btn(if (sel.floor == 0) "⬆ Up" else "⬇ Down", Pal.blue, 14, Modifier.width(90.dp)) { game.switchFloor(sel.id) }
                    Btn("📦 Away", Pal.brown, 14, Modifier.width(100.dp)) { game.putAway(sel.id); selected = null }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Txt("Storage. Place on:", 16)
                for ((f, label) in listOf(0 to "Downstairs", 1 to "Upstairs")) {
                    Txt(label, 14, if (targetFloor == f) Color.White else Pal.ink,
                        modifier = Modifier.squish { targetFloor = f }.panel(if (targetFloor == f) Pal.green else Color.White).padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            val stored = Item.entries.filter { game.available(it) > 0 }
            if (stored.isEmpty()) Txt("Everything is out! Buy more in the Shop 🛍️", 14)
            for (i in stored) {
                Row(Modifier.fillMaxWidth().panel(Color.White).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Px(i.sprite, 38.dp) }
                    Txt("${i.title} ×${game.available(i)}", 15, modifier = Modifier.weight(1f))
                    Btn("Place", Pal.green, 14, Modifier.width(80.dp)) { selected = game.place(i, targetFloor) }
                }
            }
        }
    }
}

@Composable
private fun House(game: GameState, selected: Long?, onSelect: (Long?) -> Unit) {
    Box(Modifier.size(HOUSE_W.dp, HOUSE_H.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            fun r(c: Color, x: Float, y: Float, w: Float, h: Float) = drawRect(c, Offset(x * u, y * u), Size(w * u, h * u))
            drawPath(Path().apply { moveTo(0f, ROOF_H * u); lineTo(HOUSE_W / 2 * u, 0f); lineTo(HOUSE_W * u, ROOF_H * u); close() }, hex(0xA02338))
            drawPath(Path().apply { moveTo(40 * u, (ROOF_H - 18) * u); lineTo(HOUSE_W / 2 * u, 6 * u); lineTo((HOUSE_W - 40) * u, (ROOF_H - 18) * u); lineTo((HOUSE_W - 40) * u, (ROOF_H - 8) * u); lineTo(HOUSE_W / 2 * u, 20 * u); lineTo(40 * u, (ROOF_H - 8) * u); close() }, hex(0xF4F8FF))
            r(hex(0x6B4A3A), HOUSE_W - 190, 10f, 40f, 60f)
            r(hex(0xF4F8FF), HOUSE_W - 196, 6f, 52f, 10f)
            r(Pal.ink, 0f, ROOF_H, HOUSE_W, ROOM_H * 2 + 16)
            for (f in 0..1) {
                val top = roomTop(f)
                val wall = if (f == 1) hex(0xF6D7E3) else hex(0xE3F0D8)
                val stripe = if (f == 1) hex(0xEFC3D3) else hex(0xD3E6C4)
                r(wall, ROOM_X, top + 8, ROOM_W, ROOM_H - 8)
                var x = ROOM_X
                while (x < ROOM_X + ROOM_W) { r(stripe, x, top + 8, 10f, ROOM_H - 22); x += 40f }
                val wx = if (f == 1) ROOM_X + ROOM_W * 0.42f else ROOM_X + ROOM_W * 0.58f
                r(Pal.ink, wx - 4, top + 40, 88f, 78f)
                r(hex(0x1D2B53), wx, top + 44, 80f, 70f)
                for (i in 0 until 8) r(Color.White, wx + (i * 23) % 76, top + 48 + (i * 31) % 62, 4f, 4f)
                r(hex(0xF4F8FF), wx, top + 106, 80f, 8f)
                r(Pal.ink, wx + 38, top + 44, 4f, 70f)
                r(hex(0x9A5B34), ROOM_X, top + ROOM_H - 14, ROOM_W, 14f)
                r(hex(0x7A4428), ROOM_X, top + ROOM_H - 14, ROOM_W, 3f)
            }
            r(hex(0xF4F8FF), -20f, ROOF_H + ROOM_H * 2 + 8, HOUSE_W + 40, 12f)
        }
        val placed = game.s.placed.sortedWith(compareBy({ it.item.spot != Spot.Rug }, { it.id == selected }))
        for (p in placed) PlacedItem(game, p, p.id == selected, onSelect)
    }
}

@Composable
private fun PlacedItem(game: GameState, p: Placed, isSelected: Boolean, onSelect: (Long?) -> Unit) {
    val img = Sprites.named(p.item.sprite)
    val w = img.width * PIXEL
    val h = img.height * PIXEL
    val cx = ROOM_X + p.x * ROOM_W
    val top = when (p.item.spot) {
        Spot.Floor -> floorLine(p.floor) - h
        Spot.Rug -> floorLine(p.floor) - h + 10
        Spot.Wall -> roomTop(p.floor) + 16 + p.y * (ROOM_H - 60 - h).coerceAtLeast(10f)
    }
    Box(
        Modifier.offset((cx - w / 2).dp, top.dp)
            .then(if (isSelected) Modifier.border(4.dp, hex(0xFFCC4D), RoundedCornerShape(4.dp)) else Modifier)
            .squish { onSelect(p.id) }
            .pointerInput(p.id) {
                detectDragGestures(onDragStart = { onSelect(p.id) }) { change, drag ->
                    change.consume()
                    val cur = game.s.placed.firstOrNull { it.id == p.id } ?: return@detectDragGestures
                    val wallRange = (ROOM_H - 60 - h).coerceAtLeast(10f)
                    game.move(p.id, cur.x + drag.x / density / ROOM_W, cur.y + drag.y / density / wallRange)
                }
            },
    ) { Px(img, h.dp) }
}

// MARK: - Snacks

@Composable
fun SnacksScreen(game: GameState) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt("Snacks give you energy ⚡ for working.", 17, Color.White, align = TextAlign.Center)
        val snacks = Food.entries.filter { game.s.food.n(it) > 0 }
        val cookies = Cookie.entries.filter { game.s.cookies.n(it) > 0 }
        if (snacks.isEmpty() && cookies.isEmpty()) {
            Column(Modifier.fillMaxWidth().panel().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("No snacks! 😮", 20)
                Txt("Bake cookies in the Bakery, or buy food in the Shop.", 15, align = TextAlign.Center)
            }
        }
        for (f in snacks) SnackRow(f.sprite, "${f.title} ×${game.s.food.n(f)}", "+${f.energy} energy") { game.eat(f) }
        for (c in cookies) SnackRow(c.sprite, "${c.title} ×${game.s.cookies.n(c)}", "+${c.energy} energy (but Santa needs cookies too!)") { game.eat(c) }
    }
}

@Composable
private fun SnackRow(sprite: String, title: String, energy: String, onEat: () -> Unit) {
    Row(Modifier.fillMaxWidth().panel(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Px(sprite, 44.dp)
        Column(Modifier.weight(1f)) { Txt(title, 17); Txt(energy, 13, Pal.darkGreen) }
        Btn("Eat", Pal.red, 16, Modifier.width(80.dp), onClick = onEat)
    }
}
