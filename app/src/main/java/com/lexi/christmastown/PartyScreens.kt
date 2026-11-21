package com.lexi.christmastown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// ======================================================================
// MARK: - Party Place: juice bar, party games, friends and Christmas movies.
// Opens at Level 7.
// ======================================================================

private val partyPink = hex(0xFF5FA2)
private val balloonColors = listOf(hex(0xE5394B), hex(0x3D6FD6), hex(0x3FAF5A), hex(0x9B6BE0), hex(0xFF8FB0), hex(0xFF9A3C))

private enum class PartyTab(val title: String) { Juice("🧃Juice"), Games("🎈Games"), Friends("💬Chat"), Movies("🎬Movie"), Dance("💃Dance"), Photo("📸Photo") }

@Composable
fun PartyScreen(game: GameState) {
    var tab by remember { mutableStateOf(PartyTab.Juice) }
    var busy by remember { mutableStateOf(false) }   // playing a game or watching a movie: hide the tabs
    val brought = remember { mutableStateListOf<Npc>() }   // friends you came out of the movie with
    if (!game.partyOpen) {
        LockedParty(game)
    } else {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!busy) Tabs(PartyTab.entries, tab, { it.title }, partyPink, size = 12) { tab = it }
            when (tab) {
                PartyTab.Juice -> JuiceBar(game)
                PartyTab.Games -> PartyGames(game) { busy = it }
                PartyTab.Friends -> PartyFriends(game)
                PartyTab.Movies -> Cinema(game, { busy = it }) { fs -> brought.clear(); brought.addAll(fs); busy = false; tab = PartyTab.Dance }
                PartyTab.Dance -> DanceFloor(game, brought)
                PartyTab.Photo -> PhotoBooth(game)
            }
        }
    }
}

@Composable
private fun LockedParty(game: GameState) {
    Column(Modifier.fillMaxWidth().panel().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Bob(4f, 900) { Px("balloon", 60.dp) }
            Bob(4f, 700) { Px("juice_punch", 50.dp) }
            Bob(4f, 800) { Px("popcorn", 50.dp) }
            Bob(4f, 1000) { Px("balloon", 60.dp) }
        }
        Txt("🔒 The Party Place opens at Level $PARTY_LEVEL!", 22, Pal.red, align = TextAlign.Center)
        Txt("You're Level ${game.level} right now. Finish ALL of Santa's jobs each day to level up!", 16, align = TextAlign.Center)
        Txt("Then come for fruit juice 🧃, party games 🎈, friends 💬 and Christmas movies 🎬", 15, Pal.brown, align = TextAlign.Center)
    }
}

// MARK: Juice bar

@Composable
private fun JuiceBar(game: GameState) {
    val barElf = npcs.first { it.name == "Sprout" }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 800) { ElfPx(barElf.look, 110.dp) }
            Bubble("Welcome to the juice bar! 🍊🍎🍓🍇 What would you like?", 230.dp)
        }
        for (j in Juice.entries) {
            Row(Modifier.fillMaxWidth().panel(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Px(j.sprite, 46.dp)
                Column(Modifier.weight(1f)) { Txt(j.title, 17); Txt("+${j.energy} energy ⚡", 13, Pal.darkGreen) }
                Btn("Drink ${money(j.price)}", partyPink, 15, Modifier.width(140.dp)) { game.drink(j) }
            }
        }
    }
}

// MARK: Friends at the party

private fun hearts(n: Int) = "💖".repeat(n) + "🤍".repeat((5 - n).coerceAtLeast(0))

@Composable
private fun PartyFriends(game: GameState) {
    val guests = game.partyGuests
    var talking by remember { mutableStateOf(guests.first()) }
    var line by remember { mutableIntStateOf(Random.nextInt(partyLines.size)) }
    var sharing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt("Who came to the party today? Tap a friend!", 17, Color.White, align = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for ((i, npc) in guests.withIndex()) {
                val sel = npc == talking
                Column(
                    Modifier.width(96.dp).squish { talking = npc; sharing = false; line++; game.chat(npc) }
                        .panel(if (sel) hex(0xFFF1B8) else Pal.cream, if (sel) partyPink else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Bob(6f, 380 + i * 70) { ElfPx(npc.look, 80.dp) }   // dancing!
                    Txt(npc.name, 15)
                    Txt(hearts(game.hearts(npc)), 8)
                }
            }
        }
        val npc = talking
        Column(Modifier.fillMaxWidth().panel().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Bob(5f, 450) { ElfPx(npc.look, 110.dp) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Txt(npc.name, 26, partyPink)
                    Txt(hearts(game.hearts(npc)), 15)
                    if (npc.name in game.s.juiceShared) Tag("Has a juice 🧃", Pal.green, 13)
                }
            }
            if (sharing) {
                Txt("Pick a juice for ${npc.name}:", 17)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (j in Juice.entries) Column(
                        Modifier.width(58.dp).squish { game.shareJuice(npc, j); sharing = false }.panel(Color.White).padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) { Px(j.sprite, 36.dp); Txt(money(j.price), 12) }
                }
            } else {
                Txt("“${partyLines[line % partyLines.size]}”", 19, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().panel(Color.White).padding(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn("💬 Chat", Pal.blue, 16, Modifier.weight(1f)) { line++; sharing = false; game.chat(npc); Audio.play(Sfx.Message) }
                Btn("🧃 Share a juice", partyPink, 16, Modifier.weight(1f), enabled = npc.name !in game.s.juiceShared) { sharing = !sharing }
            }
            Txt("Sharing a juice makes you better friends (+1 💖, once a day each).", 13, Pal.brown, align = TextAlign.Center)
        }
    }
}

// MARK: Party games

private enum class PartyGame { BalloonPop, Memory }

