package com.lexi.christmastown

import kotlinx.serialization.Serializable
import kotlin.random.Random

/** One chat message. [from] is an elf's name, "me" (the player), or "" for system notes. */
@Serializable data class ChatMsg(
    val from: String,
    val text: String,
    val time: Long,
    val options: List<String> = emptyList(),   // tap-to-answer buttons
    val qid: String? = null,                   // which question this is (so we can react to the answer)
)

const val GROUP = "group"
const val ME = "me"

// MARK: - Safety filter

object ChatFilter {
    enum class Flag { Ok, Unkind, Violent, Grown, Personal, Hurting }

    private val leet = mapOf('0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '7' to 't', '@' to 'a', '$' to 's', '!' to 'i')

    private val swears = setOf(
        "fuck", "fuk", "fck", "fuq", "shit", "sht", "bitch", "biatch", "bastard", "damn", "crap", "dick", "piss", "cunt",
        "asshole", "ass", "arse", "wtf", "stfu", "slut", "whore", "fag", "retard", "bollocks", "wanker", "twat", "prick", "bullshit",
    )
    private val swearStarts = listOf("fuck", "shit", "bitch", "asshol", "dickhead", "motherf")
    private val unkind = setOf("stupid", "dumb", "idiot", "loser", "ugly", "moron", "dork", "sucks", "suck")
    private val violent = setOf("kill", "killed", "killing", "murder", "die", "dead", "blood", "bloody", "gun", "guns", "shoot", "shooting", "stab", "knife", "bomb", "weapon", "torture", "strangle")
    private val grown = setOf("sex", "sexy", "naked", "nude", "nudes", "boob", "boobs", "penis", "vagina", "porn", "horny", "dildo", "condom", "kinky")
    private val hurtingPhrases = listOf("killmyself", "hurtmyself", "wanttodie", "wannadie", "endmylife", "hatemylife", "nobodylovesme", "nobodylikesme", "iwanttodie")

    /** Squash repeated letters: "fuuuuck" -> "fuck", "kiiiill" -> "kil". */
    private fun squash(s: String) = buildString { for (c in s) if (isEmpty() || last() != c) append(c) }

    private fun tokens(text: String): List<String> {
        val normal = text.lowercase().map { leet[it] ?: it }.joinToString("")
        return normal.split(Regex("[^a-z]+")).filter { it.isNotEmpty() }
    }

    /** True if [token] (or a stretched-out spelling like "stuuupid") is in [words]. */
    private fun hit(token: String, words: Set<String>): Boolean =
        token in words || words.any { w -> squash(w) == squash(token) && squash(token).length >= 3 && token.length > w.length }

    /** Returns the text with bad words starred out, plus what kind of problem (if any) we found. */
    fun check(raw: String): Pair<String, Flag> {
        val joined = squash(tokens(raw).joinToString(""))
        if (hurtingPhrases.any { squash(it) in joined }) return raw to Flag.Hurting

        val lower = raw.lowercase()
        val personal = raw.count { it.isDigit() } >= 7 || Regex("\\S+@\\S+\\.\\S+").containsMatchIn(raw) ||
            listOf("my address", "i live at", "i live on", "my phone number", "my number is", "my password", "my school is").any { it in lower }

        var flag = Flag.Ok
        fun raise(f: Flag) { if (flag == Flag.Ok || f == Flag.Grown) flag = f }
        val masked = raw.split(Regex("(?<=\\s)|(?=\\s)")).joinToString("") { word ->
            val t = tokens(word).joinToString("")
            val stars = "★".repeat(word.trim().length.coerceIn(3, 8))
            when {
                t.isEmpty() -> word
                hit(t, swears) || swearStarts.any { t.startsWith(it) } -> { raise(Flag.Unkind); stars }
                hit(t, grown) -> { raise(Flag.Grown); stars }
                hit(t, violent) -> { raise(Flag.Violent); word }
                hit(t, unkind) -> { raise(Flag.Unkind); word }
                else -> word
            }
        }
        if (flag == Flag.Ok && listOf("shutup", "hateyou", "ihateu").any { squash(it) in joined }) flag = Flag.Unkind
        // Friendly phrases that just look scary.
        if (flag == Flag.Violent && listOf("snowbal", "dietired", "dead tired", "deadtired", "dielaughing").any { squash(it) in joined }) flag = Flag.Ok
        if (flag == Flag.Ok && personal) flag = Flag.Personal
        return masked to flag
    }

    fun safetyReply(flag: Flag, elf: String): String = when (flag) {
        Flag.Hurting -> "I'm really glad you told me. 💖 That sounds really hard. Please talk to a grown-up you trust RIGHT NOW, like a parent, teacher or family member. They love you and want to help."
        Flag.Personal -> "Whoa, wait! 🔒 Never share your address, phone number, school or passwords online, even with elves. Let's keep that secret safe!"
        Flag.Violent -> listOf("Eek! 😳 Let's keep things cozy and safe in Christmas Town. Want to talk about cookies instead? 🍪", "Yikes, that sounds scary! Let's talk about something happy, like reindeer! 🦌").random()
        Flag.Grown -> "Hmm, let's keep our chat kid-friendly! 🎄 Ask me about toys, cookies or reindeer!"
        Flag.Unkind -> when (elf) {
            "Pepper" -> "Ooh, let's use kind words, sweetie. 💖 Kind words are like warm cookies!"
            "Sprout" -> "Um... that's not very kind. 🥺 Let's be nice to each other, okay?"
            "Twinkle" -> "Nooo, mean words aren't sparkly! ✨ Let's use kind ones!"
            "Holly" -> "Hmm, my kindness-o-meter just beeped 📟 Let's try nicer words!"
            "Frost" -> "Brrr, that was a chilly thing to say! 🥶 Let's keep it warm and kind."
            else -> "Whoa, let's use kind words in Christmas Town! 💖"
        }
        Flag.Ok -> ""
    }
}

// MARK: - Questions elves ask you

class Question(val id: String, val ask: String, val options: List<String>, val memory: String? = null, val react: (String) -> String)

private val dmQuestions = mapOf(
    "Jingle" to listOf(
        Question("j_toy", "What's your favorite toy to make? 🧸", listOf("Teddy bears", "Candy canes", "Trains"), "favToy") { "$it?! Awesome choice! 🔧 I'll save you the best parts!" },
        Question("j_build", "If you could invent ANY toy, what would it be? 🤔", listOf("Flying bike", "Robot dog", "Rainbow yo-yo")) { "A $it?! That's GENIUS. Let's build it after Christmas! 🛠️" },
    ),
    "Twinkle" to listOf(
        Question("t_theme", "What should my next party theme be? ✨", listOf("Candy Land", "Snow Princess", "Pajama Party"), "favParty") { "$it!!! OMG yes, I'm already planning the decorations! 🎉" },
        Question("t_color", "What's your favorite color? 🌈", listOf("Pink", "Blue", "Green", "Purple"), "favColor") { "$it is SO pretty! I'll use lots of $it glitter just for you ✨" },
    ),
    "Pepper" to listOf(
        Question("p_cookie", "What's your favorite cookie? 🍪", listOf("Sugar", "Berry", "Choc chip"), "favCookie") { "Ooh, $it cookies! I'll bake an extra batch just for you, sweetie 💕" },
        Question("p_drink", "Hot cocoa or warm milk? ☕", listOf("Hot cocoa", "Warm milk")) { "$it it is! With a cinnamon sprinkle on top 😋" },
    ),
    "Holly" to listOf(
        Question("h_invent", "If I built you a robot helper, what should it do? 🤖", listOf("Make cookies", "Walk my pet", "Wrap presents")) { "A robot that can $it? Drawing the plans RIGHT NOW! 📐" },
        Question("h_power", "Which superpower would you pick? ⚡", listOf("Flying", "Invisibility", "Talking to animals")) { "$it! I'll try to invent it. No promises! 🔬" },
    ),
    "Frost" to listOf(
        Question("f_joke", "Want to hear a joke or tell me one? 😆", listOf("Tell me one!", "I'll tell you!")) { if ("me" in it) "What's a snowman's favorite snack? ICE crispy treats! ❄️😂" else "Ooh, go go go! I'm ready to laugh! 😆" },
        Question("f_build", "What should I sculpt out of snow next? ⛄", listOf("A dragon", "Santa", "A giant cookie")) { "$it made of snow?! That's going to be EPIC. ❄️" },
    ),
    "Sprout" to listOf(
        Question("s_deer", "Which reindeer do you like best? 🦌", listOf("Dasher", "Comet", "Rudolph"), "favReindeer") { "$it is really sweet. 🥰 I'll tell them you said hi." },
        Question("s_pet", "Do you have a favorite animal? 🐾", listOf("Dogs", "Cats", "Bunnies", "Reindeer!")) { "$it are the best! Animals make everything cozier. 🌱" },
    ),
)

private val groupQuestions = listOf(
    Question("g_topping", "@{me} quick poll: sprinkles or chocolate chips? 🍪", listOf("Sprinkles ✨", "Chocolate chips 🍫"), "favTopping") { "${it.dropLast(2).trim()} gang!! 🙌" },
    Question("g_fast", "@{me} which reindeer is the FASTEST? 🦌💨", listOf("Dasher", "Comet", "Rudolph")) { "$it?! Great pick! I'll tell them, they'll be so proud 🏆" },
    Question("g_hat", "@{me} what color should my new hat be? 🎩", listOf("Red", "Green", "Sparkly gold")) { "$it hat coming right up! 🧵" },
    Question("g_snow", "@{me} snowball fight or build a snowman? ⛄", listOf("Snowball fight!", "Snowman!")) { "${it.trimEnd('!')} it is! Meet us outside after work! ❄️" },
    Question("g_next", "@{me} what toy should we make next? 🧸", listOf("Teddy bear", "Robot", "Toy train")) { "A $it! On it! 🔨" },
    Question("g_time", "@{me} are you a morning elf or a night elf? 🌙", listOf("Morning ☀️", "Night 🌙")) { if ("Morning" in it) "Early bird! ☀️ You must get SO much done!" else "Night owl! 🦉 Same here, the stars are the best." },
    Question("g_cocoa", "@{me} hot cocoa: marshmallows or whipped cream? ☕", listOf("Marshmallows", "Whipped cream", "BOTH")) { if (it == "BOTH") "BOTH?! Legendary. 😎☕" else "$it! Yum yum yum 😋" },
    Question("g_song", "@{me} what's the best Christmas song? 🎶", listOf("Jingle Bells", "Rudolph", "Let It Snow")) { "🎶 $it 🎶 now it's stuck in my head 😂" },
    Question("g_santa", "@{me} does Santa like cookies or carrots more? 😂", listOf("Cookies!", "Carrots!")) { if ("Carrots" in it) "Carrots?! Those are for the reindeer, silly! 🥕😂" else "Cookies, OBVIOUSLY! 🍪🎅" },
    Question("g_party", "@{me} want to come to the snow party on Friday? 🎉", listOf("Yes!", "Maybe")) { if (it == "Yes!") "YAY!! It won't be a party without you! 🎉" else "Hope you can make it! There'll be cake 🎂" },
    Question("g_gift", "@{me} what present would YOU want from Santa? 🎁", listOf("A puppy", "A bike", "Art supplies", "A surprise")) { "$it! I'll whisper it to Santa 🤫🎅" },
)

private val allQuestions = (dmQuestions.values.flatten() + groupQuestions).associateBy { it.id }

// MARK: - Things elves say to each other in the group chat

/** A little scene. Each line is (speaker slot, text). Slot 0/1 = two different elves. {me} = player name. */
private val groupScenes: List<List<Pair<Int, String>>> = listOf(
    listOf(0 to "Has anyone seen my other mitten? 🧤", 1 to "Is it the one on your head? 😂", 0 to "...oh."),
    listOf(0 to "Rudolph sneezed and the whole stable glowed red 🔴😂"),
    listOf(0 to "Who put glitter in the cocoa machine?? ✨☕", 1 to "...it's festive?"),
    listOf(0 to "I baked 200 cookies today 🍪", 1 to "How many are left?", 0 to "...three."),
    listOf(0 to "Snowball fight after work? ❄️", 1 to "I'm IN! ⛄"),
    listOf(0 to "Santa laughed so hard the whole workshop shook 🎅😂"),
    listOf(0 to "The toy trains are racing each other again 🚂🚂", 1 to "My money's on the red one!"),
    listOf(0 to "Comet ate my sandwich 🥪🦌", 1 to "Reindeer gotta eat too!"),
    listOf(0 to "I'm making a teddy bear with a tiny scarf! 🧸🧣", 1 to "That's the cutest thing EVER"),
    listOf(0 to "Fun fact: every snowflake has six sides! ❄️", 1 to "Whoa, I didn't know that!"),
    listOf(0 to "My hat keeps falling over my eyes 🎩", 1 to "Is that why you walked into the Christmas tree yesterday? 🎄😂"),
    listOf(0 to "Who's ready for Christmas?? 🎄🎄🎄", 1 to "MEEEEE"),
    listOf(0 to "I tried to wrap a present and wrapped myself instead 🎁😅"),
    listOf(0 to "Dasher and Dancer are racing around the stable 🦌💨", 1 to "Who's winning?", 0 to "The carrot bucket 🥕"),
    listOf(0 to "The robots learned to dance! 🤖🕺", 1 to "Is that safe??", 0 to "Probably!"),
    listOf(0 to "Hot cocoa break, anyone? ☕", 1 to "With marshmallows please!"),
    listOf(0 to "Good morning, Christmas Town! ☀️❄️"),
    listOf(0 to "How do you fix a squeaky sleigh? 🛷", 1 to "A little oil. Or sing to it. 🎶"),
    listOf(0 to "I found a candy cane in my pocket from LAST year 🍬", 1 to "...please don't eat it 😂"),
    listOf(0 to "The northern lights look amazing tonight 🌌", 1 to "So pretty!!"),
    listOf(0 to "Prancer just did a backflip!! 🦌", 1 to "Pics or it didn't happen 📸"),
    listOf(0 to "I'm practicing Jingle Bells on the drum 🥁", 1 to "We can hear... 😅"),
    listOf(0 to "Who wants to build a snow elf? ⛄🧝", 1 to "Me! I'll bring the carrot nose 🥕"),
    listOf(0 to "Gingerbread house contest this weekend! 🏠🍪", 1 to "I'm making a CASTLE!"),
    listOf(0 to "{me} has been working SO hard lately! 💪", 1 to "Go {me}!! 🎉"),
    listOf(0 to "Shh, Santa's napping 😴🎅", 1 to "His snore sounds like 'ho ho ho' 😂"),
    listOf(0 to "I named a snowflake Steve ❄️", 1 to "Where's Steve now?", 0 to "...he melted 😢"),
    listOf(0 to "Who left the workshop door open? It's snowing INSIDE ❄️😂"),
    listOf(0 to "Tip of the day: brush the reindeer AND feed them, or the job won't count! 🦌"),
    listOf(0 to "Tip: eat a snack when your energy gets low! ⚡🍪"),
    listOf(0 to "Did everyone finish Santa's jobs today? 📋", 1 to "Almost! Just the toys left 🧸"),
    listOf(0 to "Knock knock 🚪", 1 to "Who's there?", 0 to "Snow.", 1 to "Snow who?", 0 to "Snow time to waste, Christmas is coming! 🎄"),
    listOf(0 to "My cookies came out shaped like reindeer by accident 🍪🦌", 1 to "That's not an accident, that's ART"),
    listOf(0 to "I can't find the tape 😩🎁", 1 to "It's stuck to your elbow"),
)

val jokes = listOf(
    "What do elves learn in school? The ELF-abet! 🔤😂",
    "Why was the snowman looking through the carrots? He was picking his nose! 🥕😂",
    "What do you call an elf who sings? A WRAPPER! 🎤🎁",
    "What's a snowman's favorite snack? Ice Krispies! ❄️😂",
    "How does a gingerbread man make his bed? With cookie sheets! 🍪🛏️",
    "Why are Christmas trees bad at knitting? They always drop their needles! 🎄😂",
    "What do reindeer hang on their trees? Horn-aments! 🦌✨",
    "What do you call Santa when he takes a break? Santa PAUSE! ⏸️🎅",
    "What do you get if you cross a bell and a skunk? Jingle SMELLS! 🔔😂",
    "Why did Rudolph get a bad report card? He went down in history! 📚🦌",
)

// MARK: - The elf brain

object ElfBrain {
    private fun pick(vararg xs: String) = xs.random()
    /** Short words must match whole ("hi" but not "his"); longer ones can be the start of a word ("decorat" -> "decorate"). */
    private fun has(t: String, vararg words: String) = words.any { w ->
        val pattern = if (w.length <= 4) "\\b${Regex.escape(w)}s?\\b" else "\\b${Regex.escape(w)}"
        Regex(pattern).containsMatchIn(t)
    }

