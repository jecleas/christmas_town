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
    data class Paid(val newLevel: Int?, val earned: Int = 0) : Overlay
    data class Morning(val day: Int, val worked: Boolean, val interest: Int = 0) : Overlay
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
    // Settings
    val musicOn: Boolean = true,
    val soundOn: Boolean = true,
    // Expansion (all with defaults so older saves still load)
    val wrapped: Int = 0,
    val townTree: List<Ornament> = emptyList(),
    val snowballDay: Int = 0,
    val gingerHouses: Int = 0,
    val pets: List<Pet> = emptyList(),
    val washed: Set<String> = emptySet(),
    val decos: Set<Deco> = emptySet(),
    val reindeerDecos: Map<String, Set<Deco>> = emptyMap(),
    val team: List<String> = emptyList(),
    val adventYear: Int = 0,
    val adventOpened: Set<Int> = emptySet(),
    val stickers: Set<String> = emptySet(),
    val stickersSeen: Int = 0,
    val prizesClaimed: Set<Int> = emptySet(),
    // House & main pet (follow-up)
    val floors: Int = START_FLOORS,
    val mainPet: Long = 0,
    // Party Place (Level 7)
    val moviesSeen: Set<String> = emptySet(),
    val balloonBest: Int = 0,
    val juiceShared: Set<String> = emptySet(),
    // Pay: every 2 jobs = $2,000, sleigh = $1,000 extra (all reset each day)
    val paidPairs: Int = 0,
    val sleighBonusPaid: Boolean = false,
    val earnedToday: Int = 0,
    // Bank
    val bankSaved: Int = 0,
    // Town: friends visiting your house today, and houses you've visited today
    val homeGuests: List<String> = emptyList(),
    val visitedToday: Set<String> = emptySet(),
    val dancedDay: Int = 0,
    // Workmates you worked with / who helped you today
    val workedWith: Set<String> = emptySet(),
    val helpedBy: Set<String> = emptySet(),
    val servedToday: Set<String> = emptySet(),
    val ateWith: Set<String> = emptySet(),
    val playedWith: Set<String> = emptySet(),
    // Favours, mailbox, birthdays (refreshed each new day)
    val dailyDay: Int = 0,
    val favours: List<Favour> = emptyList(),
    val letters: List<Letter> = emptyList(),
    val birthdayWished: Set<String> = emptySet(),
    // Snow Park games and the photo booth
    val skateDay: Int = 0,
    val sledDay: Int = 0,
    val photos: List<Photo> = emptyList(),
    // Your birthday (month 1-12, day 1-31; 0 = not set) and Elf's Restaurant
    val myBirthMonth: Int = 0,
    val myBirthDay: Int = 0,
    val birthdayPartyYear: Int = 0,
    val ateOutDay: Int = 0,
)

/** Stickers you've earned just by what's in your save (things you own, pets, friends...). */
fun Save.withStickers(): Save {
    val earned = buildSet {
        cookies.filter { it.value > 0 }.keys.forEach { add("cookie:${it.name}") }
        toys.filter { it.value > 0 }.keys.forEach { add("toy:${it.name}") }
        (fed intersect brushed intersect washed).forEach { add("deer:$it") }
        pets.forEach { add("pet:${it.kind.name}") }
        owned.filter { it.value > 0 }.keys.forEach { add("item:${it.name}") }
        if (wrapped > 0) add("wrap")
        if (gingerHouses > 0) add("ginger")
        if (townTree.isNotEmpty()) add("tree")
        if (bestGifts.isNotEmpty()) add("friend")
        if (levelFor(workdays) >= MAX_LEVEL) add("level8")
        if (christmasDone) add("christmas")
        if (adventOpened.isNotEmpty()) add("advent")
        if (hearts.count { it.value > 0 } >= 10) add("friends10")
    }
    return if (stickers.containsAll(earned)) this else copy(stickers = stickers + earned)
}