@Composable
private fun PartyGames(game: GameState, setBusy: (Boolean) -> Unit) {
    var which by remember { mutableStateOf<PartyGame?>(null) }
    when (which) {
        null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Txt("Pick a party game!", 20, Color.White)
            GameCard("balloon", "🎈 Balloon Pop", "Pop as many balloons as you can in 20 seconds! Gold ones are worth 3.\nBest: ${game.s.balloonBest}") { which = PartyGame.BalloonPop }
            GameCard("juice_punch", "🃏 Memory Match", "Flip the cards and find all the matching pairs!") { which = PartyGame.Memory }
        }
        PartyGame.BalloonPop -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BalloonPop(game, setBusy) { which = null }
        }
        PartyGame.Memory -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MemoryMatch(game) { which = null }
        }
    }
}

@Composable
private fun GameCard(sprite: String, title: String, info: String, onPlay: () -> Unit) {
    Row(Modifier.fillMaxWidth().squish(onClick = onPlay).panel().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Bob(4f, 800) { Px(sprite, 70.dp) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Txt(title, 21, partyPink)
            Txt(info, 14)
        }
        Txt("▶", 26, Pal.green)
    }
}

private const val BALLOON_SECONDS = 20f
private const val POP_W = 406f
private const val POP_H = 520f

private class Balloon(val x: Float, val born: Float, val speed: Float, val color: Int, val gold: Boolean, val wobble: Float) {
    fun y(t: Float) = POP_H + 40f - (t - born) * speed
    fun cx(t: Float) = x + sin((t - born) * 2f + wobble) * 14f
}
private class Burst(val x: Float, val y: Float, val at: Float, val points: Int)

@Composable
private fun BalloonPop(game: GameState, setBusy: (Boolean) -> Unit, onExit: () -> Unit) {
    GameSong(game, Music.JingleBells)
    var running by remember { mutableStateOf(false) }
    var over by remember { mutableStateOf(false) }
    var t by remember { mutableFloatStateOf(0f) }
    var popped by remember { mutableIntStateOf(0) }
    var nextSpawn by remember { mutableFloatStateOf(0f) }
    val balloons = remember { mutableStateListOf<Balloon>() }
    val bursts = remember { mutableStateListOf<Burst>() }
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (running) {
            withFrameMillis { now ->
                t = (now - start) / 1000f
                if (t > nextSpawn) {
                    val faster = t / BALLOON_SECONDS
                    balloons.add(Balloon(40f + Random.nextFloat() * (POP_W - 80f), t, 90f + Random.nextFloat() * 70f + faster * 80f, Random.nextInt(balloonColors.size), Random.nextFloat() < 0.12f, Random.nextFloat() * 6f))
                    nextSpawn = t + 0.55f - faster * 0.25f
                }
                balloons.removeAll { it.y(t) < -60f }
                bursts.removeAll { t - it.at > 0.6f }
                frame++
                if (t >= BALLOON_SECONDS) { running = false; over = true; setBusy(false); game.balloonsDone(popped) }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!running) Btn("← Games", Pal.red, 15, Modifier.width(110.dp)) { onExit() }
        Tag("🎈 Popped: $popped", partyPink, 16)
        if (running) Tag("⏱ ${(BALLOON_SECONDS - t).toInt().coerceAtLeast(0)}", Pal.red, 16)
        Tag("🏆 ${game.s.balloonBest}", Pal.purple, 16)
    }
    Box(
        Modifier.size(POP_W.dp, POP_H.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures { o ->
                    if (!running) return@detectTapGestures
                    val x = o.x / density; val y = o.y / density
                    val hit = balloons.lastOrNull { b -> (Offset(b.cx(t), b.y(t)) - Offset(x, y)).getDistance() < 38f }
                    if (hit != null) {
                        val pts = if (hit.gold) 3 else 1
                        balloons.remove(hit); popped += pts
                        bursts.add(Burst(hit.cx(t), hit.y(t), t, pts))
                        Audio.play(if (hit.gold) Sfx.Ding else Sfx.Pop)
                    }
                }
            },
    ) {
        frame.let { }   // redraw every frame
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRect(Brush.verticalGradient(listOf(hex(0x3A1F5C), hex(0x7A2E6E))))
            // disco sparkles
            for (i in 0 until 24) {
                val a = 0.3f + 0.3f * sin(t * 3f + i)
                drawRect(balloonColors[i % balloonColors.size].copy(alpha = a.coerceIn(0f, 1f)), Offset(((i * 71) % 400) * u, ((i * 43) % 300) * u), Size(4 * u, 4 * u))
            }
            for (b in balloons) {
                val c = Offset(b.cx(t) * u, b.y(t) * u)
                val body = if (b.gold) Pal.gold else balloonColors[b.color]
                drawLine(Pal.ink, c + Offset(0f, 34 * u), c + Offset(sin(t * 4f + b.wobble) * 6 * u, 70 * u), 2 * u)
                drawOval(Pal.ink, c - Offset(28 * u, 34 * u), Size(56 * u, 68 * u))
                drawOval(body, c - Offset(25 * u, 31 * u), Size(50 * u, 62 * u))
                drawOval(Color.White.copy(alpha = 0.6f), c - Offset(16 * u, 22 * u), Size(10 * u, 16 * u))
                drawRect(Pal.ink, c + Offset(-4 * u, 32 * u), Size(8 * u, 5 * u))
                if (b.gold) drawCircle(Color.White, 3 * u, c + Offset(10 * u, -12 * u))
            }
            for (p in bursts) {
                val k = (t - p.at) / 0.6f
                for (i in 0 until 8) {
                    val ang = i * Math.PI.toFloat() / 4f
                    val d = (10 + 40 * k) * u
                    drawRect(balloonColors[i % balloonColors.size].copy(alpha = 1f - k), Offset(p.x * u + kotlin.math.cos(ang) * d, p.y * u + sin(ang) * d), Size(6 * u, 6 * u))
                }
            }
        }
        for (p in bursts) Txt("+${p.points}", 22, if (p.points > 1) Pal.gold else Color.White, modifier = Modifier.offset((p.x - 14).dp, (p.y - 30 - (t - p.at) * 40).dp))
        if (!running) Column(Modifier.align(Alignment.Center).width(320.dp).panel().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (over) {
                Txt(if (popped >= 25) "BALLOON CHAMPION! 🏆" else if (popped >= 10) "Pop pop hooray! 🎈" else "Nice popping! 🎈", 21, partyPink, align = TextAlign.Center)
                Txt("You popped $popped points of balloons!", 15, align = TextAlign.Center)
            } else {
                Bob(4f, 700) { Px("balloon", 70.dp) }
                Txt("Tap the balloons to pop them before they float away!", 16, align = TextAlign.Center)
                Txt("Gold balloons = 3 points ✨", 14, Pal.brown, align = TextAlign.Center)
            }
            Btn(if (over) "Play again! (⚡2)" else "Let's go! (⚡2)", partyPink, 18, Modifier.width(220.dp)) {
                if (game.spend(2)) {
                    popped = 0; t = 0f; nextSpawn = 0f; balloons.clear(); bursts.clear()
                    over = false; running = true; setBusy(true)
                }
            }
        }
    }
}