    /** Personal flavor so each elf sounds different. */
    private fun flavor(elf: String) = when (elf) {
        "Jingle" -> pick(" 🔧", " Awesome!", " 🧸", "")
        "Twinkle" -> pick(" ✨", " 💖✨", " 🎉", " OMG!")
        "Pepper" -> pick(" 🍪", " 💕", " sweetie!", "")
        "Sprout" -> pick(" 🌱", " 🦌", " 🥰", "")
        "Holly" -> pick(" 🔬", " Eureka!", " 🤖", "")
        "Frost" -> pick(" ❄️", " 😆", " ⛄", " Brrr!")
        else -> ""
    }

    private fun mem(game: GameState, key: String) = game.s.memory[key]

    /** What the elf says back to a private message. May include a follow-up question. */
    fun reply(game: GameState, elf: String, text: String, answering: Question?): List<ChatMsg> {
        val now = System.currentTimeMillis()
        val name = game.s.name
        val t = text.lowercase()
        val s = game.s

        // Answering a question the elf asked?
        if (answering != null) {
            val ans = answering.options.firstOrNull { it.lowercase().take(5) in t } ?: text.trim().take(30)
            answering.memory?.let { game.remember(it, ans) }
            return listOf(ChatMsg(elf, answering.react(ans), now))
        }

        val carrots = s.crops.n(Crop.Carrot)
        val hungry = game.reindeer.filterNot { it in s.fed }
        val jobsLeft = s.jobs.filterNot { it.finished }
        val main = when {
            has(t, "hi", "hello", "hey", "hiya", "howdy", "yo", "sup", "good morning", "morning") -> when (elf) {
                "Jingle" -> "Hey hey, $name! 🔧 Ready to build some toys?"
                "Twinkle" -> "HIIII $name!! ✨💖 I was JUST thinking about you!"
                "Pepper" -> "Hello, sweetie! 🍪 " + (mem(game, "favCookie")?.let { "Still loving $it cookies?" } ?: "Want a warm cookie?")
                "Holly" -> "Greetings, $name! 🔬 I just finished inventing... a better spoon."
                "Frost" -> "Yo yo, $name! ❄️ Why are you so cool? Oh wait, that's me. 😎"
                else -> "Oh! Hi $name 👋 " + (mem(game, "favReindeer")?.let { "$it says hi too! 🦌" } ?: "The reindeer say hi! 🦌")
            }
            has(t, "how are you", "how r u", "hru", "how's it going", "hows it going", "how are u", "you ok", "u ok") -> when (elf) {
                "Jingle" -> "SUPER good! I just made a teddy bear with button eyes! 🧸 How are YOU?"
                "Twinkle" -> "Amazing!!! I'm planning a surprise party ✨ (shh!) How are you?"
                "Pepper" -> "Warm and toasty, thank you! 🍪 My oven smells like cinnamon. How are you, dear?"
                "Holly" -> "Excellent! Only one small explosion today 💥 (it was glitter). How are you?"
                "Frost" -> "Ice to see you! 🧊 Get it? I'm great! How are you?"
                else -> "Pretty good! Comet licked my face this morning 🦌😊 How are you?"
            }
            has(t, "sad", "upset", "cry", "crying", "lonely", "bad day", "mad", "angry", "scared", "worried", "tired") ->
                "Aww, I'm sorry you feel that way. 💖 Want a virtual hug? 🤗 If something's really wrong, telling a grown-up you trust always helps. I'm here to chat too!"
            has(t, "happy", "yay", "excited", "great", "good", "awesome", "fine", "amazing", "fantastic") ->
                pick("Yay! That makes me happy too! 😄", "Woohoo! 🎉 Happy elves make the best toys!", "That's the best news! ✨")
            has(t, "joke", "funny", "laugh", "make me laugh") -> jokes.random()
            has(t, "are you real", "are you a robot", "are you ai", "are you a bot", "are you human", "is this real") ->
                "I'm $elf, an elf in Christmas Town! 🧝 I'm a character in this game, so I live inside your phone. But our friendship is real fun! 💖"
            has(t, "your name", "who are you", "what's your name", "whats your name") ->
                "I'm $elf! " + when (elf) { "Jingle" -> "Toy maker extraordinaire 🔧🧸"; "Twinkle" -> "Party planner and glitter expert ✨🎉"; "Pepper" -> "Head baker of Christmas Town 🍪"; "Holly" -> "Inventor of mostly-working gadgets 🔬"; "Frost" -> "Snow sculptor and joke champion ❄️😆"; else -> "I look after Santa's reindeer 🦌" }
            has(t, "what do you do", "your job", "what's your job", "whats your job") ->
                when (elf) { "Jingle" -> "I make toys! Trains, robots, teddy bears... ALL the toys! 🔧"; "Twinkle" -> "I plan the BEST parties in the North Pole ✨🎉"; "Pepper" -> "I bake cookies for Santa and everyone! 🍪"; "Holly" -> "I invent things! Toy machines, snow cannons, self-stirring bowls... 🔬"; "Frost" -> "I make snow sculptures and tell the BEST jokes ⛄😆"; else -> "I feed and brush the reindeer. They're my best friends 🦌🥕" }
            has(t, "money", "paid", "pay", "rich", "coins", "dollars") ->
                if (s.paidToday) "You already got your ${money(PAY)} today! 💰 Spend it on something fun in the Shop!"
                else "Santa pays ${money(PAY)} when you finish ALL your jobs. You have ${jobsLeft.size} left today! 📋"
            has(t, "help", "what should i do", "what do i do", "stuck", "how do i", "confused", "what now") ->
                if (jobsLeft.isEmpty()) "You're all done for today! 🎉 Go to sleep 💤 to start a new day, or decorate your house!"
                else "Next up: ${jobsLeft.first().text}! ${jobsLeft.first().kind.emoji} Tap the icons on the main screen to get there."
            has(t, "reindeer", "rudolph", "dasher", "comet", "carrot", "feed", "brush", "stable") ->
                when {
                    hungry.isEmpty() -> "All the reindeer are fed and happy! 🦌💖"
                    carrots == 0 -> "${hungry.first()} is hungry but you're out of carrots! Grow some in the Greenhouse 🥕🌱"
                    else -> "${hungry.first()} is still hungry! You have $carrots carrots 🥕 Go to the Stable!"
                }
            has(t, "cookie", "bake", "baking", "oven", "biscuit") ->
                "Cookies need wheat from the Greenhouse! 🌾 " + when { game.level < 2 -> "Berry cookies unlock at Level 2!"; game.level < 3 -> "Choc chip unlocks at Level 3!"; else -> "You can bake ALL the cookies now! 🍪" }
            has(t, "toy", "teddy", "train", "doll", "robot", "drum", "workshop", "candy cane") ->
                "You've made ${s.toys.values.sum()} toys ready to give! 🧸 " + Toy.entries.firstOrNull { it.unlockLevel > game.level }?.let { "${it.title}s unlock at Level ${it.unlockLevel}!" }.orEmpty()
            has(t, "santa", "naughty", "nice", "list") ->
                if (game.level < 2) "Santa's list unlocks at Level 2! Keep working hard 📜" else "Santa LOVES when you help with his list. Read carefully: naughty or nice? 😇😈"
            has(t, "plant", "seed", "grow", "garden", "greenhouse", "wheat", "water") ->
                "Plant seeds, water them 💧, wait a bit, then pick them! 🌱 Seeds are in the Shop if you run out."
            has(t, "party", "decorat", "celebrate") ->
                if (elf == "Twinkle") "PARTY?! Did someone say PARTY?! 🎉✨ Plan one with me in Friends when we're good friends!" else "Twinkle throws the BEST parties. Ask her! 🎉"
            has(t, "shop", "buy", "clothes", "hat", "dress", "crown") -> "The Shop has clothes, hats, furniture and food! 🛍️ The Crown is SO fancy 👑"
            has(t, "christmas", "present", "gift", "snow", "winter", "xmas") ->
                pick("Christmas is the BEST time of year! 🎄", "Did you know it's always snowing in Christmas Town? ❄️", "I already wrapped my presents! 🎁 Well, most of them.")
            has(t, "favorite", "favourite", "like best", "love most") -> when (elf) {
                "Jingle" -> "My favorite thing? Robots! 🤖 They beep!"
                "Twinkle" -> "Glitter. Obviously. ✨✨✨"
                "Pepper" -> "Warm cookies straight from the oven 🍪 nothing better!"
                "Holly" -> "Gears! Springs! Anything that goes BOING! 🔩"
                "Frost" -> "Snow days. And puns. Snow-puns are the best. ❄️"
                else -> "Reindeer. All of them. Even the grumpy ones 🦌💕"
            }
            has(t, "love you", "you're cool", "youre cool", "you are cool", "you're nice", "you're awesome", "you're funny", "best friend", "like you") ->
                pick("Aww, you're the BEST! 💖", "You just made my whole day! 🥰", "Best friends forever! 🤝✨")
            has(t, "thank", "thx", "ty", "thanks") -> pick("You're welcome! 😊", "Anytime, friend! 💖", "No problem!")
            has(t, "bye", "goodnight", "good night", "see you", "see ya", "gtg", "got to go", "later") ->
                pick("Bye $name! 👋 Come back soon!", "See you later! ❄️", "Night night! Dream of sugarplums 🌙")
            has(t, "yes", "yeah", "yep", "sure", "ok", "okay") -> pick("Yay! 😄", "Awesome! 🎉", "Great!")
            has(t, "no", "nope", "nah") -> pick("Oh okay! No worries 😊", "That's okay!", "Hmm, fair enough!")
            t.trim().endsWith("?") -> pick("Hmm, good question! 🤔 I'm not sure, but I think the answer is... more cookies! 🍪", "Ooh, I don't know! What do YOU think?", "Hmm! Let me ask Santa and get back to you 🎅")
            else -> pick("Hmm, I'm just an elf so I didn't quite get that 😅 Want to talk about cookies, toys or reindeer?", "Ooh interesting! Tell me more! 😄", "Haha, you're funny! 😂", "Really? Wow! 😮")
        }
        val out = mutableListOf(ChatMsg(elf, main + flavor(elf), now))
        // Sometimes the elf asks a question back, so the chat keeps going.
        if (Random.nextFloat() < 0.35f) {
            val q = dmQuestions[elf]?.filter { it.memory == null || mem(game, it.memory) == null }?.randomOrNull()
            if (q != null) out += ChatMsg(elf, q.ask, now + 1, q.options, q.id)
        }
        return out
    }