/** One ornament hung on the Town Tree (x/y are fractions of the tree picture). */
@Serializable data class Ornament(val kind: Int, val color: Int, val x: Float, val y: Float)

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
    /** A mini-game can ask for its own song while it's on screen. */
    var gameSong by mutableStateOf<Music?>(null)
    private var toastUntil = 0L

    val level get() = levelFor(s.workdays)
    val levelTitle get() = levelTitles[level - 1]
    val reindeer get() = reindeerNames.take(reindeerCount(level))
    val crops get() = Crop.entries.filter { it.unlockLevel <= level }
    val jobsDone get() = s.jobs.count { it.finished }

    private fun update(change: Save.() -> Save) {
        s = s.change().withStickers()
        prefs.edit().putString("save", json.encodeToString(s)).apply()
    }

    private fun load(): Save = (prefs.getString("save", null)?.let {
        runCatching { json.decodeFromString<Save>(it) }.getOrNull()
    } ?: Save()).withStickers()

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
            Audio.play(Sfx.Oops)
            return false
        }
        update { copy(energy = energy - amount) }
        return true
    }

    /** Uses energy for a mini-game (shows "too tired" if you can't). */
    fun spend(amount: Int): Boolean = useEnergy(amount)

    /** Checks (without using) energy, so mini-games can start only if you can finish them. */
    fun hasEnergy(amount: Int): Boolean {
        if (s.energy >= amount) return true
        say("Too tired! 😴 Eat a cookie or some food, or go to sleep.")
        Audio.play(Sfx.Oops)
        return false
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
        val wasDone = s.jobs.firstOrNull { it.kind == kind }?.finished == true
        update { copy(jobs = jobs.map { if (it.kind == kind) it.copy(done = (it.done + amount).coerceAtMost(it.goal)) else it }) }
        if (!wasDone && s.jobs.firstOrNull { it.kind == kind }?.finished == true) jobFinished(kind)
    }

    /** Pay as you go: every 2 finished jobs pays $2,000, and the sleigh deliveries pay $1,000 extra. */
    private fun jobFinished(kind: JobKind) {
        workTogether(kind)
        val pairs = jobsDone / 2
        val forPairs = (pairs - s.paidPairs).coerceAtLeast(0) * PAY_PER_TWO_JOBS
        val sleigh = if (kind == JobKind.Deliver && !s.sleighBonusPaid) SLEIGH_BONUS else 0
        val earned = forPairs + sleigh
        update { copy(money = money + earned, paidPairs = maxOf(paidPairs, pairs), sleighBonusPaid = sleighBonusPaid || sleigh > 0, earnedToday = earnedToday + earned) }
        if (s.jobs.all { it.finished }) { dayDone(earned); return }
        when {
            forPairs > 0 && sleigh > 0 -> say("Santa paid you ${money(forPairs)} for 2 jobs + ${money(sleigh)} sleigh bonus! 💰🛷")
            forPairs > 0 -> say("2 jobs done! Santa paid you ${money(forPairs)}! 💰")
            sleigh > 0 -> say("Sleigh bonus! Santa paid you ${money(sleigh)} extra! 🛷💰")
            else -> say("Job done! ✅ Finish one more to get ${money(PAY_PER_TWO_JOBS)}!")
        }
        Audio.play(if (earned > 0) Sfx.Coins else Sfx.Ding)
    }

    /** Every job finished: the day counts toward levelling up and Christmas Eve. */
    private fun dayDone(earned: Int) {
        val before = level
        update { copy(paidToday = true, workdays = workdays + 1) }
        overlay = Overlay.Paid(if (level > before) level else null, earned)
        Audio.play(if (level > before) Sfx.LevelUp else Sfx.Coins)
        if (level >= 3) update { copy(plots = plots + (plots.size until 9).map { Plot(it) }) }
    }

    fun afterPaid() {
        overlay = if (s.workdays >= CHRISTMAS_EVE_WORKDAYS && !s.christmasDone) Overlay.ChristmasEve else Overlay.None
    }

    fun sleep() {
        val worked = s.paidToday
        val interest = bankInterest(s.bankSaved)
        update {
            copy(
                day = day + 1, energy = 100, paidToday = false, fed = emptySet(), brushed = emptySet(),
                talkedToday = emptySet(), jobs = jobsFor(levelFor(workdays)), kids = makeKids(listOf(5, 5, 5, 5, 5, 5, 6, 6)[levelFor(workdays) - 1]), kidIndex = 0,
                washed = emptySet(), juiceShared = emptySet(), paidPairs = 0, sleighBonusPaid = false, earnedToday = 0, bankSaved = bankSaved + interest, homeGuests = emptyList(), visitedToday = emptySet(), workedWith = emptySet(), helpedBy = emptySet(), servedToday = emptySet(), ateWith = emptySet(), playedWith = emptySet(), birthdayWished = emptySet(),
            )
        }
        overlay = Overlay.Morning(s.day, worked, interest)
        birthdayDance = false
        newDay()
        place = null
    }

    // MARK: Greenhouse

    fun tapPlot(id: Int) {
        val plot = s.plots.firstOrNull { it.id == id } ?: return
        when (plot.stage(System.currentTimeMillis())) {
            0 -> {
                val c = selectedSeed
                if (s.seeds.n(c) <= 0) { say("No ${c.title.lowercase()} seeds! Buy some at the Shop 🛍️"); Audio.play(Sfx.Oops); return }
                Audio.play(Sfx.Pop)
                update { copy(seeds = seeds.add(c, -1), plots = plots.map { if (it.id == id) it.copy(crop = c) else it }) }
            }
            1 -> { update { copy(plots = plots.map { if (it.id == id) it.copy(wateredAt = System.currentTimeMillis()) else it }) }; Audio.play(Sfx.Tap) }
            2 -> say("Still growing… 🌱")
            3 -> {
                val c = plot.crop!!
                update { copy(crops = crops.add(c, HARVEST), plots = plots.map { if (it.id == id) Plot(id) else it }) }
                say("Picked $HARVEST ${c.plural}!")
                Audio.play(Sfx.Pop)
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
        if (!canBake(c)) { say("Not enough ingredients! Grow some in the Greenhouse 🌱"); Audio.play(Sfx.Oops); return false }
        if (!useEnergy(5)) return false
        update {
            var cr = crops
            c.recipe.forEach { (crop, n) -> cr = cr.add(crop, -n) }
            copy(crops = cr, cookies = cookies.add(c, 1))
        }
        Audio.play(Sfx.Ding)
        progress(JobKind.Bake)
        return true
    }

    // MARK: Workshop

    fun startToy(): Boolean = useEnergy(5)

    fun finishToy(t: Toy) {
        update { copy(toys = toys.add(t, 1)) }
        say("You made a ${t.title}! 🎁")
        Audio.play(Sfx.Ding)
        progress(JobKind.Toys)
    }

    // MARK: Stable

    fun feed(name: String) {
        if (name in s.fed) { say("$name is full! 🥕"); return }
        if (s.crops.n(Crop.Carrot) <= 0) { say("No carrots! Grow some in the Greenhouse 🥕"); Audio.play(Sfx.Oops); return }
        if (!useEnergy(2)) return
        update { copy(fed = fed + name, crops = crops.add(Crop.Carrot, -1)) }
        say("$name munches happily! 🥕")
        Audio.play(Sfx.Munch)
        checkReindeer()
    }

    fun brush(name: String) {
        if (name in s.brushed) { say("$name is already super shiny! ✨"); return }
        if (!useEnergy(2)) return
        update { copy(brushed = brushed + name) }
        say("$name looks so shiny! ✨")
        Audio.play(Sfx.SleighBells)
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
        Audio.play(if (kid.nice == nice) Sfx.Ding else Sfx.Oops)
        return kid.nice == nice
    }

    // MARK: Delivery

    val presents get() = s.toys.values.sum() + s.wrapped

    /** Uses one present: a wrapped one first, then a plain toy (or one of Santa's on Christmas Eve). */
    fun deliver(): Boolean {
        if (christmasEveRide) { Audio.play(Sfx.PresentDrop); return true }
        val toy = s.toys.entries.firstOrNull { it.value > 0 }?.key
        if (s.wrapped > 0) update { copy(wrapped = wrapped - 1) }
        else if (toy != null) update { copy(toys = toys.add(toy, -1)) }
        else { say("No presents left! Make toys in the Workshop 🧸"); Audio.play(Sfx.Oops); return false }
        Audio.play(Sfx.PresentDrop)
        progress(JobKind.Deliver)
        return true
    }

    fun startDelivery() {
        val job = s.jobs.firstOrNull { it.kind == JobKind.Deliver }
        when {
            job == null -> say("Deliveries start at Level 3! 🛷")
            job.finished -> say("All of today's presents are delivered! 🎉")
            !useEnergy(10) -> {}
            else -> { delivering = true; Audio.play(Sfx.SleighBells) }
        }
    }

    fun startChristmasEve() {
        overlay = Overlay.None
        christmasEveRide = true
        delivering = true
        Audio.play(Sfx.SleighBells)
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

    // MARK: Reindeer care (wash, decorate, harness)

    fun wash(name: String) {
        if (name in s.washed) return
        if (!useEnergy(2)) return
        update { copy(washed = washed + name) }
        say("$name is squeaky clean! 🛁")
        Audio.play(Sfx.Ding)
    }

    fun decosOn(name: String) = s.reindeerDecos[name].orEmpty()

    fun buyDeco(d: Deco) {
        if (d in s.decos) return
        if (pay(d.price)) { update { copy(decos = decos + d) }; say("You got ${d.title}! ${d.emoji} Tap it to put it on a reindeer.") }
    }

    fun toggleDeco(name: String, d: Deco) {
        if (d !in s.decos) { buyDeco(d); if (d !in s.decos) return }
        val on = decosOn(name)
        update { copy(reindeerDecos = reindeerDecos + (name to (if (d in on) on - d else on + d))) }
        Audio.play(Sfx.SleighBells)
    }

    /** The reindeer pulling the sleigh (up to [TEAM_SIZE]). If you never picked, the first ones go. */
    val sleighTeam get() = s.team.filter { it in reindeer }.ifEmpty { reindeer.take(TEAM_SIZE) }

    fun toggleTeam(name: String) {
        val t = s.team.filter { it in reindeer }
        when {
            name in t -> update { copy(team = t - name) }
            t.size >= TEAM_SIZE -> { say("The sleigh only fits $TEAM_SIZE reindeer! Unhitch one first."); Audio.play(Sfx.Oops) }
            else -> { update { copy(team = t + name) }; say("$name is hitched to the sleigh! 🛷"); Audio.play(Sfx.SleighBells) }
        }
    }

    /** 0..TEAM_SIZE: how many sleigh reindeer are fed and brushed today. Happy reindeer steer better. */
    val teamPower get() = sleighTeam.count { it in s.fed && it in s.brushed }

    // MARK: Wrapping presents

    fun startWrap(toy: Toy): Boolean {
        if (s.toys.n(toy) <= 0) { say("You don't have a ${toy.title}! Make one in the Workshop 🧸"); Audio.play(Sfx.Oops); return false }
        return hasEnergy(3)
    }

    fun finishWrap(toy: Toy) {
        if (s.toys.n(toy) <= 0 || !useEnergy(3)) return
        update { copy(toys = toys.add(toy, -1), wrapped = wrapped + 1) }
        say("A beautiful present! 🎁 Wrapped presents go on the sleigh first.")
        Audio.play(Sfx.Ding)
        progress(JobKind.Wrap)
    }

    // MARK: Gingerbread house

    val gingerRecipe = mapOf(Crop.Wheat to 3)
    fun canBuildHouse() = gingerRecipe.all { (c, n) -> s.crops.n(c) >= n }

    fun finishGingerHouse() {
        if (!canBuildHouse() || !useEnergy(5)) return
        update {
            var cr = crops
            gingerRecipe.forEach { (c, n) -> cr = cr.add(c, -n) }
            copy(crops = cr, owned = owned.add(Item.GingerbreadHouse, 1), gingerHouses = gingerHouses + 1)
        }
        say("Your gingerbread house is in storage. Put it out at Home! 🏠")
        Audio.play(Sfx.LevelUp)
    }

    // MARK: Town tree

    fun finishTree(ornaments: List<Ornament>) {
        update { copy(townTree = ornaments) }
        say("The Town Tree looks AMAZING! 🎄✨")
        Audio.play(Sfx.LevelUp)
        progress(JobKind.Tree)
    }

    // MARK: Snowball fight

    /** Friends you play with get a little closer (once a day). */
    fun snowballDone(hits: Int, friends: List<Npc>) {
        if (s.snowballDay != s.day && hits >= 3) {
            update { copy(snowballDay = day, hearts = friends.fold(hearts) { h, f -> h.add(f.name, 1) }.mapValues { it.value.coerceAtMost(5) }) }
            say("What a snowball fight! Your friends had a blast 💖")
            friends.forEach { checkBestFriend(it) }
        } else say(if (hits >= 3) "Snow much fun! ❄️" else "Brrr! Try again and hit 3 friends! ❄️")
    }

    // MARK: Pets

    fun buyPet(kind: PetKind) {
        if (pay(kind.price)) {
            val name = (petNames - s.pets.map { it.name }.toSet()).randomOrNull() ?: petNames.random()
            val id = (s.pets.maxOfOrNull { it.id } ?: 0) + 1
            update { copy(pets = pets + Pet(id, kind, name, fedAt = System.currentTimeMillis(), washedAt = System.currentTimeMillis())) }
            say("Meet $name the ${kind.title}! ${kind.noise} 💖")
        }
    }

    private fun petCare(id: Long, change: Pet.(Long) -> Pet) {
        val t = System.currentTimeMillis()
        update { copy(pets = pets.map { if (it.id == id) it.change(t) else it }) }
        val pet = s.pets.firstOrNull { it.id == id } ?: return
        // Good care reward: a little gift once a day when every meter is high.
        if (pet.fullness(t) > 0.8f && pet.cleanness(t) > 0.8f && pet.happiness(t) > 0.8f && pet.giftDay != s.day) {
            val gift = listOf(Food.Candy, Food.HotCocoa, Food.Candy).random()
            update { copy(pets = pets.map { if (it.id == id) it.copy(giftDay = day) else it }, food = food.add(gift, 1)) }
            say("${pet.name} is SO happy, they found you a ${gift.title}! 🎁")
            Audio.play(Sfx.Coins)
        }
    }

    fun feedPet(id: Long) = petCare(id) { copy(fedAt = it) }
    fun washPet(id: Long) = petCare(id) { copy(washedAt = it) }
    fun cuddlePet(id: Long) = petCare(id) { copy(pettedAt = it) }

    // MARK: Advent calendar

    private val today get() = java.time.LocalDate.now()
    /** Doors you may open: 1..today in December (none before Dec 1). Missed days can still be opened. */
    val adventDay get() = if (today.monthValue == 12) today.dayOfMonth.coerceAtMost(24) else 0
    val daysToAdvent get() = if (today.monthValue == 12) 0 else java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.of(today.year, 12, 1)).toInt()
    val adventOpenedThisYear get() = if (s.adventYear == today.year) s.adventOpened else emptySet()

    fun openAdvent(door: Int): Surprise? {
        if (door > adventDay || door in adventOpenedThisYear) return null
        val gift = adventSurprises[door - 1]
        val year = today.year
        update { gift.give(copy(adventYear = year, adventOpened = (if (adventYear == year) adventOpened else emptySet()) + door)) }
        Audio.play(Sfx.LevelUp)
        return gift
    }

    // MARK: Sticker book

    fun sticker(id: String) { if (id !in s.stickers) update { copy(stickers = stickers + id) } }
    val newStickers get() = (s.stickers.size - s.stickersSeen).coerceAtLeast(0)
    fun sawStickers() { if (s.stickersSeen != s.stickers.size) update { copy(stickersSeen = stickers.size) } }

    fun claimPrize(at: Int) {
        val prize = stickerPrizes.firstOrNull { it.first == at } ?: return
        if (s.stickers.size < at || at in s.prizesClaimed) return
        update { prize.second.give(copy(prizesClaimed = prizesClaimed + at)) }
        say("Sticker prize: ${prize.second.title}! 🎁")
        Audio.play(Sfx.LevelUp)
    }

    // MARK: Shop

    private fun pay(price: Int): Boolean {
        if (s.money < price) { say("Not enough money! 💸 Work for Santa: every 2 jobs pays \$2,000."); Audio.play(Sfx.Oops); return false }
        update { copy(money = money - price) }
        Audio.play(Sfx.Coins)
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
        newDay()
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

    /** Moves a thing to the next floor up (and back to the bottom from the top). */
    fun switchFloor(id: Long) = update { copy(placed = placed.map { if (it.id == id) it.copy(floor = (it.floor + 1) % floors) else it }) }

    // MARK: House upgrades & where the pets are

    val nextRoom get() = rooms.getOrNull(s.floors)

    fun upgradeHouse() {
        val room = nextRoom ?: return
        if (pay(room.price)) {
            update { copy(floors = floors + 1) }
            say("Your house has a new ${room.title}! ${room.emoji} Tap it in Decorate to look inside.")
            Audio.play(Sfx.LevelUp)
        }
    }

    /** Your main pet (the one by your side on the main screen). Defaults to your first pet. */
    val mainPet get() = s.pets.firstOrNull { it.id == s.mainPet } ?: s.pets.firstOrNull()

    fun setMainPet(id: Long) { update { copy(mainPet = id) }; s.pets.firstOrNull { it.id == id }?.let { say("${it.name} is your main pet now! ⭐ ${it.kind.noise}") } }

    /** Other pets wander from room to room: each one picks a new room every 20 minutes. */
    fun petRoom(pet: Pet, now: Long = this.now): Int =
        if (pet.id == mainPet?.id) 0 else ((now / 1_200_000 + pet.id * 7) % s.floors).toInt()

    fun petsIn(floor: Int) = s.pets.filter { it.id != mainPet?.id && petRoom(it) == floor }
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
        Audio.play(Sfx.Ding)
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

    // MARK: Bank

    /** Put money in the bank. Returns how much went in. */
    fun deposit(amount: Int): Int {
        val n = amount.coerceAtMost(s.money)
        if (n <= 0) { say("Your pockets are empty! 💸"); Audio.play(Sfx.Oops); return 0 }
        update { copy(money = money - n, bankSaved = bankSaved + n) }
        Audio.play(Sfx.Coins)
        return n
    }

    /** Take money out of the bank. Returns how much came out. */
    fun withdraw(amount: Int): Int {
        val n = amount.coerceAtMost(s.bankSaved)
        if (n <= 0) { say("There's nothing saved in the bank yet! 🏦"); Audio.play(Sfx.Oops); return 0 }
        update { copy(money = money + n, bankSaved = bankSaved - n) }
        Audio.play(Sfx.Coins)
        return n
    }

    // MARK: Party Place

    val partyOpen get() = level >= PARTY_LEVEL

    /** Four elf friends are at the party; who comes changes every day. */
    val partyGuests get() = npcs.shuffled(kotlin.random.Random(s.day)).take(4)

    fun drink(j: Juice) {
        if (s.energy >= 100) { say("You're already full of energy! ⚡"); return }
        if (pay(j.price)) {
            update { copy(energy = (energy + j.energy).coerceAtMost(100)) }
            say("Slurp! ${j.title} is yummy! +${j.energy} energy ⚡")
        }
    }

    /** Buy a friend a juice: +1 💖, once per friend each day. */
    fun shareJuice(npc: Npc, j: Juice) {
        if (npc.name in s.juiceShared) { say("${npc.name} already has a juice today! 🧃"); return }
        if (pay(j.price)) {
            update { copy(juiceShared = juiceShared + npc.name, hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            say("${npc.name} loves the ${j.title}! Cheers! 💖")
            checkBestFriend(npc)
        }
    }

    /** Pay for your ticket and one for each friend you bring. Friends you bring get +1 💖. */
    fun buyTickets(m: Movie, friends: List<Npc>): Boolean {
        if (!pay(m.price * (1 + friends.size))) return false
        update { copy(moviesSeen = moviesSeen + m.title, hearts = friends.fold(hearts) { h, f -> h.add(f.name, 1) }.mapValues { it.value.coerceAtMost(5) }) }
        sticker("movie")
        say(if (friends.isEmpty()) "Here's your ticket! 🎟" else "Tickets for everyone! Your friends are so happy you invited them 💖")
        friends.forEach { checkBestFriend(it) }
        return true
    }

    /** Buy snacks at the cinema snack bar. */
    fun buySnacks(order: Map<MovieSnack, Int>): Boolean {
        val total = order.entries.sumOf { it.key.price * it.value }
        if (total == 0) return true
        if (!pay(total)) return false
        say("Yum! Enjoy your snacks! 🍿🧃")
        return true
    }

    /** Munch a snack during the movie: a little energy. */
    fun munch(snack: MovieSnack) {
        update { copy(energy = (energy + snack.energy).coerceAtMost(100)) }
        Audio.play(Sfx.Munch)
        say("Munch munch! ${snack.title} +${snack.energy} ⚡")
    }

    /** Say hi to an elf. If you've never met them before, they become your new friend! */
    fun meet(npc: Npc) {
        val isNew = hearts(npc) == 0
        chat(npc)
        if (isNew) { say("You made a new friend: ${npc.name}! 💖 They're in your phone now 📱"); Audio.play(Sfx.Ding) }
    }

    /** Lots of dancing together: everyone on the dance floor gets +1 💖 (once a day). */
    fun danceParty(dancers: List<Npc>) {
        sticker("dance")
        if (s.dancedDay == s.day) { say("What a dance party! 🎉💃"); return }
        update { copy(dancedDay = day, hearts = dancers.fold(hearts) { h, f -> h.add(f.name, 1) }.mapValues { it.value.coerceAtMost(5) }) }
        say("DANCE PARTY! 🎉 Everyone loved your moves! +1 💖 for all your dance friends")
        Audio.play(Sfx.LevelUp)
        dancers.forEach { checkBestFriend(it) }
    }

    fun balloonsDone(popped: Int) {
        if (popped >= 25) sticker("balloons")
        if (popped > s.balloonBest) { update { copy(balloonBest = popped) }; say("New best: $popped balloons! 🎈🏆") }
        else say("You popped $popped balloons! 🎈")
    }

    fun go(p: Place?) { place = p; fromTown = false }

    /** You walked into a building from the Town map, so Back goes back to the town. */
    private var fromTown = false
    fun goFromTown(p: Place) { place = p; fromTown = true }

    /** The Back button: places in town go back to the Town map, everything else to your house. */
    fun back() { val toTown = fromTown || place == Place.House; place = if (toTown && place != Place.Town) Place.Town else null; fromTown = false }

    // MARK: Workmates

    /** What a workmate just said when you finished a job together (shown at that workplace). */
    var workmateCheer by mutableStateOf<Pair<String, String>?>(null)

    /** Finishing a job together: your workmate cheers, and you become better friends (+1 💖 once a day). */
    private fun workTogether(kind: JobKind) {
        val w = workmateAt(placeFor(kind)) ?: return
        workmateCheer = w.name to w.cheer
        if (w.name in s.workedWith) return
        update { copy(workedWith = workedWith + w.name, hearts = hearts.add(w.name, 1).mapValues { it.value.coerceAtMost(5) }) }
        npcs.firstOrNull { it.name == w.name }?.let { checkBestFriend(it) }
    }

    /** Once a day, each workmate gives you a little help. */
    fun workmateHelp(w: Workmate): Boolean {
        if (w.name in s.helpedBy) { say("${w.name} already helped today. Ask again tomorrow! 💖"); return false }
        update {
            val base = copy(helpedBy = helpedBy + w.name)
            when (w.place) {
                Place.Greenhouse -> base.copy(crops = crops.add(Crop.Wheat, 2).add(Crop.Carrot, 2))
                Place.Bakery -> base.copy(cookies = cookies.add(Cookie.entries.filter { it.unlockLevel <= levelFor(workdays) }.random(), 1))
                Place.Stable -> base.copy(crops = crops.add(Crop.Carrot, 3))
                Place.Workshop -> base.copy(toys = toys.add(Toy.Teddy, 1))
                else -> base.copy(energy = (energy + 15).coerceAtMost(100))
            }
        }
        say(w.help)
        Audio.play(Sfx.Ding)
        return true
    }

    // MARK: Favours, mail, birthdays

    /** Each new day: two friends ask for favours, and Maple brings the mail. */
    private fun newDay() {
        if (s.dailyDay == s.day || !s.hasElf) return
        val r = kotlin.random.Random(s.day * 7919)
        val askers = npcs.shuffled(r).take(2)
        val favours = askers.map { n -> Want.entries.random(r).let { w -> Favour(n.name, w, if (w == Want.Toy || w == Want.Cocoa) 1 else 2) } }
        val writers = (npcs.filter { s.hearts.n(it.name) > 0 }.ifEmpty { npcs }).shuffled(r).take(if (s.day == 1) 1 else 2)
        val mail = writers.map { Letter(it.name, letterTemplates.random(r).replace("%me", s.name), s.day) } +
            npcs.filter { isBirthday(it, s.day) }.map { Letter("Maple", "Psst, ${s.name}! Today is ${it.name}'s BIRTHDAY! 🎂 Go and say happy birthday!", s.day) }
        val birthdayMail = if (isMyBirthday) listOf(Letter("Maple", "🎂 HAPPY BIRTHDAY, ${s.name}!!! 🎂 Everyone in Christmas Town is so excited! Tap the birthday button at home to throw your party! 🎉", s.day)) else emptyList()
        update { copy(dailyDay = day, favours = favours, letters = (birthdayMail + mail + letters).take(20)) }
    }

    val unreadLetters get() = s.letters.count { !it.read }
    fun favourFrom(name: String) = s.favours.firstOrNull { it.from == name && !it.done }
    fun birthdayToday(npc: Npc) = isBirthday(npc, s.day)

    /** How many of a wanted thing you have. */
    fun have(w: Want) = when (w) {
        Want.Cookie -> s.cookies.values.sum()
        Want.Toy -> s.toys.values.sum()
        Want.Carrot -> s.crops.n(Crop.Carrot)
        Want.Wheat -> s.crops.n(Crop.Wheat)
        Want.Cocoa -> s.food.n(Food.HotCocoa)
    }

    private fun <K> Map<K, Int>.takeAny(n: Int): Map<K, Int> {
        var left = n
        return mapValues { (_, v) -> val t = minOf(v, left); left -= t; v - t }
    }

    /** Give your friend what they asked for: $300 and +1 💖. */
    fun doFavour(f: Favour): Boolean {
        if (have(f.want) < f.amount) { say("You need ${f.amount} ${f.want.many} for ${f.from}! You have ${have(f.want)}."); Audio.play(Sfx.Oops); return false }
        update {
            val paid = when (f.want) {
                Want.Cookie -> copy(cookies = cookies.takeAny(f.amount))
                Want.Toy -> copy(toys = toys.takeAny(f.amount))
                Want.Carrot -> copy(crops = crops.add(Crop.Carrot, -f.amount))
                Want.Wheat -> copy(crops = crops.add(Crop.Wheat, -f.amount))
                Want.Cocoa -> copy(food = food.add(Food.HotCocoa, -f.amount))
            }
            paid.copy(
                favours = favours.map { if (it == f) it.copy(done = true) else it }, money = money + FAVOUR_REWARD,
                hearts = hearts.add(f.from, 1).mapValues { it.value.coerceAtMost(5) },
            )
        }
        sticker("favour")
        say("${f.from} says THANK YOU! They gave you ${money(FAVOUR_REWARD)} 💰💖")
        Audio.play(Sfx.Coins)
        npcs.firstOrNull { it.name == f.from }?.let { checkBestFriend(it) }
        return true
    }

    fun readLetters() { if (unreadLetters > 0) update { copy(letters = letters.map { it.copy(read = true) }) } }

    /** Write back to a letter: +1 💖 to whoever sent it. */
    fun reply(letter: Letter) {
        update {
            copy(
                letters = letters.map { if (it == letter) it.copy(replied = true, read = true) else it },
                hearts = if (npcs.any { it.name == letter.from }) hearts.add(letter.from, 1).mapValues { it.value.coerceAtMost(5) } else hearts,
            )
        }
        sticker("letter")
        say("Maple will deliver your letter to ${letter.from}! ✉️💖")
        Audio.play(Sfx.Message)
    }

    /** Wish a friend happy birthday (+1 💖), or give them a toy as a present (+2 💖). */
    fun happyBirthday(npc: Npc, withPresent: Boolean): String {
        if (withPresent) {
            if (s.toys.values.sum() == 0) { say("You need a toy to give! Make one in the Workshop 🧸"); Audio.play(Sfx.Oops); return "Aww, that's okay! Your hug is the best present! 🤗" }
            update { copy(toys = toys.takeAny(1), hearts = hearts.add(npc.name, 2).mapValues { it.value.coerceAtMost(5) }, birthdayWished = birthdayWished + npc.name) }
            Audio.play(Sfx.LevelUp)
            sticker("birthday")
            checkBestFriend(npc)
            return "A PRESENT?! For ME?! 🎁 This is the best birthday EVER! Thank you! 🥳"
        }
        if (npc.name !in s.birthdayWished) {
            update { copy(birthdayWished = birthdayWished + npc.name, hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            sticker("birthday")
            checkBestFriend(npc)
        }
        Audio.play(Sfx.Ding)
        return "Thank you!! 🎂 You remembered my birthday! 🥳"
    }

    // MARK: Snow Park games

    fun skateDone(twirls: Int) {
        if (twirls >= 6) sticker("skate")
        val coach = npcs.first { it.name == "Snowflake" }
        if (s.skateDay != s.day && twirls >= 4) {
            update { copy(skateDay = day, hearts = hearts.add(coach.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            checkBestFriend(coach)
            say("Snowflake loved skating with you! +1 💖")
        } else say("$twirls twirls! ⛸️" + if (twirls < 4) " Try for 4 to impress Snowflake!" else "")
    }

    fun sledDone(won: Boolean) {
        if (won) { sticker("sled"); Audio.play(Sfx.LevelUp) } else Audio.play(Sfx.Pop)
        val pip = npcs.first { it.name == "Pip" }
        if (s.sledDay != s.day) {
            update { copy(sledDay = day, hearts = hearts.add(pip.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            checkBestFriend(pip)
        }
        say(if (won) "You WON the sled race! 🏆🛷" else "Pip won this time! Race again? 🛷")
    }

    // MARK: Your birthday

    private fun today() = java.util.Calendar.getInstance()
    private val thisYear get() = today().get(java.util.Calendar.YEAR)

    /** Is it your birthday today (by the phone's calendar)? */
    val isMyBirthday get() = s.myBirthMonth > 0 && today().let { it.get(java.util.Calendar.MONTH) + 1 == s.myBirthMonth && it.get(java.util.Calendar.DAY_OF_MONTH) == s.myBirthDay }
    val birthdayPartyReady get() = isMyBirthday && s.birthdayPartyYear != thisYear

    fun setMyBirthday(month: Int, day: Int) {
        val m = ((month - 1).mod(12)) + 1
        update { copy(myBirthMonth = m, myBirthDay = ((day - 1).mod(daysInMonth[m - 1])) + 1) }
    }

    /** At the end of the party your friends give you presents (furniture and decorations for your house). */
    fun birthdayPresents(guests: List<Npc>): List<Pair<Npc, Item>> {
        val r = kotlin.random.Random(thisYear + s.day)
        val gifts = guests.map { it to Item.entries.filter { i -> i.price in 100..600 }.random(r) }
        update { copy(owned = gifts.fold(owned) { o, (_, item) -> o.add(item, 1) }, birthdayPartyYear = thisYear) }
        sticker("mybday")
        Audio.play(Sfx.LevelUp)
        return gifts
    }

    /** The friends who come dancing with you at the Party Place after your birthday dinner. */
    var partyWith by mutableStateOf<List<String>>(emptyList())
    /** On your birthday the Party Place is open for you, whatever your level. */
    var birthdayDance by mutableStateOf(false)

    // MARK: Elf's Restaurant

    /** Pay for a meal (free on your birthday!). */
    fun orderMeal(total: Int, free: Boolean): Boolean {
        if (free) { say("Happy birthday! Your meal is FREE today! 🎂"); Audio.play(Sfx.Ding); return true }
        if (!pay(total)) return false
        say("Your food is coming! 🍝")
        return true
    }

    /** Eating together: energy for you, and +1 💖 for each friend at the table (once a day). */
    fun finishMeal(friends: List<Npc>, yours: Dish?) {
        sticker("restaurant")
        update { copy(energy = (energy + (yours?.energy ?: 0)).coerceAtMost(100)) }
        if (s.ateOutDay != s.day && friends.isNotEmpty()) {
            update { copy(ateOutDay = day, hearts = friends.fold(hearts) { h, f -> h.add(f.name, 1) }.mapValues { it.value.coerceAtMost(5) }) }
            friends.forEach { checkBestFriend(it) }
            say("What a yummy meal with friends! +1 💖 each")
        } else say("Yum! That was delicious! 😋")
        Audio.play(Sfx.Ding)
    }

    // MARK: Photo booth

    fun takePhoto(p: Photo) {
        update { copy(photos = (listOf(p) + photos).take(MAX_PHOTOS)) }
        sticker("photo")
        Audio.play(Sfx.Pop)
        say("Click! 📸 Your photo is on your living room wall!")
    }

    // MARK: Talking & treats

    /** The friend you're having a chat with (a pop-up over any screen), or null. */
    var chatWith by mutableStateOf<String?>(null)

    /** What each friend is holding right now (a treat you gave them). */
    val holding = androidx.compose.runtime.mutableStateMapOf<String, Treat>()

    /** Serve or share a treat. Cookies come from your cookie jar, cocoa from your snacks; tea is always free. */
    fun serve(npc: Npc, t: Treat): String? {
        when (t) {
            Treat.Cookie -> {
                if (s.cookies.values.sum() == 0) { say("No cookies! Bake some in the Bakery 🍪"); Audio.play(Sfx.Oops); return null }
                giveCookie(npc)
            }
            Treat.Cocoa -> {
                if (s.food.n(Food.HotCocoa) <= 0) { say("No hot cocoa! Buy some at Tinsel's Shop ☕"); Audio.play(Sfx.Oops); return null }
                update { copy(food = food.add(Food.HotCocoa, -1)) }
            }
            Treat.Tea -> {}
        }
        holding[npc.name] = t
        Audio.play(Sfx.Munch)
        if (t != Treat.Cookie && npc.name !in s.servedToday) {
            update { copy(servedToday = servedToday + npc.name, hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            checkBestFriend(npc)
        }
        chat(npc)
        return treatThanks.random()
    }

    // MARK: Town & friends' houses

    /** Whose house you're visiting right now (not saved). */
    var visiting by mutableStateOf<String?>(null)

    fun visit(npc: Npc) { visiting = npc.name; place = Place.House; fromTown = true }

    /** Knock knock! The first visit each day, your friend makes you hot cocoa. */
    fun arriveAt(npc: Npc): String {
        chat(npc)
        if (npc.name in s.visitedToday) return "Back again? Yay! Make yourself at home 🏠"
        update { copy(visitedToday = visitedToday + npc.name, energy = (energy + 10).coerceAtMost(100)) }
        Audio.play(Sfx.Ding)
        return "Welcome to my house, ${s.name}! Have some hot cocoa ☕ (+10 ⚡)"
    }

    /** Your friend cooks for you! Yummy food and energy, once a day at each friend's house. */
    fun eatAt(npc: Npc): String {
        if (npc.name in s.ateWith) return "We already ate today! I'm SO full 😄 Come back tomorrow!"
        val dish = listOf("warm cookies 🍪", "a big mug of hot cocoa ☕", "apple pie 🥧", "gingerbread 🍪", "pancakes with syrup 🥞", "vegetable soup 🥕").random()
        update { copy(ateWith = ateWith + npc.name, energy = (energy + 15).coerceAtMost(100)) }
        Audio.play(Sfx.Munch)
        say("Yum! +15 ⚡")
        return "I made $dish for us! Let's eat! 😋"
    }

    /** You found your friend in hide & seek! +1 💖 the first game each day. */
    fun foundInHideAndSeek(npc: Npc, tries: Int): String {
        Audio.play(Sfx.Ding)
        if (npc.name !in s.playedWith) {
            update { copy(playedWith = playedWith + npc.name, hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }) }
            checkBestFriend(npc)
        }
        return if (tries == 1) "WHAT?! You found me first try! 😲 You're SO good at this!" else "Boo! 😆 You found me in $tries tries! Play again?"
    }

    /** Invite a friend to come over to your house for the rest of the day. */
    fun invite(npc: Npc) {
        when {
            npc.name in s.homeGuests -> say("${npc.name} is already at your house! 🏠")
            s.homeGuests.size >= MAX_HOME_GUESTS -> say("Your house is full! $MAX_HOME_GUESTS friends at a time 🏠")
            else -> {
                update { copy(homeGuests = homeGuests + npc.name, hearts = hearts.add(npc.name, 1).mapValues { it.value.coerceAtMost(5) }) }
                say("${npc.name} is coming over to your house! 🏠💖")
                Audio.play(Sfx.Ding)
                checkBestFriend(npc)
            }
        }
    }

    /** Say goodbye to a friend at your house. */
    fun sendHome(name: String) {
        update { copy(homeGuests = homeGuests - name) }
        say("Bye bye, $name! 👋")
    }

    fun toggleMusic() { update { copy(musicOn = !musicOn) }; Audio.musicOn = s.musicOn }
    fun toggleSound() { update { copy(soundOn = !soundOn) }; Audio.soundOn = s.soundOn; Audio.play(Sfx.Tap) }

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
        if (msgs.any { it.from != ME }) Audio.play(Sfx.Message)
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

    init { newDay() }
}