// Memory Match: 12 cards, 6 pairs of party pictures.

private val memoryPictures = listOf("juice_orange", "juice_grape", "popcorn", "balloon", "cookie_ginger", "candycane", "snowman", "gift")

@Composable
private fun MemoryMatch(game: GameState, onExit: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var cards by remember { mutableStateOf(emptyList<String>()) }
    val matched = remember { mutableStateListOf<Int>() }
    val open = remember { mutableStateListOf<Int>() }
    var moves by remember { mutableIntStateOf(0) }

    fun deal() {
        cards = memoryPictures.shuffled().take(6).flatMap { listOf(it, it) }.shuffled()
        matched.clear(); open.clear(); moves = 0
    }

    // Two cards face up: keep them if they match, otherwise flip them back.
    LaunchedEffect(open.toList()) {
        if (open.size < 2) return@LaunchedEffect
        val (a, b) = open[0] to open[1]
        moves++
        if (cards[a] == cards[b]) {
            delay(250); matched.add(a); matched.add(b); open.clear(); Audio.play(Sfx.Ding)
            if (matched.size == cards.size) { game.sticker("memory"); game.say("You found them all in $moves tries! 🎉") }
        } else {
            delay(800); open.clear()
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Btn("← Games", Pal.red, 15, Modifier.width(110.dp)) { onExit() }
        if (started) Tag("Tries: $moves", partyPink, 16)
        if (started) Tag("Pairs: ${matched.size / 2}/${cards.size / 2}", Pal.green, 16)
    }
    if (!started || (matched.size == cards.size && cards.isNotEmpty())) {
        Column(Modifier.width(340.dp).panel().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (started) {
                Txt("ALL MATCHED! 🎉", 24, partyPink)
                Txt("You did it in $moves tries.", 16)
            } else {
                Row { Px("juice_punch", 50.dp); Px("popcorn", 50.dp); Px("balloon", 56.dp) }
                Txt("Flip two cards. If they match, they stay! Find all 6 pairs.", 16, align = TextAlign.Center)
            }
            Btn(if (started) "Play again! (⚡2)" else "Let's play! (⚡2)", partyPink, 18, Modifier.width(220.dp)) {
                if (game.spend(2)) { deal(); started = true }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in cards.indices.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in row) {
                    val faceUp = i in open || i in matched
                    Box(
                        Modifier.size(120.dp, 120.dp)
                            .squish(enabled = !faceUp && open.size < 2) { open.add(i); Audio.play(Sfx.Tap) }
                            .panel(if (i in matched) hex(0xD8F5DD) else if (faceUp) Color.White else partyPink),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (faceUp) Px(cards[i], 70.dp) else Txt("🎉", 40)
                    }
                }
            }
        }
    }
}

// MARK: Movies

private const val SCENE_SECONDS = 5.5f
private const val SCREEN_W = 390f
private const val SCREEN_H = 280f

private enum class CinemaStep(val title: String) { Pick("🎬 Movie"), Friends("👫 Friends"), Seats("💺 Seats"), Snacks("🍿 Snacks"), Watch("") }

/** Who's in your movie group: you first, then the friends you brought, each with a seat. */
private class Moviegoer(val look: ElfLook, val name: String, val seat: Pair<Int, Int>)

@Composable
private fun Cinema(game: GameState, setBusy: (Boolean) -> Unit, onDance: (List<Npc>) -> Unit) {
    var step by remember { mutableStateOf(CinemaStep.Pick) }
    var movie by remember { mutableStateOf(movies.first()) }
    val friends = remember { mutableStateListOf<Npc>() }
    val seats = remember { mutableStateListOf<Pair<Int, Int>>() }
    val order = remember { androidx.compose.runtime.mutableStateMapOf<MovieSnack, Int>() }
    fun go(to: CinemaStep) { step = to; setBusy(to != CinemaStep.Pick) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (step != CinemaStep.Pick && step != CinemaStep.Watch) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("←", Pal.red, 16, Modifier.width(54.dp)) { go(CinemaStep.entries[step.ordinal - 1]) }
                for (s in listOf(CinemaStep.Friends, CinemaStep.Seats, CinemaStep.Snacks)) Tag(s.title, if (s == step) partyPink else Pal.ink.copy(alpha = 0.5f), 14)
            }
            Txt("🎬 ${movie.title}", 17, Color.White, align = TextAlign.Center)
        }
        when (step) {
            CinemaStep.Pick -> MovieList(game) { m -> movie = m; friends.clear(); seats.clear(); order.clear(); go(CinemaStep.Friends) }
            CinemaStep.Friends -> BringFriends(game, movie, friends) { seats.clear(); go(CinemaStep.Seats) }
            CinemaStep.Seats -> PickSeats(game, movie, friends, seats) { if (game.buyTickets(movie, friends.toList())) go(CinemaStep.Snacks) }
            CinemaStep.Snacks -> SnackBar(order) { if (game.buySnacks(order.toMap())) go(CinemaStep.Watch) }
            CinemaStep.Watch -> {
                val group = listOf(Moviegoer(game.s.look, "You", seats[0])) + friends.mapIndexed { i, f -> Moviegoer(f.look, f.name, seats[i + 1]) }
                val snacks = remember { order.flatMap { (s, n) -> List(n) { s } }.mapIndexed { i, s -> (i % group.size) to s } }
                MoviePlayer(game, movie, group, snacks, onDance = { go(CinemaStep.Pick); onDance(friends.toList()) }) { go(CinemaStep.Pick) }
            }
        }
    }
}

