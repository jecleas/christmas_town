package com.lexi.christmastown

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class Plot(val id: Int, val crop: Crop? = null, val wateredAt: Long? = null) {
    fun progress(now: Long): Float {
        if (crop == null || wateredAt == null) return 0f
        return ((now - wateredAt) / 1000f / crop.growSeconds).coerceIn(0f, 1f)
    }
    fun stage(now: Long) = when {
        crop == null -> 0          // empty
        wateredAt == null -> 1     // needs water
        progress(now) < 1f -> 2    // growing
        else -> 3                  // ready
    }
}

sealed interface Overlay {
    data object None : Overlay
    data class Paid(val newLevel: Int?) : Overlay
    data class Morning(val day: Int, val worked: Boolean) : Overlay
    data object ChristmasEve : Overlay
    data object Won : Overlay
}

/** Everything that gets saved. */
@Serializable data class Save(
    val name: String = "",
    val look: ElfLook = ElfLook(),
    val hasElf: Boolean = false,
    val hair: Set<HairStyle> = setOf(HairStyle.Short, HairStyle.Long),
    val outfits: Set<Outfit> = setOf(Outfit.Tunic),
    val hats: Set<Hat> = setOf(Hat.None, Hat.Elf),
    val accessories: Set<Accessory> = setOf(Accessory.None),
    val money: Int = 1000,
    val day: Int = 1,
    val workdays: Int = 0,
    val energy: Int = 100,
    val paidToday: Boolean = false,
    val christmasDone: Boolean = false,
    val seeds: Map<Crop, Int> = mapOf(Crop.Wheat to 4, Crop.Carrot to 4),
    val crops: Map<Crop, Int> = mapOf(Crop.Wheat to 4, Crop.Carrot to 4),
    val cookies: Map<Cookie, Int> = emptyMap(),
    val toys: Map<Toy, Int> = emptyMap(),
    val food: Map<Food, Int> = mapOf(Food.HotCocoa to 1),
    val plots: List<Plot> = (0 until 6).map { Plot(it) },
    val fed: Set<String> = emptySet(),
    val brushed: Set<String> = emptySet(),
    val owned: Map<Item, Int> = startingHouse.groupingBy { it.item }.eachCount(),
    val placed: List<Placed> = startingHouse,
    val hearts: Map<String, Int> = emptyMap(),
    val talkedToday: Set<String> = emptySet(),
    val partyDone: Boolean = false,
    val bestGifts: Set<String> = emptySet(),
    val jobs: List<Job> = jobsFor(1),
    val kids: List<Kid> = makeKids(5),
    val kidIndex: Int = 0,
    // Phone chats
    val chats: Map<String, List<ChatMsg>> = emptyMap(),
    val lastRead: Map<String, Long> = emptyMap(),
    val lastGroupVisit: Long = 0,
    val memory: Map<String, String> = emptyMap(),
)

fun <K> Map<K, Int>.n(k: K) = this[k] ?: 0
fun <K> Map<K, Int>.add(k: K, d: Int) = this + (k to (n(k) + d))

