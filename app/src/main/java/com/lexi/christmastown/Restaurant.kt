package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ======================================================================
// MARK: - Elf's Restaurant: bring friends, order from Lexi's menu, eat together
// ======================================================================

private val restaurantRed = hex(0xC0392B)

@Composable
fun RestaurantScreen(game: GameState) {
    var eating by remember { mutableStateOf(false) }
    val friends = remember { mutableStateListOf<Npc>() }
    if (eating) {
        Meal(game, friends.toList(), birthday = false) { eating = false; friends.clear() }
        return
    }
    val met = npcs.filter { game.hearts(it) > 0 }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 800) { ElfPx(npcNamed(WAITER).look, 110.dp) }
            Bubble("Welcome to Elf's Restaurant! 🍝 A table for how many? Bring up to 3 friends!", 240.dp)
        }
        Column(Modifier.fillMaxWidth().panel().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Txt("Who's eating with you?", 16, restaurantRed)
            if (met.isEmpty()) Txt("Make friends first, or eat on your own! 😊", 13)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (n in met) {
                    val on = n in friends
                    Column(
                        Modifier.width(66.dp).squish { if (on) friends.remove(n) else if (friends.size < 3) friends.add(n) else game.say("The table fits 3 friends! 🍽️") }
                            .panel(if (on) hex(0xFFE1E1) else Color.White, if (on) restaurantRed else Pal.ink, 8.dp).padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { ElfPx(n.look, 46.dp); Txt(n.name, 10, maxLines = 1) }
                }
            }
        }
        Txt("📜 Menu", 20, Color.White)
        for (d in Dish.entries) Row(Modifier.fillMaxWidth().panel(Color.White).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Px(d.sprite, 34.dp); Txt(d.title, 15, modifier = Modifier.weight(1f)); Txt("${money(d.price)} · +${d.energy}⚡", 13, Pal.brown)
        }
        Btn(if (friends.isEmpty()) "🍽️ Table for one, please!" else "🍽️ Table for ${friends.size + 1}, please!", restaurantRed, 18, Modifier.fillMaxWidth()) { eating = true }
    }
}

/**
 * A meal at the restaurant: order a dish for everyone at the table, then eat together.
 * On your birthday ([birthday]) the food is free, and [onDone] leads to the cake.
 */
@Composable
fun Meal(game: GameState, friends: List<Npc>, birthday: Boolean, onDone: () -> Unit) {
    val people = listOf(game.s.look to "You") + friends.map { it.look to it.name }
    val orders = remember { mutableStateListOf<Dish?>().apply { repeat(people.size) { add(null) } } }
    var who by remember { mutableIntStateOf(0) }
    var ordered by remember { mutableStateOf(false) }
    var bites by remember { mutableIntStateOf(0) }
    val total = orders.sumOf { it?.price ?: 0 }
    val waiter = npcNamed(WAITER)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ElfPx(waiter.look, 64.dp)
            Bubble(
                when {
                    bites >= 3 -> if (birthday) "Now for the BIRTHDAY CAKE! 🎂" else "Clean plates! Come back soon! 😊"
                    ordered -> "Bon appétit! Tap EAT to take a bite! 😋"
                    birthday -> "HAPPY BIRTHDAY! 🎂 Everything is FREE today! What would ${people[who].second.let { if (it == "You") "you" else it }} like?"
                    else -> "What would ${people[who].second.let { if (it == "You") "you" else it }} like? Tap a dish!"
                }, 300.dp,
            )
        }
        Table(people, orders, if (ordered) bites else -1, highlight = if (ordered) -1 else who) { if (!ordered) who = it }
        if (!ordered) {
            Column(Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                for (d in Dish.entries) Row(
                    Modifier.fillMaxWidth().squish {
                        orders[who] = d; Audio.play(Sfx.Pop)
                        val next = orders.indexOfFirst { it == null }
                        if (next >= 0) who = next
                    }.panel(Color.White).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Px(d.sprite, 32.dp); Txt(d.title, 15, modifier = Modifier.weight(1f))
                    Txt(if (birthday) "FREE" else money(d.price), 14, if (birthday) Pal.green else Pal.brown)
                }
            }
            Btn(
                if (birthday) "🎂 Order everything (free!)" else "🧾 Order! ${money(total)}", restaurantRed, 18, Modifier.fillMaxWidth(),
                enabled = orders.all { it != null },
            ) { if (game.orderMeal(total, birthday)) ordered = true }
        } else if (bites < 3) {
            Btn("😋 EAT! (bite ${bites + 1} of 3)", Pal.green, 20, Modifier.width(280.dp)) {
                bites++; Audio.play(Sfx.Munch)
                if (bites == 3) game.finishMeal(friends, orders.first())
            }
        } else {
            Btn(if (birthday) "🎂 Cake time!" else "👋 Thank you, bye!", if (birthday) Pal.pink else restaurantRed, 18, Modifier.width(280.dp), onClick = onDone)
        }
    }
}

/** Everyone sitting at the table, with their plates in front of them. [bites] = -1 while ordering. */
@Composable
private fun Table(people: List<Pair<ElfLook, String>>, orders: List<Dish?>, bites: Int, highlight: Int, onPick: (Int) -> Unit) {
    Box(Modifier.size(406.dp, 230.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRect(hex(0xF6E2C8))
            for (i in 0 until 10) drawRect(hex(0xEFD2B0), Offset((i * 44f) * u, 0f), Size(22 * u, 120 * u))
            // red & white checked tablecloth
            drawRect(Pal.ink, Offset(0f, 132 * u), Size(size.width, 100 * u))
            for (r in 0 until 5) for (c in 0 until 21) drawRect(if ((r + c) % 2 == 0) restaurantRed else Color.White, Offset((c * 20f) * u, (135 + r * 20f) * u), Size(20 * u, 20 * u))
        }
        Row(Modifier.align(Alignment.TopCenter).padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            people.forEachIndexed { i, (look, name) ->
                Column(Modifier.width(66.dp).squish { onPick(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Txt(name, 11, if (i == highlight) Color.White else Pal.ink, maxLines = 1,
                        modifier = Modifier.panel(if (i == highlight) restaurantRed else Color.White, radius = 5.dp).padding(horizontal = 4.dp))
                    if (bites in 0..2 && orders[i] != null) Bob(2f, 300 + i * 40) { ElfPx(look, 96.dp) } else ElfPx(look, 96.dp)
                    Box(Modifier.height(54.dp), contentAlignment = Alignment.Center) {
                        val d = orders[i]
                        when {
                            d == null -> Txt("🍽️", 24)
                            bites >= 3 -> Txt("✨", 22)
                            else -> Px(d.sprite, 40.dp, alpha = 1f - 0.28f * bites.coerceAtLeast(0))
                        }
                    }
                }
            }
        }
    }
}