@Composable
private fun MovieList(game: GameState, onPick: (Movie) -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Px("popcorn", 44.dp); Txt("Christmas Movie Night!", 22, Color.White); Px("popcorn", 44.dp)
        }
        Txt("Pick a movie! Then bring friends, choose your seats and grab some snacks 🍿", 15, Color.White, align = TextAlign.Center)
        for (movie in movies) {
            Row(Modifier.fillMaxWidth().squish { onPick(movie) }.panel(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(64.dp, 76.dp).background(Pal.night, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { Px(movie.poster, 52.dp) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Txt(movie.title, 17)
                    Txt("🎟 ${money(movie.price)} a ticket", 14, Pal.brown)
                    if (movie.title in game.s.moviesSeen) Tag("Watched ✓", Pal.green, 12)
                }
                Txt("▶", 26, partyPink)
            }
        }
    }
}

@Composable
private fun BringFriends(game: GameState, movie: Movie, friends: MutableList<Npc>, onNext: () -> Unit) {
    Column(Modifier.fillMaxWidth().panel().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Txt("Who do you want to bring?", 20, partyPink)
        Txt("Tap up to $MAX_MOVIE_FRIENDS friends. Bringing a friend makes you better friends (+1 💖)!", 14, Pal.brown, align = TextAlign.Center)
        Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in npcs.chunked(3)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (npc in row) {
                val coming = npc in friends
                Column(
                    Modifier.width(116.dp).squish {
                        if (coming) friends.remove(npc)
                        else if (friends.size >= MAX_MOVIE_FRIENDS) game.say("You can bring up to $MAX_MOVIE_FRIENDS friends! 🎟")
                        else { friends.add(npc); Audio.play(Sfx.Ding) }
                    }.panel(if (coming) hex(0xFFE1EE) else Color.White, if (coming) partyPink else Pal.ink).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (coming) Bob(4f, 500) { ElfPx(npc.look, 74.dp) } else ElfPx(npc.look, 74.dp)
                    Txt(npc.name, 15)
                    Txt(if (coming) "Coming! 🎉" else hearts(game.hearts(npc)), if (coming) 13 else 8, if (coming) partyPink else Pal.ink)
                }
            }
        }
        }
        val n = 1 + friends.size
        Txt("🎟 $n ticket${if (n > 1) "s" else ""} × ${money(movie.price)} = ${money(n * movie.price)}", 18)
        Btn(if (friends.isEmpty()) "Just me! Choose seats 💺" else "Choose our seats 💺", partyPink, 18, Modifier.fillMaxWidth(), onClick = onNext)
    }
}

@Composable
private fun PickSeats(game: GameState, movie: Movie, friends: List<Npc>, seats: MutableList<Pair<Int, Int>>, onBuy: () -> Unit) {
    val taken = remember(movie) { takenSeats(movie, game.s.day) }
    val people = listOf(game.s.look to "You") + friends.map { it.look to it.name }
    Column(Modifier.fillMaxWidth().panel().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val next = people.getOrNull(seats.size)
        Txt(if (next == null) "Everyone has a seat! 💺" else if (seats.isEmpty()) "Tap a seat for YOU!" else "Now tap a seat for ${next.second}!", 19, partyPink, align = TextAlign.Center)
        // the screen is at the top, the back row at the bottom
        Box(Modifier.width(320.dp).height(18.dp).background(hex(0xDDEBF5), RoundedCornerShape(4.dp)).border(2.dp, Pal.ink, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            Txt("SCREEN", 12, Pal.ink)
        }
        for (row in cinemaRowSeats.indices) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Txt("${'A' + row}", 13, Pal.brown, modifier = Modifier.width(14.dp))
                for (seat in 0 until cinemaRowSeats[row]) {
                    val here = row to seat
                    val who = seats.indexOf(here)
                    val isTaken = here in taken
                    Box(
                        Modifier.size(44.dp, 48.dp)
                            .squish(enabled = !isTaken) {
                                if (who >= 0) seats.removeAt(who)
                                else if (seats.size < people.size) { seats.add(here); Audio.play(Sfx.Pop) }
                                else game.say("Everyone has a seat! Tap one of yours to move it.")
                            }
                            .panel(when { who >= 0 -> hex(0xFFE1EE); isTaken -> hex(0x8D96A8); else -> hex(0xB0283C) }, if (who >= 0) partyPink else Pal.ink, 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            who >= 0 -> ElfPx(people[who].first, 40.dp)
                            isTaken -> Px(Sprites.elfBack(ElfLook(hairColor = (row * 7 + seat) % ElfColors.hairs.size)), 38.dp, alpha = 0.5f)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(14.dp).background(hex(0xB0283C), RoundedCornerShape(3.dp))); Txt(" Free", 12) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(14.dp).background(hex(0x8D96A8), RoundedCornerShape(3.dp))); Txt(" Taken", 12) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(14.dp).background(hex(0xFFE1EE), RoundedCornerShape(3.dp)).border(1.dp, partyPink, RoundedCornerShape(3.dp))); Txt(" Yours", 12) }
        }
        Txt("Row C is the back row. Tap your seat again to change it.", 13, Pal.brown, align = TextAlign.Center)
        val total = people.size * movie.price
        Btn("🎟 Buy ${people.size} ticket${if (people.size > 1) "s" else ""}: ${money(total)}", Pal.red, 18, Modifier.fillMaxWidth(), enabled = seats.size == people.size, onClick = onBuy)
    }
}

