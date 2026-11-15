package com.lexi.christmastown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private val screenColor = hex(0xEAF2FF)
private val myBubble = hex(0x3D6FD6)

@Composable
fun PhoneScreen(game: GameState) {
    var thread by remember { mutableStateOf<String?>(null) }
    // The phone itself: a chunky pixel frame with a screen inside.
    Column(
        Modifier.fillMaxSize().panel(hex(0x2A2833), Pal.ink, 26.dp).padding(8.dp)
            .clip(RoundedCornerShape(18.dp)).background(screenColor),
    ) {
        Row(Modifier.fillMaxWidth().background(hex(0x2A2833)).padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt(SimpleDateFormat("h:mm", Locale.getDefault()).format(Date(game.now)), 13, Color.White)
            Spacer(Modifier.weight(1f))
            Txt("🎄 ElfPhone", 13, Color.White)
            Spacer(Modifier.weight(1f))
            Txt("📶 🔋", 11, Color.White)
        }
        val t = thread
        if (t == null) ChatList(game) { thread = it } else ChatThread(game, t) { thread = null }
    }
}

// MARK: - List of chats

@Composable
private fun ChatList(game: GameState, open: (String) -> Unit) {
    val met = game.met
    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Txt("Messages", 26)
        if (met.isEmpty()) {
            Column(Modifier.fillMaxWidth().panel(Color.White).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Txt("📭 No contacts yet!", 20)
                Txt("Go say hi to elves in Friends and they'll give you their number!", 16, align = TextAlign.Center)
                Btn("🧝 Go to Friends", Pal.green, 17, Modifier.width(220.dp)) { game.go(Place.Friends) }
            }
            return
        }
        ChatRow(
            avatar = { GroupAvatar(met) },
            name = "🎄 Christmas Town Elves",
            preview = game.messages(GROUP).lastOrNull()?.let { "${if (it.from == ME) "You" else it.from}: ${it.text}" } ?: "Tap to say hi to everyone!",
            unread = game.unread(GROUP).takeIf { it > 0 }?.toString() ?: if (game.groupHasNews) "new" else null,
        ) { open(GROUP) }
        Txt("Private chats", 15, Pal.brown)
        for (name in met) {
            val npc = npcs.first { it.name == name }
            ChatRow(
                avatar = { Face(npc.look, 46.dp) },
                name = name,
                preview = game.messages(name).lastOrNull()?.let { (if (it.from == ME) "You: " else "") + it.text } ?: npc.job,
                unread = game.unread(name).takeIf { it > 0 }?.toString(),
            ) { open(name) }
        }
    }
}

@Composable
private fun ChatRow(avatar: @Composable () -> Unit, name: String, preview: String, unread: String?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().squish(onClick = onClick).panel(Color.White).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        avatar()
        Column(Modifier.weight(1f)) {
            Txt(name, 17, maxLines = 1)
            Txt(preview, 13, Pal.ink.copy(alpha = 0.6f), maxLines = 1)
        }
        if (unread != null) Txt(unread, 13, Color.White, modifier = Modifier.background(Pal.red, RoundedCornerShape(10.dp)).padding(horizontal = 7.dp, vertical = 2.dp))
    }
}

/** An elf's face in a little circle (the top of their sprite). */
@Composable
fun Face(look: ElfLook, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(hex(0xBFE9F5)).border(2.dp, Pal.ink, CircleShape), contentAlignment = Alignment.TopCenter) {
        ElfPx(look, size * 1.45f, Modifier.offset(y = -size * 0.08f))
    }
}

@Composable
private fun GroupAvatar(met: List<String>) {
    Box(Modifier.size(46.dp)) {
        met.take(3).forEachIndexed { i, n ->
            Box(Modifier.offset(x = (i * 10).dp, y = (if (i % 2 == 0) 0 else 14).dp)) { Face(npcs.first { it.name == n }.look, 30.dp) }
        }
    }
}

// MARK: - A conversation

private val dmQuick = listOf("👋 Hi!", "How are you?", "Tell me a joke!", "What should I do?", "How are the reindeer?", "What's your favorite thing?", "Bye! 👋")
private val groupQuick = listOf("Hi everyone! 👋", "What's new?", "Tell me a joke!", "I finished my jobs! 🎉", "Good night! 🌙")