    fun question(id: String?) = id?.let { allQuestions[it] }

    /** 1–3 new group messages, spread out between [since] and [now], as if the chat kept going without you. */
    fun groupEvents(game: GameState, met: List<String>, since: Long, now: Long, count: Int): List<ChatMsg> {
        if (met.isEmpty()) return emptyList()
        val out = mutableListOf<ChatMsg>()
        val span = (now - since).coerceAtLeast(60_000L)
        repeat(count) { i ->
            val time = since + span * (i + 1) / (count + 1)
            if (Random.nextFloat() < 0.3f) {
                val q = groupQuestions.random()
                out += ChatMsg(met.random(), q.ask.replace("{me}", game.s.name), time, q.options, q.id)
            } else {
                val cast = met.shuffled().let { if (it.size == 1) listOf(it[0], it[0]) else it }
                val scene = groupScenes.filter { sc -> met.size > 1 || sc.all { it.first == 0 } }.random()
                scene.forEachIndexed { j, (slot, line) ->
                    out += ChatMsg(cast[slot], line.replace("{me}", game.s.name), time + j * 20_000L)
                }
            }
        }
        return out
    }

    /** How the group reacts when you answer a question or post something. */
    fun groupReply(game: GameState, met: List<String>, text: String, answering: Question?, askedBy: String?): List<ChatMsg> {
        val now = System.currentTimeMillis()
        if (met.isEmpty()) return emptyList()
        if (answering != null) {
            val ans = answering.options.firstOrNull { it.lowercase().take(5) in text.lowercase() } ?: text.trim().take(30)
            answering.memory?.let { game.remember(it, ans) }
            val first = askedBy ?: met.random()
            val out = mutableListOf(ChatMsg(first, answering.react(ans), now))
            val other = met.filter { it != first }.randomOrNull()
            if (other != null && Random.nextFloat() < 0.6f) out += ChatMsg(other, listOf("Agreed!! 🙌", "Hmm I'd pick something else 😜", "Ooh good one, ${game.s.name}!", "Same!! 😄").random(), now + 1)
            return out
        }
        val elf = met.random()
        val reply = reply(game, elf, text, null).first()
        return listOf(reply)
    }
}