@Composable
private fun SnackBar(order: MutableMap<MovieSnack, Int>, onDone: () -> Unit) {
    Column(Modifier.fillMaxWidth().panel().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bob(3f, 800) { ElfPx(npcs.first { it.name == "Pepper" }.look, 70.dp) }
            Bubble("Snacks for the movie? Popcorn's fresh! 🍿", 230.dp)
        }
        for (s in MovieSnack.entries) {
            val n = order[s] ?: 0
            Row(Modifier.fillMaxWidth().panel(if (n > 0) hex(0xFFE1EE) else Color.White).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Px(s.sprite, 36.dp)
                Column(Modifier.weight(1f)) { Txt(s.title, 16); Txt("${money(s.price)} · +${s.energy} ⚡", 12, Pal.brown) }
                Btn("−", Pal.night, 18, Modifier.width(48.dp), enabled = n > 0) { if (n <= 1) order.remove(s) else order[s] = n - 1 }
                Txt("$n", 18, align = TextAlign.Center, modifier = Modifier.width(26.dp))
                Btn("+", Pal.green, 18, Modifier.width(48.dp), enabled = n < 9) { order[s] = n + 1 }
            }
        }
        val total = order.entries.sumOf { it.key.price * it.value }
        Btn(if (total > 0) "Buy snacks ${money(total)} & go in! 🎬" else "No snacks, let's go in! 🎬", partyPink, 17, Modifier.fillMaxWidth(), onClick = onDone)
    }
}

@Composable
private fun MoviePlayer(game: GameState, movie: Movie, group: List<Moviegoer>, bought: List<Pair<Int, MovieSnack>>, onDance: () -> Unit = {}, onLeave: () -> Unit) {
    GameSong(game, Music.SilentNight)
    var scene by remember { mutableIntStateOf(0) }
    var st by remember { mutableFloatStateOf(0f) }     // seconds into this scene
    val ended = scene >= movie.scenes.size
    val snacks = remember { mutableStateListOf<Pair<Int, MovieSnack>>().apply { addAll(bought) } }   // (who has it, what)

    LaunchedEffect(scene) {
        if (scene >= movie.scenes.size) return@LaunchedEffect
        val start = withFrameMillis { it }
        st = 0f
        while (st < SCENE_SECONDS) withFrameMillis { st = (it - start) / 1000f }
        scene++
    }
    // your friends munch their snacks now and then
    LaunchedEffect(Unit) {
        while (true) {
            delay(6000L + Random.nextLong(4000))
            snacks.firstOrNull { it.first > 0 }?.let { snacks.remove(it); Audio.play(Sfx.Munch) }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt("🎬 ${movie.title}", 19, Color.White, align = TextAlign.Center)
        Cinema3D(game, movie, group, snacks) {
            if (ended) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Txt("✨ THE END ✨", 34, Pal.gold)
                    Txt("Thanks for watching!", 16, Color.White)
                }
            } else {
                SceneView(game, movie.scenes[scene], st)
            }
        }
        val caption = if (ended) "Everybody claps! 👏👏👏" else movie.scenes[scene].caption
        val shown = if (ended) caption else caption.take((st * 28).toInt() + 1)
        Txt(shown, 17, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().height(72.dp).panel().padding(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val mine = snacks.firstOrNull { it.first == 0 }
            if (!ended) Btn("⏭ Next", Pal.blue, 16, Modifier.width(110.dp)) { scene++ }
            if (mine != null) Btn(Pal.gold, Modifier.width(130.dp), onClick = { snacks.remove(mine); game.munch(mine.second) }) {
                Px(mine.second.sprite, 22.dp); Txt("Munch!", 16, Pal.ink)
            }
            if (ended) Btn("💃 Go dancing!", partyPink, 16, Modifier.width(180.dp), onClick = onDance)
            Btn(if (ended) "🎉 Back" else "🚪 Leave", if (ended) Pal.purple else Pal.red, 16, Modifier.width(110.dp)) { onLeave() }
        }
        if (!ended) Txt("Scene ${scene + 1} of ${movie.scenes.size}", 13, Color.White)
    }
}

@Composable
private fun SceneView(game: GameState, scene: Scene, st: Float) {
    val fade = (st / 0.5f).coerceIn(0f, 1f)
    val p = (st / SCENE_SECONDS).coerceIn(0f, 1f)
    Box(Modifier.fillMaxSize().graphicsLayer { alpha = fade }) {
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            val groundY = SCREEN_H * 0.74f * u
            drawRect(hex(scene.sky))
            if (scene.sky == 0x1D2B53L) for (i in 0 until 20) drawRect(Color.White.copy(alpha = 0.5f + 0.5f * sin(st * 2f + i)), Offset(((i * 89) % 390) * u, ((i * 37) % 150) * u), Size(2 * u, 2 * u))
            drawRect(hex(scene.ground), Offset(0f, groundY), Size(size.width, size.height - groundY))
            drawLine(Pal.ink.copy(alpha = 0.3f), Offset(0f, groundY), Offset(size.width, groundY), 2 * u, cap = StrokeCap.Square)
        }
        if (scene.snow) Snow(game.now, count = 25)
        for ((i, a) in scene.actors.withIndex()) {
            val img: ImageBitmap = when {
                a.sprite == "@me" -> Sprites.elf(game.s.look)
                a.sprite.startsWith("@") -> Sprites.elf((npcs.firstOrNull { it.name == a.sprite.drop(1) } ?: npcs.first()).look)
                else -> Sprites.named(a.sprite)
            }
            val w = a.h * img.width.toFloat() / img.height
            val cx = (a.x + a.dx * p) * SCREEN_W
            val bob = sin(st * 5f + i * 1.7f) * 2f
            val bottom = SCREEN_H * 0.74f + 6f - a.up - a.dy * p + bob
            Px(img, a.h.dp, Modifier.offset((cx - w / 2).dp, (bottom - a.h).dp).graphicsLayer { if (a.flip) scaleX = -1f })
        }
    }
}