@Composable
private fun ChatThread(game: GameState, thread: String, back: () -> Unit) {
    val scope = rememberCoroutineScope()
    var typing by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    val msgs = game.messages(thread)
    val list = rememberLazyListState()
    val isGroup = thread == GROUP

    LaunchedEffect(thread) { if (isGroup) game.openGroup() }
    LaunchedEffect(msgs.size, typing) {
        game.markRead(thread)
        val total = msgs.size + (if (typing != null) 1 else 0)
        if (total > 0) list.animateScrollToItem(total - 1)
    }
    // The group keeps chatting while you watch.
    if (isGroup) LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(25_000, 45_000))
            if (typing == null && game.met.isNotEmpty()) {
                typing = game.met.random()
                delay(1500)
                game.liveGroupEvent()
                typing = null
            }
        }
    }

    // What the elves are waiting for you to answer (if anything).
    val lastFromThem = msgs.lastOrNull()?.takeIf { it.from != ME && it.qid != null }
    val pending = ElfBrain.question(lastFromThem?.qid)

    fun send(text: String) {
        if (text.isBlank() || typing != null) return
        val (clean, flag) = game.cleanMessage(text.trim().take(200))
        game.post(thread, listOf(ChatMsg(ME, clean, System.currentTimeMillis())))
        draft = ""
        val responder = if (isGroup) (lastFromThem?.from ?: game.met.randomOrNull()) else thread
        if (responder == null) return
        scope.launch {
            typing = responder
            delay(Random.nextLong(900, 1800))
            val replies = if (flag != ChatFilter.Flag.Ok) listOf(ChatMsg(responder, ChatFilter.safetyReply(flag, responder), System.currentTimeMillis()))
            else if (isGroup) ElfBrain.groupReply(game, game.met, clean, pending, lastFromThem?.from)
            else ElfBrain.reply(game, thread, clean, pending)
            replies.forEachIndexed { i, r ->
                if (i > 0) { typing = r.from; delay(Random.nextLong(900, 1600)) }
                typing = null
                game.post(thread, listOf(r.copy(time = System.currentTimeMillis())))
            }
            typing = null
            if (!isGroup) game.chatFriendship(thread)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Txt("‹", 30, Pal.blue, modifier = Modifier.squish(onClick = back).padding(horizontal = 6.dp))
            if (isGroup) GroupAvatar(game.met) else Face(npcs.first { it.name == thread }.look, 40.dp)
            Column(Modifier.weight(1f)) {
                Txt(if (isGroup) "Christmas Town Elves" else thread, 17, maxLines = 1)
                Txt(if (isGroup) game.met.joinToString(", ") + ", you" else npcs.first { it.name == thread }.job, 12, Pal.ink.copy(alpha = 0.6f), maxLines = 1)
            }
        }

        // Messages
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp), state = list, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Spacer(Modifier.size(4.dp)) }
            items(msgs) { m -> Bubble(m, isGroup) }
            if (typing != null) item { TypingBubble(typing!!) }
        }

        // Tap-to-answer buttons, or handy quick replies
        val chips = if (pending != null) pending.options else if (isGroup) groupQuick else dmQuick
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (c in chips) Txt(
                c, 14, if (pending != null) Color.White else Pal.blue,
                modifier = Modifier.squish { send(c) }.background(if (pending != null) Pal.green else Color.White, RoundedCornerShape(14.dp))
                    .border(2.dp, if (pending != null) Pal.darkGreen else Pal.blue, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }

        // Type your own message
        Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                draft, { draft = it.take(200) }, singleLine = true,
                textStyle = TextStyle(fontFamily = Pixelify, fontSize = 17.sp, color = Pal.ink),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send(draft) }),
                modifier = Modifier.weight(1f).background(screenColor, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
                decorationBox = { inner -> Box { if (draft.isEmpty()) Txt("Type a message…", 17, Pal.ink.copy(alpha = 0.35f)); inner() } },
            )
            Txt("➤", 22, Color.White, modifier = Modifier.squish { send(draft) }.background(myBubble, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}

private fun ago(t: Long): String {
    val m = (System.currentTimeMillis() - t) / 60_000
    return when {
        m < 1 -> "now"
        m < 60 -> "${m}m"
        m < 24 * 60 -> "${m / 60}h"
        else -> "${m / (24 * 60)}d"
    }
}

@Composable
private fun Bubble(m: ChatMsg, isGroup: Boolean) {
    val mine = m.from == ME
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        if (!mine) {
            npcs.firstOrNull { it.name == m.from }?.let { Face(it.look, 30.dp) }
            Spacer(Modifier.size(6.dp))
        }
        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
            if (!mine && isGroup) Txt(m.from, 12, Pal.brown)
            Txt(
                m.text, 16, if (mine) Color.White else Pal.ink,
                modifier = Modifier.widthIn(max = 290.dp)
                    .background(if (mine) myBubble else Color.White, RoundedCornerShape(14.dp))
                    .then(if (mine) Modifier else Modifier.border(2.dp, hex(0xD5DBE6), RoundedCornerShape(14.dp)))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
            Txt(ago(m.time), 10, Pal.ink.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun TypingBubble(elf: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        npcs.firstOrNull { it.name == elf }?.let { Face(it.look, 30.dp) }
        Bob(3f, 300) { Txt("$elf is typing… 💬", 14, Pal.ink.copy(alpha = 0.6f), modifier = Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) }
    }
}