class GameState(private val prefs: SharedPreferences) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    var s by mutableStateOf(load())
        private set

    // Right-now stuff (not saved)
    /** null = the main screen (your living room). */
    var place by mutableStateOf<Place?>(null)
    var overlay by mutableStateOf<Overlay>(Overlay.None)
    var toast by mutableStateOf<String?>(null)
    var showCreator by mutableStateOf(false)
    var showJobs by mutableStateOf(false)
    var delivering by mutableStateOf(false)
    var christmasEveRide by mutableStateOf(false)
    var selectedSeed by mutableStateOf(Crop.Wheat)
    var now by mutableLongStateOf(System.currentTimeMillis())
    private var toastUntil = 0L

    val level get() = levelFor(s.workdays)
    val levelTitle get() = levelTitles[level - 1]
    val reindeer get() = reindeerNames.take(reindeerCount(level))
    val crops get() = Crop.entries.filter { it.unlockLevel <= level }
    val jobsDone get() = s.jobs.count { it.finished }

    private fun update(change: Save.() -> Save) {
        s = s.change()
        prefs.edit().putString("save", json.encodeToString(s)).apply()
    }

    private fun load(): Save = prefs.getString("save", null)?.let {
        runCatching { json.decodeFromString<Save>(it) }.getOrNull()
    } ?: Save()

    fun tick() {
        now = System.currentTimeMillis()
        if (toast != null && now > toastUntil) toast = null
    }

    fun say(msg: String) {
        toast = msg
        toastUntil = System.currentTimeMillis() + 3000
    }

    // MARK: Energy

    private fun useEnergy(amount: Int): Boolean {
        if (s.energy < amount) {
            say("Too tired! 😴 Eat a cookie or some food, or go to sleep.")
            return false
        }
        update { copy(energy = energy - amount) }
        return true
    }

    fun eat(food: Food) {
        if (s.food.n(food) <= 0) return
        if (s.energy >= 100) { say("You're already full of energy! ⚡"); return }
        update { copy(food = this.food.add(food, -1), energy = (energy + food.energy).coerceAtMost(100)) }
        say("Yum! +${food.energy} energy ⚡")
    }

    fun eat(cookie: Cookie) {
        if (s.cookies.n(cookie) <= 0) return
        if (s.energy >= 100) { say("You're already full of energy! ⚡"); return }
        update { copy(cookies = cookies.add(cookie, -1), energy = (energy + cookie.energy).coerceAtMost(100)) }
        say("Crunch! +${cookie.energy} energy ⚡")
    }

    // MARK: Jobs & pay

    private fun progress(kind: JobKind, amount: Int = 1) {
        if (s.paidToday) return
        update { copy(jobs = jobs.map { if (it.kind == kind) it.copy(done = (it.done + amount).coerceAtMost(it.goal)) else it }) }
        if (s.jobs.all { it.finished }) payDay()
    }

    private fun payDay() {
        val before = level
        update { copy(money = money + PAY, paidToday = true, workdays = workdays + 1) }
        overlay = Overlay.Paid(if (level > before) level else null)
        if (level >= 3) update { copy(plots = plots + (plots.size until 9).map { Plot(it) }) }
    }

    fun afterPaid() {
        overlay = if (s.workdays >= CHRISTMAS_EVE_WORKDAYS && !s.christmasDone) Overlay.ChristmasEve else Overlay.None
    }

    fun sleep() {
        val worked = s.paidToday
        update {
            copy(
                day = day + 1, energy = 100, paidToday = false, fed = emptySet(), brushed = emptySet(),
                talkedToday = emptySet(), jobs = jobsFor(levelFor(workdays)), kids = makeKids(5), kidIndex = 0,
            )
        }
        overlay = Overlay.Morning(s.day, worked)
        place = null
    }

    // MARK: Greenhouse

    fun tapPlot(id: Int) {
        val plot = s.plots.firstOrNull { it.id == id } ?: return
        when (plot.stage(System.currentTimeMillis())) {
            0 -> {
                val c = selectedSeed
                if (s.seeds.n(c) <= 0) { say("No ${c.title.lowercase()} seeds! Buy some at the Shop 🛍️"); return }
                update { copy(seeds = seeds.add(c, -1), plots = plots.map { if (it.id == id) it.copy(crop = c) else it }) }
            }
            1 -> update { copy(plots = plots.map { if (it.id == id) it.copy(wateredAt = System.currentTimeMillis()) else it }) }
            2 -> say("Still growing… 🌱")
            3 -> {
                val c = plot.crop!!
                update { copy(crops = crops.add(c, HARVEST), plots = plots.map { if (it.id == id) Plot(id) else it }) }
                say("Picked $HARVEST ${c.plural}!")
            }
        }
    }

    fun plantAll() {
        val c = selectedSeed
        var n = 0
        update {
            var seedsLeft = seeds.n(c)
            val newPlots = plots.map { if (it.crop == null && seedsLeft > 0) { seedsLeft--; n++; it.copy(crop = c) } else it }
            copy(plots = newPlots, seeds = seeds + (c to seedsLeft))
        }
        say(if (n == 0) "No empty pots, or no ${c.title.lowercase()} seeds!" else "Planted $n seeds 🌱")
    }

    fun waterAll() {
        val t = System.currentTimeMillis()
        update { copy(plots = plots.map { if (it.stage(t) == 1) it.copy(wateredAt = t) else it }) }
    }

    fun pickAll() {
        val t = System.currentTimeMillis()
        var picked = 0
        update {
            var c = crops
            val p = plots.map { if (it.stage(t) == 3) { c = c.add(it.crop!!, HARVEST); picked += HARVEST; Plot(it.id) } else it }
            copy(plots = p, crops = c)
        }
        say(if (picked == 0) "Nothing is ready yet 🌱" else "Picked $picked crops!")
    }

    // MARK: Bakery

    fun canBake(c: Cookie) = c.recipe.all { (crop, n) -> s.crops.n(crop) >= n }

    fun bake(c: Cookie): Boolean {
        if (!canBake(c)) { say("Not enough ingredients! Grow some in the Greenhouse 🌱"); return false }
        if (!useEnergy(5)) return false
        update {
            var cr = crops
            c.recipe.forEach { (crop, n) -> cr = cr.add(crop, -n) }
            copy(crops = cr, cookies = cookies.add(c, 1))
        }
        progress(JobKind.Bake)
        return true
    }

    // MARK: Workshop

    fun startToy(): Boolean = useEnergy(5)

    fun finishToy(t: Toy) {
        update { copy(toys = toys.add(t, 1)) }
        say("You made a ${t.title}! 🎁")
        progress(JobKind.Toys)
    }

    // MARK: Stable

    fun feed(name: String) {
        if (name in s.fed) { say("$name is full! 🥕"); return }
        if (s.crops.n(Crop.Carrot) <= 0) { say("No carrots! Grow some in the Greenhouse 🥕"); return }
        if (!useEnergy(2)) return
        update { copy(fed = fed + name, crops = crops.add(Crop.Carrot, -1)) }
        say("$name munches happily! 🥕")
        checkReindeer()
    }

    fun brush(name: String) {
        if (name in s.brushed) { say("$name is already super shiny! ✨"); return }
        if (!useEnergy(2)) return
        update { copy(brushed = brushed + name) }
        say("$name looks so shiny! ✨")
        checkReindeer()
    }

    private fun checkReindeer() {
        if (reindeer.all { it in s.fed && it in s.brushed }) progress(JobKind.Reindeer)
    }

    // MARK: Santa's list

    val currentKid get() = s.kids.getOrNull(s.kidIndex)

    /** Returns true if the kid was sorted correctly. */
    fun sortKid(nice: Boolean): Boolean {
        val kid = currentKid ?: return true
        if (!useEnergy(1)) return true
        update { copy(kidIndex = kidIndex + 1) }
        progress(JobKind.SortList)
        return kid.nice == nice
    }

    // MARK: Delivery

    val presents get() = s.toys.values.sum()

    /** Uses one toy (or one of Santa's on Christmas Eve). */
    fun deliver(): Boolean {
        if (christmasEveRide) return true
        val toy = s.toys.entries.firstOrNull { it.value > 0 }?.key
        if (toy == null) { say("No presents left! Make toys in the Workshop 🧸"); return false }
        update { copy(toys = toys.add(toy, -1)) }
        progress(JobKind.Deliver)
        return true
    }

    fun startDelivery() {
        val job = s.jobs.firstOrNull { it.kind == JobKind.Deliver }
        when {
            job == null -> say("Deliveries start at Level 3! 🛷")
            job.finished -> say("All of today's presents are delivered! 🎉")
            !useEnergy(10) -> {}
            else -> delivering = true
        }
    }

    fun startChristmasEve() {
        overlay = Overlay.None
        christmasEveRide = true
        delivering = true
    }

    fun endDelivery(finishedChristmas: Boolean) {
        delivering = false
        if (christmasEveRide) {
            christmasEveRide = false
            if (finishedChristmas) {
                update { copy(christmasDone = true) }
                overlay = Overlay.Won
            }
        }
    }

    // MARK: Shop

    private fun pay(price: Int): Boolean {
        if (s.money < price) { say("Not enough money! 💸 Work for Santa to earn \$1,000 a day."); return false }
        update { copy(money = money - price) }
        return true
    }

    fun buyItem(i: Item) { if (pay(i.price)) { update { copy(owned = owned.add(i, 1)) }; say("Bought ${i.title}! Place it at Home 🏠") } }
    fun buyFood(f: Food) { if (pay(f.price)) update { copy(food = food.add(f, 1)) } }
    fun buySeed(c: Crop) {
        if (c.unlockLevel > level) { say("${c.title} seeds unlock at Level ${c.unlockLevel} 🔒"); return }
        if (pay(c.seedPrice)) update { copy(seeds = seeds.add(c, 1)) }
    }
    fun buyHair(h: HairStyle) { if (h !in s.hair && pay(h.price)) { update { copy(hair = hair + h, look = look.copy(hairStyle = h)) }; say("New hairstyle! 💇") } }
    fun buyOutfit(o: Outfit) { if (o !in s.outfits && pay(o.price)) { update { copy(outfits = outfits + o, look = look.copy(outfit = o)) }; say("Ooh, stylish! 👗") } }
    fun buyHat(h: Hat) { if (h !in s.hats && pay(h.price)) { update { copy(hats = hats + h, look = look.copy(hat = h)) }; say("Nice hat! 🎩") } }
    fun buyAccessory(a: Accessory) { if (a !in s.accessories && pay(a.price)) { update { copy(accessories = accessories + a, look = look.copy(accessory = a)) }; say("So cute! ✨") } }

    fun setLook(look: ElfLook) = update { copy(look = look) }

    fun saveElf(name: String, look: ElfLook) {
        update { copy(name = name.trim().ifEmpty { "Lexi" }.take(12), look = look, hasElf = true) }
        showCreator = false
    }

    // MARK: House

    fun available(i: Item) = s.owned.n(i) - s.placed.count { it.item == i }

    fun place(i: Item, floor: Int): Long? {
        if (available(i) <= 0) return null
        val id = (s.placed.maxOfOrNull { it.id } ?: 0) + 1
        update { copy(placed = placed + Placed(id, i, floor, 0.5f, 0.3f)) }
        return id
    }

    fun move(id: Long, x: Float, y: Float) = update {
        copy(placed = placed.map { if (it.id == id) it.copy(x = x.coerceIn(0.04f, 0.96f), y = y.coerceIn(0f, 0.7f)) else it })
    }

    fun switchFloor(id: Long) = update { copy(placed = placed.map { if (it.id == id) it.copy(floor = 1 - it.floor) else it }) }
    fun putAway(id: Long) = update { copy(placed = placed.filter { it.id != id }) }

    // MARK: Elf friends

    fun hearts(npc: Npc) = s.hearts.n(npc.name)

    fun chat(npc: Npc) {
        if (npc.name !in s.talkedToday) update { copy(hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }, talkedToday = talkedToday + npc.name) }
        checkBestFriend(npc)
    }

    fun giveCookie(npc: Npc) {
        val cookie = s.cookies.entries.firstOrNull { it.value > 0 }?.key
        if (cookie == null) { say("You don't have any cookies! Bake some 🍪"); return }
        update { copy(cookies = cookies.add(cookie, -1), hearts = hearts.add(npc.name, 2).mapValues { it.value.coerceAtMost(5) }) }
        say("${npc.name} loves the ${cookie.title}! 💖")
        checkBestFriend(npc)
    }

    private fun checkBestFriend(npc: Npc) {
        if (hearts(npc) >= 5 && npc.name !in s.bestGifts) {
            update { copy(bestGifts = bestGifts + npc.name, owned = owned.add(npc.bestFriendGift, 1)) }
            say("${npc.name} is your BEST FRIEND! They gave you a ${npc.bestFriendGift.title}! 🎁")
        }
    }

    fun throwParty(reward: Item) {
        update { copy(partyDone = true, owned = owned.add(reward, 1), hearts = hearts.add("Twinkle", 1).mapValues { it.value.coerceAtMost(5) }) }
        say("Best party ever! 🎉 You got a ${reward.title} for your house!")
    }

    fun go(p: Place?) { place = p }

    // MARK: Phone

    /** Elves you've met (talked to in Friends) are in your phone. */
    val met get() = npcs.filter { s.hearts.n(it.name) > 0 }.map { it.name }

    fun messages(thread: String) = s.chats[thread].orEmpty()

    fun unread(thread: String) = messages(thread).count { it.from != ME && it.time > s.lastRead.n(thread).toLong() }

    private fun Map<String, Long>.n(k: String) = this[k] ?: 0L

    /** The group chat feels "live": time passes while you're away, so there are new messages when you look. */
    val groupHasNews get() = met.isNotEmpty() && System.currentTimeMillis() - s.lastGroupVisit > 120_000

    fun remember(key: String, value: String) = update { copy(memory = memory + (key to value)) }

    fun post(thread: String, msgs: List<ChatMsg>) {
        if (msgs.isEmpty()) return
        update { copy(chats = chats + (thread to (chats[thread].orEmpty() + msgs).takeLast(80))) }
    }

    fun markRead(thread: String) = update { copy(lastRead = lastRead + (thread to System.currentTimeMillis())) }

    /** Called when you open the group chat: 1–3 things happened while you were away. */
    fun openGroup() {
        val now = System.currentTimeMillis()
        val since = s.lastGroupVisit
        when {
            met.isEmpty() -> {}
            since == 0L -> post(GROUP, met.mapIndexed { i, n -> ChatMsg(n, listOf("Welcome to the group chat, ${s.name}! 🎄", "Yay, ${s.name} is here!! ✨", "Hi hi hi! 👋", "Hello! 🦌").random(), now - (met.size - i) * 30_000L) })
            now - since > 60_000 -> {
                val count = if (now - since > 30 * 60_000) 3 else (1..3).random()
                post(GROUP, ElfBrain.groupEvents(this, met, since, now, count))
            }
        }
        update { copy(lastGroupVisit = now) }
    }

    /** While you're watching, the chat sometimes keeps going. */
    fun liveGroupEvent() {
        val now = System.currentTimeMillis()
        post(GROUP, ElfBrain.groupEvents(this, met, now - 30_000, now, 1).map { it.copy(time = minOf(it.time, now)) })
        update { copy(lastGroupVisit = now) }
    }

    /** Checks what you typed. Returns the cleaned text and, if needed, the elf's safety reply. */
    fun cleanMessage(text: String) = ChatFilter.check(text)

    /** Elves get a little friendlier when you chat (once a day each). */
    fun chatFriendship(elf: String) {
        npcs.firstOrNull { it.name == elf }?.let { chat(it) }
    }
}