// MARK: The cinema room: rows of seats, you in the back row, elves popping out for snacks

private const val ROOM_W = 406f
private const val ROOM_H = 470f
private const val AISLE_X = 374f          // the aisle runs down the right-hand side
private const val EXIT_Y = 560f           // the door is behind you, below the bottom edge
private const val ELF_H = 78f
private const val TRIP_SECONDS = 10f
private const val SIT = 0.64f            // how much of a seated elf shows above the seat back
private const val STAND = 0.95f
private val rowY = listOf(258f, 318f, 396f)        // top of each row's seat backs, far → near
private val rowScale = listOf(0.55f, 0.75f, 1.05f)
private val rowSeats = cinemaRowSeats
private val screenScale = 292f / SCREEN_W

private class Viewer(val front: ImageBitmap, val back: ImageBitmap, val row: Int, val seat: Int, val member: Int = -1) { val you get() = member == 0 }
private class Trip(val who: Int, val start: Float)

private fun seatSpan(row: Int) = (20f + (2 - row) * 14f) to (AISLE_X - 26f)
private fun seatX(row: Int, seat: Int): Float { val (l, r) = seatSpan(row); return l + (r - l) / rowSeats[row] * (seat + 0.5f) }
private fun seatW(row: Int): Float { val (l, r) = seatSpan(row); return (r - l) / rowSeats[row] * 0.88f }
/** Things look bigger the closer they are to you. */
private fun scaleAt(y: Float) = (0.55f + (y - 270f) / (408f - 270f) * 0.5f).coerceAtLeast(0.5f)
private fun lerp(a: Float, b: Float, k: Float) = a + (b - a) * k.coerceIn(0f, 1f)

private fun randomLook(r: Random) = ElfLook(
    girl = r.nextBoolean(), skin = r.nextInt(ElfColors.skins.size), hairStyle = HairStyle.entries.random(r), hairColor = r.nextInt(ElfColors.hairs.size),
    eye = r.nextInt(ElfColors.eyes.size), outfit = Outfit.entries.random(r), outfitColor = r.nextInt(ElfColors.clothes.size),
    hat = Hat.entries.random(r), hatColor = r.nextInt(ElfColors.clothes.size),
)

@Composable
private fun Cinema3D(game: GameState, movie: Movie, group: List<Moviegoer>, snacks: List<Pair<Int, MovieSnack>>, screen: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    val audience = remember(movie, group) {
        val r = Random(movie.title.hashCode() + game.s.day)
        val mine = group.map { it.seat }.toSet()
        group.mapIndexed { i, g -> Viewer(Sprites.elf(g.look), Sprites.elfBack(g.look), g.seat.first, g.seat.second, member = i) } +
            takenSeats(movie, game.s.day).filter { it !in mine }.map { (row, seat) -> randomLook(r).let { Viewer(Sprites.elf(it), Sprites.elfBack(it), row, seat) } }
    }
    val snackImages = remember { MovieSnack.entries.associateWith { Sprites.named(it.sprite) } }
    var ct by remember { mutableFloatStateOf(0f) }
    var nextTrip by remember { mutableFloatStateOf(2f) }
    val trips = remember { mutableStateListOf<Trip>() }
    val snacking = remember { mutableStateListOf<Int>() }   // came back with popcorn
    val popcorn = remember { Sprites.named("popcorn") }

    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { now ->
            ct = (now - start) / 1000f
            trips.removeAll { t -> (ct - t.start > TRIP_SECONDS).also { if (it && t.who !in snacking) snacking.add(t.who) } }
            if (ct > nextTrip && trips.size < 2) {
                val free = audience.indices.filter { i -> !audience[i].you && trips.none { it.who == i } }
                if (free.isNotEmpty()) trips.add(Trip(free.random(), ct))
                nextTrip = ct + 3f + Random.nextFloat() * 4f
            }
        }
    }

    Box(Modifier.size(ROOM_W.dp, ROOM_H.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
        // walls, floor and curtains
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            drawRect(Brush.verticalGradient(listOf(hex(0x1A1024), hex(0x2E1838))))
            drawRect(hex(0x3A2030), Offset(0f, 236 * u), Size(size.width, size.height))
            for (side in listOf(0f, 1f)) for (i in 0 until 4) {
                val x = if (side == 0f) (6 + i * 11) * u else (ROOM_W - 50 + i * 11) * u
                drawRect(if (i % 2 == 0) hex(0x8E1B2C) else hex(0xB3263A), Offset(x, 6 * u), Size(11 * u, 228 * u))
            }
            drawRect(hex(0x8E1B2C), Offset(0f, 0f), Size(size.width, 10 * u))
        }
        // the big screen
        Box(Modifier.offset(((ROOM_W - 292f) / 2).dp, 12.dp).size(292.dp, (SCREEN_H * screenScale).dp).clip(RoundedCornerShape(4.dp)).background(Color.Black)) {
            Box(Modifier.align(Alignment.Center).requiredSize(SCREEN_W.dp, SCREEN_H.dp).graphicsLayer { scaleX = screenScale; scaleY = screenScale }, content = screen)
        }
        // seats and everyone watching (drawn far to near so closer things cover farther ones)
        Canvas(Modifier.fillMaxSize()) {
            val u = density
            val items = ArrayList<Pair<Float, () -> Unit>>()
            for (row in 0..2) items.add(rowY[row] to {
                val sc = rowScale[row]
                for (seat in 0 until rowSeats[row]) {
                    val w = seatW(row); val h = 62f * sc; val x = seatX(row, seat) - w / 2
                    drawRoundRect(Pal.ink, Offset(x * u, rowY[row] * u), Size(w * u, h * u), androidx.compose.ui.geometry.CornerRadius(8 * sc * u))
                    drawRoundRect(hex(0xB0283C), Offset((x + 2) * u, (rowY[row] + 2) * u), Size((w - 4) * u, (h - 4) * u), androidx.compose.ui.geometry.CornerRadius(7 * sc * u))
                    drawRect(hex(0xD2455A), Offset((x + 5) * u, (rowY[row] + 5) * u), Size((w - 10) * u, 4 * sc * u))
                }
            })
            for ((i, v) in audience.withIndex()) {
                val sc0 = rowScale[v.row]; val h0 = ELF_H * sc0
                val sx = seatX(v.row, v.seat); val ry = rowY[v.row]
                val trip = trips.firstOrNull { it.who == i }
                val tau = trip?.let { ct - it.start } ?: -1f
                var x = sx; var top = ry - h0 * SIT; var h = h0; var img = v.back; var depth = ry - 0.5f; var carrying = false
                var visible = true
                when {
                    trip == null -> {}
                    tau < 0.5f -> top = ry - h0 * lerp(SIT, STAND, tau / 0.5f)                          // stand up
                    tau < 1.5f -> { top = ry - h0 * STAND; x = lerp(sx, AISLE_X, tau - 0.5f) }            // squeeze to the aisle
                    tau < 3.5f -> {                                                                        // walk out, towards you
                        val feet = lerp(ry + h0 * (1 - STAND), EXIT_Y, (tau - 1.5f) / 2f)
                        h = ELF_H * scaleAt(feet); top = feet - h + sin(ct * 14f) * 1.5f; x = AISLE_X; img = v.front; depth = 999f
                    }
                    tau < 6.5f -> visible = false                                                          // getting snacks!
                    tau < 8.5f -> {                                                                        // walk back in with popcorn
                        val feet = lerp(EXIT_Y, ry + h0 * (1 - STAND), (tau - 6.5f) / 2f)
                        h = ELF_H * scaleAt(feet); top = feet - h + sin(ct * 14f) * 1.5f; x = AISLE_X; depth = 999f; carrying = true
                    }
                    tau < 9.5f -> { top = ry - h0 * STAND; x = lerp(AISLE_X, sx, tau - 8.5f); carrying = true }
                    else -> top = ry - h0 * lerp(STAND, SIT, (tau - 9.5f) / 0.5f)                         // sit back down
                }
                if (!visible) continue
                val w = h * img.width / img.height
                items.add(depth to {
                    sprite(img, Offset((x - w / 2) * u, top * u), h * u)
                    if (carrying) sprite(popcorn, Offset((x + w * 0.25f) * u, (top + h * 0.45f) * u), h * 0.3f * u)
                })
                if (trip == null && i in snacking) items.add(ry + 0.5f to { sprite(popcorn, Offset((x + w * 0.22f) * u, (ry - h0 * 0.2f) * u), h0 * 0.26f * u) })
                // snacks you bought, held up beside each of you
                if (trip == null && v.member >= 0) {
                    val held = snacks.filter { it.first == v.member }.take(2)
                    for ((k, sn) in held.withIndex()) {
                        val side = if (k == 0) -0.42f else 0.22f
                        items.add(ry + 0.5f to { sprite(snackImages.getValue(sn.second), Offset((x + w * side) * u, (ry - h0 * 0.2f) * u), h0 * 0.26f * u) })
                    }
                }
            }
            items.sortedBy { it.first }.forEach { it.second() }
            // light from the projector behind you
            val beam = androidx.compose.ui.graphics.Path().apply {
                moveTo(ROOM_W / 2 * u, (ROOM_H + 20) * u); lineTo((ROOM_W - 292f) / 2 * u, 12 * u); lineTo((ROOM_W + 292f) / 2 * u, 12 * u); close()
            }
            drawPath(beam, Color.White.copy(alpha = 0.05f))
        }
        val you = audience.first { it.you }
        Box(Modifier.offset((seatX(you.row, you.seat) - 16).dp, (rowY[you.row] - ELF_H * rowScale[you.row] * SIT - 18).dp)) { Tag("You", partyPink, 11) }
    }
}


// ======================================================================
// MARK: Dance floor: dance with your friends and meet new elves
// ======================================================================

private enum class Move(val label: String) { Spin("🌀 Spin"), Jump("⬆️ Jump"), Wiggle("〰️ Wiggle"), Clap("👏 Clap") }
private const val MOVE_SECONDS = 0.8f
private const val DANCE_PARTY_MOVES = 12

/** Where each dancer stands: x centre, feet y, height. The middle of the front row is you. */
private val danceSpots = listOf(
    Triple(70f, 410f, 120f), Triple(336f, 410f, 120f),                                   // front row, either side of you
    Triple(45f, 300f, 88f), Triple(140f, 300f, 88f), Triple(266f, 300f, 88f), Triple(361f, 300f, 88f), // back row
)
private val youSpot = Triple(203f, 420f, 140f)

@Composable
private fun DanceFloor(game: GameState, brought: List<Npc>) {
    GameSong(game, Music.DeckTheHalls)
    // the friends you came with, a couple of elves you haven't met yet, and whoever's at the party today
    val crowd = remember(brought.toList()) {
        val unmet = npcs.filter { game.hearts(it) == 0 && it !in brought }.shuffled(Random(game.s.day)).take(2)
        (brought + unmet + game.partyGuests).distinct().take(danceSpots.size)
    }
    var t by remember { mutableFloatStateOf(0f) }
    val moves = remember { androidx.compose.runtime.mutableStateMapOf<Int, Pair<Move, Float>>() }   // dancer (-1 = you) to (move, when)
    var count by remember { mutableIntStateOf(0) }
    var said by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LaunchedEffect(Unit) { val start = withFrameMillis { it }; while (true) withFrameMillis { t = (it - start) / 1000f } }
    LaunchedEffect(Unit) {   // the others bust a move now and then
        while (true) { delay(1200L + Random.nextLong(1500)); if (crowd.isNotEmpty()) moves[crowd.indices.random()] = Move.entries.random() to t }
    }
    LaunchedEffect(said) { if (said != null) { delay(3200); said = null } }

    fun dance(m: Move) {
        moves[-1] = m to t
        crowd.indices.forEach { moves[it] = m to t + 0.12f * (it + 1) }   // everyone copies you!
        count++
        Audio.play(Sfx.Pop)
        if (count == DANCE_PARTY_MOVES) game.danceParty(crowd)
    }

    fun tap(i: Int) {
        val npc = crowd[i]
        said = if (game.hearts(npc) == 0) i to "Hi! I'm ${npc.name}! ${npc.about}. Nice to meet you! 💖" else i to partyLines.random()
        game.meet(npc)
        Audio.play(Sfx.Message)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(406.dp, 440.dp).clip(RoundedCornerShape(12.dp)).border(3.dp, Pal.ink, RoundedCornerShape(12.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                val u = density
                drawRect(Brush.verticalGradient(listOf(hex(0x1A1024), hex(0x3A1F5C))))
                // light-up dance floor
                val top = 200f; val rows = 5; val cols = 8; val tw = 406f / cols; val th = 48f
                for (r in 0 until rows) for (c in 0 until cols) {
                    val col = balloonColors[(r * 3 + c + (t * 2.5f).toInt()) % balloonColors.size]
                    drawRect(Pal.ink, Offset(c * tw * u, (top + r * th) * u), Size(tw * u, th * u))
                    drawRect(col.copy(alpha = 0.55f + 0.3f * sin(t * 5f + r + c)), Offset((c * tw + 2) * u, (top + r * th + 2) * u), Size((tw - 4) * u, (th - 4) * u))
                }
                // spinning disco ball and its sparkles
                val ball = Offset(203f * u, 44f * u)
                drawLine(Color.White.copy(alpha = 0.5f), Offset(ball.x, 0f), ball, 2 * u)
                drawCircle(Pal.ink, 28 * u, ball); drawCircle(hex(0xB8BFCC), 26 * u, ball)
                for (k in 0 until 16) {
                    val a = k * 0.7f + t * 2f
                    drawRect(if (k % 3 == 0) Color.White else hex(0x8D96A8), ball + Offset(kotlin.math.cos(a) * 16 * u, (k % 4 - 1.5f) * 9 * u), Size(5 * u, 5 * u))
                }
                for (k in 0 until 14) {
                    val a = k * 0.45f + t * 0.8f
                    val p = Offset((203f + kotlin.math.cos(a) * (60f + k * 12f)) * u, (130f + sin(a * 1.3f) * 60f + k * 14f) * u)
                    drawCircle(balloonColors[k % balloonColors.size].copy(alpha = 0.35f), 6 * u, p)
                }
            }
            // back row first, then the front row, then you in the middle
            for (i in crowd.indices.sortedBy { danceSpots[it].second }) {
                val (x, feet, h) = danceSpots[i]
                DancerView(crowd[i].look, crowd[i].name, x, feet, h, moves[i], t, i, isNew = game.hearts(crowd[i]) == 0, said = said?.takeIf { it.first == i }?.second) { tap(i) }
            }
            val (x, feet, h) = youSpot
            DancerView(game.s.look, "You", x, feet, h, moves[-1], t, 7, isNew = false, said = null) {}
            Txt("💃 DANCE FLOOR 🕺", 16, Color.White, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
        }
        Txt(if (count < DANCE_PARTY_MOVES) "Tap a move, everyone copies you! Dance party: $count/$DANCE_PARTY_MOVES" else "🎉 DANCE PARTY! Keep dancing! 🎉", 15, Color.White, align = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (m in Move.entries) Btn(m.label, partyPink, 15, Modifier.weight(1f)) { dance(m) }
        }
        Txt("Tap an elf to chat. Elves with a NEW tag become new friends! 💖", 13, Color.White, align = TextAlign.Center)
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.DancerView(
    look: ElfLook, name: String, x: Float, feet: Float, h: Float, move: Pair<Move, Float>?, t: Float, seed: Int, isNew: Boolean, said: String?, onTap: () -> Unit,
) {
    val img = remember(look) { Sprites.elf(look) }
    val w = h * img.width / img.height
    val k = move?.let { (t - it.second) / MOVE_SECONDS } ?: -1f
    val active = k in 0f..1f
    val m = move?.first
    Column(
        Modifier.offset((x - 70).dp, (feet - h - 52).dp).width(140.dp).squish(onClick = onTap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(34.dp), contentAlignment = Alignment.BottomCenter) {
            when {
                said != null -> Txt(said, 11, align = TextAlign.Center, modifier = Modifier.width(140.dp).panel(Color.White, radius = 6.dp).padding(3.dp), maxLines = 3)
                isNew -> Tag("NEW", Pal.green, 11)
                active && m == Move.Clap -> Txt("👏", 22)
            }
        }
        Px(img, h.dp, Modifier.graphicsLayer {
            val sway = sin(t * 4f + seed)
            when {
                active && m == Move.Spin -> scaleX = kotlin.math.cos(k * 2f * Math.PI.toFloat())
                active && m == Move.Jump -> translationY = -sin(k * Math.PI.toFloat()) * 40f * density
                active && m == Move.Wiggle -> rotationZ = sin(k * 25f) * 14f
                active && m == Move.Clap -> { scaleY = 1f - 0.08f * sin(k * 12f); translationY = -4f * density }
                else -> { rotationZ = sway * 5f; translationY = -kotlin.math.abs(sway) * 5f * density }
            }
        })
        Txt(name, 11, Color.White, maxLines = 1, modifier = Modifier.background(Pal.ink.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
    }
}
