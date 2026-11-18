package com.lexi.christmastown

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

fun hex(v: Long): Color = Color(0xFF000000L or v)

object Pal {
    val ink = hex(0x2B1D2E)
    val red = hex(0xE5394B)
    val darkRed = hex(0xA02338)
    val green = hex(0x3FAF5A)
    val darkGreen = hex(0x2E7D4A)
    val gold = hex(0xFFCC4D)
    val snow = hex(0xF4F8FF)
    val night = hex(0x1D2B53)
    val blue = hex(0x3D6FD6)
    val purple = hex(0x9B6BE0)
    val pink = hex(0xFF8FB0)
    val brown = hex(0x9A5B34)
    val cream = hex(0xFFF6E6)
}

// MARK: - Your elf

@Serializable enum class HairStyle(val title: String, val price: Int, val sprite: String) {
    Short("Short", 0, "hair_short"), Long("Long", 0, "hair_long"),
    Pigtails("Pigtails", 300, "hair_pigtails"), Bun("Bun", 300, "hair_bun"), Bob("Bob", 300, "hair_bob"),
}

@Serializable enum class Outfit(val title: String, val price: Int, val sprite: String?) {
    Tunic("Elf Tunic", 0, null), Sweater("Christmas Sweater", 500, "outfit_sweater"),
    Dress("Party Dress", 400, "outfit_dress"), Overalls("Overalls", 450, "outfit_overalls"),
    Pajamas("Polka Pajamas", 350, "outfit_pajamas"), Snowsuit("Snowsuit", 550, "outfit_snowsuit"),
}

@Serializable enum class Hat(val title: String, val price: Int, val sprite: String?) {
    None("No Hat", 0, null), Elf("Elf Hat", 0, "hat_elf"), Beanie("Beanie", 300, "hat_beanie"),
    Santa("Santa Hat", 600, "hat_santa"), Antlers("Antlers", 500, "hat_antlers"), Crown("Crown", 1500, "hat_crown"),
    Earmuffs("Earmuffs", 350, "hat_earmuffs"), TopHat("Top Hat", 700, "hat_tophat"),
}

@Serializable enum class Accessory(val title: String, val price: Int, val sprite: String?) {
    None("Nothing", 0, null), Scarf("Scarf", 250, "acc_scarf"), Bell("Jingle Bell", 200, "acc_bell"),
}

object ElfColors {
    val skins = listOf(hex(0xFFE0C8), hex(0xF5C29A), hex(0xD69A6E), hex(0x9C6648))
    val hairs = listOf(hex(0x6B4130), hex(0x2E2A35), hex(0xF2C65B), hex(0xE0743C), hex(0xFF8FB8), hex(0xB79CFF), hex(0xF4F4F4))
    val eyes = listOf(hex(0x6B4A3A), hex(0x3D6FD6), hex(0x2E9E5B), hex(0x7E57C2))
    val clothes = listOf(hex(0x3FAF5A), hex(0xE5394B), hex(0x3D6FD6), hex(0x9B6BE0), hex(0xFF8FB8), hex(0xF2B632), hex(0x2EB5A5), hex(0xF0F0F0))
}

@Serializable data class ElfLook(
    val girl: Boolean = true,
    val skin: Int = 0,
    val hairStyle: HairStyle = HairStyle.Long,
    val hairColor: Int = 0,
    val eye: Int = 0,
    val outfit: Outfit = Outfit.Tunic,
    val outfitColor: Int = 0,
    val hat: Hat = Hat.Elf,
    val hatColor: Int = 1,
    val accessory: Accessory = Accessory.None,
)

// MARK: - Greenhouse, bakery, workshop, food

@Serializable enum class Crop(val title: String, val plural: String, val seedPrice: Int, val growSeconds: Int, val unlockLevel: Int, val sprite: String) {
    Wheat("Wheat", "wheat", 20, 10, 1, "wheat"),
    Carrot("Carrot", "carrots", 20, 10, 1, "carrot"),
    Berry("Berry", "berries", 40, 14, 2, "berry"),
    Cocoa("Cocoa", "cocoa pods", 60, 18, 3, "cocoa"),
    Ginger("Ginger", "ginger roots", 70, 20, 6, "ginger"),
    Peppermint("Peppermint", "peppermint leaves", 80, 22, 7, "peppermint"),
}

const val HARVEST = 3

@Serializable enum class Cookie(val title: String, val recipe: Map<Crop, Int>, val unlockLevel: Int, val energy: Int, val sprite: String) {
    Sugar("Sugar Cookie", mapOf(Crop.Wheat to 2), 1, 15, "cookie_sugar"),
    BerryJam("Berry Cookie", mapOf(Crop.Wheat to 2, Crop.Berry to 1), 2, 20, "cookie_berry"),
    Choco("Choc Chip Cookie", mapOf(Crop.Wheat to 2, Crop.Cocoa to 1), 3, 25, "cookie_choco"),
    Gingerbread("Gingerbread Person", mapOf(Crop.Wheat to 2, Crop.Ginger to 1), 6, 30, "cookie_ginger"),
    CandyCane("Candy Cane Cookie", mapOf(Crop.Wheat to 2, Crop.Peppermint to 1), 7, 30, "cookie_candycane"),
    Reindeer("Reindeer Cookie", mapOf(Crop.Wheat to 2, Crop.Cocoa to 1, Crop.Berry to 1), 8, 40, "cookie_reindeer"),
}

@Serializable enum class Toy(val title: String, val unlockLevel: Int, val sprite: String, val steps: List<String>) {
    Teddy("Teddy Bear", 1, "teddy", listOf("Stuff it with cotton", "Sew it up", "Pick a fur color")),
    CandyCane("Candy Cane", 1, "candycane", listOf("Pull the sugar", "Pick a stripe color", "Bend the hook")),
    Train("Toy Train", 2, "train", listOf("Build the engine", "Screw on the wheels", "Paint it")),
    Doll("Doll", 3, "doll", listOf("Sew the face", "Brush the hair", "Pick a dress")),
    Drum("Drum", 4, "drum", listOf("Stretch the top", "Paint the drum", "Add the sticks")),
    Robot("Robot", 5, "robot", listOf("Bolt the body", "Charge the battery", "Pick a paint")),
    RockingHorse("Rocking Horse", 6, "rockinghorse", listOf("Carve the rockers", "Brush the mane", "Paint the saddle")),
    YoYo("Yo-Yo", 7, "yoyo", listOf("Wind the string", "Glue the halves", "Pick a color")),
    SnowGlobe("Snow Globe", 8, "snowglobe", listOf("Shake in the snow", "Seal the glass", "Paint the base")),
}

@Serializable enum class Food(val title: String, val price: Int, val energy: Int, val sprite: String) {
    Candy("Candy", 30, 15, "candy"),
    HotCocoa("Hot Cocoa", 50, 40, "cocoa_mug"),
    Pie("Apple Pie", 90, 60, "pie"),
}

// MARK: - House stuff

enum class Spot { Floor, Wall, Rug }
enum class ItemKind { Furniture, Decoration }

@Serializable enum class Item(val title: String, val price: Int, val sprite: String, val spot: Spot, val kind: ItemKind) {
    Tree("Christmas Tree", 800, "tree", Spot.Floor, ItemKind.Decoration),
    Fireplace("Fireplace", 900, "fireplace", Spot.Floor, ItemKind.Furniture),
    Couch("Couch", 600, "couch", Spot.Floor, ItemKind.Furniture),
    Armchair("Armchair", 400, "armchair", Spot.Floor, ItemKind.Furniture),
    Bed("Bed", 700, "bed", Spot.Floor, ItemKind.Furniture),
    Table("Table", 300, "table", Spot.Floor, ItemKind.Furniture),
    Lamp("Lamp", 200, "lamp", Spot.Floor, ItemKind.Furniture),
    Bookshelf("Bookshelf", 500, "bookshelf", Spot.Floor, ItemKind.Furniture),
    Rug("Rug", 250, "rug", Spot.Rug, ItemKind.Furniture),
    Clock("Clock", 200, "clock", Spot.Wall, ItemKind.Furniture),
    Picture("Picture", 150, "picture", Spot.Wall, ItemKind.Furniture),
    Stocking("Stocking", 100, "stocking", Spot.Wall, ItemKind.Decoration),
    Wreath("Wreath", 150, "wreath", Spot.Wall, ItemKind.Decoration),
    Garland("Twinkle Lights", 200, "garland", Spot.Wall, ItemKind.Decoration),
    Snowman("Snowman", 350, "snowman", Spot.Floor, ItemKind.Decoration),
    Presents("Presents", 300, "presents", Spot.Floor, ItemKind.Decoration),
    Poinsettia("Poinsettia", 150, "poinsettia", Spot.Floor, ItemKind.Decoration),
    GingerbreadHouse("Gingerbread House", 250, "ginghouse", Spot.Floor, ItemKind.Decoration),
    Candles("Candles", 120, "candles", Spot.Floor, ItemKind.Decoration),
    RockingChair("Rocking Chair", 450, "rockchair", Spot.Floor, ItemKind.Furniture),
    ToyChest("Toy Chest", 500, "toychest", Spot.Floor, ItemKind.Furniture),
    Nutcracker("Nutcracker", 300, "nutcracker", Spot.Floor, ItemKind.Decoration),
    Sled("Sled", 350, "sled", Spot.Floor, ItemKind.Decoration),
}

/** One thing placed in the house. x/y are fractions of the room (0..1). Floor 0 = downstairs. */
@Serializable data class Placed(val id: Long, val item: Item, val floor: Int, val x: Float, val y: Float = 0.3f)

val startingHouse = listOf(
    Placed(1, Item.Rug, 0, 0.45f), Placed(2, Item.Fireplace, 0, 0.42f), Placed(3, Item.Tree, 0, 0.12f),
    Placed(4, Item.Couch, 0, 0.75f), Placed(5, Item.Wreath, 0, 0.42f, 0.08f), Placed(6, Item.Stocking, 0, 0.3f, 0.45f),
    Placed(7, Item.Lamp, 0, 0.94f),
    Placed(8, Item.Bed, 1, 0.22f), Placed(9, Item.Table, 1, 0.62f), Placed(10, Item.Clock, 1, 0.6f, 0.15f),
    Placed(11, Item.Picture, 1, 0.22f, 0.12f), Placed(12, Item.Bookshelf, 1, 0.88f),
)

// MARK: - The house: two storeys to start, more floors as upgrades

/** One floor of the house, bottom (0) to top. [price] is the upgrade cost (0 = you start with it). */
class Room(val title: String, val emoji: String, val price: Int, val wall: Long, val stripe: Long)

val rooms = listOf(
    Room("Living Room", "🛋️", 0, 0xE3F0D8, 0xD3E6C4),
    Room("Bedroom", "🛏️", 0, 0xF6D7E3, 0xEFC3D3),
    Room("Attic Playroom", "🧸", 3000, 0xFFF1C9, 0xF6E2A6),
    Room("Starlight Loft", "🔭", 6000, 0xD9D4F7, 0xC6BFF0),
)
const val START_FLOORS = 2

/** Little things your pets get up to, by room. "%s" is the pet's name. */
val petHappenings = listOf(
    "%s is napping on the rug 💤", "%s is chasing a dust bunny 🐾", "%s found a sock! 🧦", "%s is staring out the window ❄️",
    "%s is doing zoomies! 💨", "%s is playing with a ribbon 🎀", "%s is hiding under the bed 👀", "%s is guarding the presents 🎁",
)
val roomHappenings = listOf(
    "The fire is crackling 🔥", "Someone left cookie crumbs on the pillow 🍪", "The toy train is going round and round 🚂", "The stars are twinkling through the telescope ✨",
)

// MARK: - Reindeer

val reindeerNames = listOf("Dasher", "Dancer", "Prancer", "Vixen", "Comet", "Cupid", "Donner", "Blitzen", "Rudolph")
/** Rudolph (the last name) only joins at the top level. */
fun reindeerCount(level: Int) = listOf(2, 3, 4, 5, 6, 7, 8, 9)[(level - 1).coerceIn(0, MAX_LEVEL - 1)]

/** Things you can put on a reindeer in its care screen. They show everywhere the reindeer does. */
@Serializable enum class Deco(val title: String, val emoji: String, val price: Int, val sprite: String) {
    Bells("Jingle Bells", "🔔", 150, "rd_bells"),
    Scarf("Cozy Scarf", "🧣", 200, "rd_scarf"),
    Lights("Antler Lights", "💡", 250, "rd_lights"),
    Blanket("Saddle Blanket", "🟥", 300, "rd_blanket"),
}

/** Reindeer you've fully cared for and harnessed pull the sleigh better. */
const val TEAM_SIZE = 4

// MARK: - Levels & jobs

val levelTitles = listOf("Helper Elf", "Cookie Elf", "Toy Elf", "Sleigh Elf", "Workshop Star", "Gift Wrapper", "Tree Trimmer", "Santa's Best Elf")
const val MAX_LEVEL = 8
const val PAY = 1000
const val CHRISTMAS_EVE_WORKDAYS = 10

/** Workdays needed to reach each level. */
fun levelFor(workdays: Int) = when {
    workdays >= 8 -> 8      // Rudolph joins two days before Christmas Eve (10 workdays)
    workdays >= 7 -> 7
    workdays >= 6 -> 6
    workdays >= 5 -> 5
    workdays >= 3 -> 4
    workdays >= 2 -> 3
    workdays >= 1 -> 2
    else -> 1
}

@Serializable enum class JobKind(val emoji: String) { Bake("🍪"), Toys("🧸"), Reindeer("🦌"), SortList("📜"), Deliver("🛷"), Wrap("🎁"), Tree("🎄") }

@Serializable data class Job(val kind: JobKind, val goal: Int, val done: Int = 0) {
    val finished get() = done >= goal
    val text get() = when (kind) {
        JobKind.Bake -> "Bake $goal cookies"
        JobKind.Toys -> "Make $goal toys"
        JobKind.Reindeer -> "Feed & brush every reindeer"
        JobKind.SortList -> "Sort $goal kids on Santa's list"
        JobKind.Deliver -> "Deliver $goal presents"
        JobKind.Wrap -> "Wrap $goal presents"
        JobKind.Tree -> "Decorate the Town Tree"
    }
}

fun jobsFor(level: Int): List<Job> = buildList {
    val i = level.coerceIn(1, MAX_LEVEL) - 1
    add(Job(JobKind.Bake, listOf(2, 3, 3, 4, 4, 4, 5, 5)[i]))
    add(Job(JobKind.Toys, listOf(2, 3, 4, 4, 5, 5, 5, 6)[i]))
    add(Job(JobKind.Reindeer, 1))
    if (level >= 2) add(Job(JobKind.SortList, listOf(0, 5, 5, 5, 5, 5, 6, 6)[i]))
    if (level >= 3) add(Job(JobKind.Deliver, listOf(0, 0, 4, 4, 5, 6, 6, 7)[i]))
    if (level >= 6) add(Job(JobKind.Wrap, listOf(0, 0, 0, 0, 0, 2, 2, 3)[i]))
    if (level >= 7) add(Job(JobKind.Tree, 1))
}

/** How tricky the sleigh flight is: faster, more clouds, naughty houses, and a dark foggy night at the top. */
data class Flight(val speed: Float, val cloudEvery: Float, val naughtyChance: Float, val foggy: Boolean)

fun flightFor(level: Int, christmasEve: Boolean): Flight = when {
    christmasEve -> Flight(95f, 260f, 0.2f, true)
    else -> Flight(
        speed = 70f + 5f * (level - 3).coerceAtLeast(0),
        cloudEvery = listOf(520f, 520f, 520f, 460f, 400f, 340f, 300f, 260f)[(level - 1).coerceIn(0, 7)],
        naughtyChance = if (level >= 5) 0.25f else 0.15f,
        foggy = level >= 8,
    )
}

// MARK: - Santa's list

@Serializable data class Kid(val name: String, val deed: String, val nice: Boolean)

private val kidNames = listOf("Mia", "Leo", "Ava", "Noah", "Zoe", "Max", "Lily", "Sam", "Ella", "Ben", "Ruby", "Jack", "Chloe", "Oscar", "Ivy", "Theo", "Grace", "Finn", "Lucy", "Kai")
private val niceDeeds = listOf(
    "Shared toys with their little brother", "Helped grandma carry the groceries", "Fed the neighbor's cat",
    "Cleaned up their room without being asked", "Said thank you to the bus driver", "Made a card for a sick friend",
    "Helped plant flowers at school", "Gave half their cookie to a friend", "Walked the dog every single day",
    "Helped a new kid make friends",
)
private val naughtyDeeds = listOf(
    "Put a frog in the teacher's desk", "Ate all the cookies and blamed the dog", "Drew on the walls with crayons",
    "Pulled their sister's pigtails", "Never brushed their teeth", "Hid dad's car keys in the fish tank",
    "Threw snowballs at the mail carrier", "Said NO to bedtime 47 times", "Put salt in the sugar bowl",
    "Fed their broccoli to the dog under the table",
)

fun makeKids(n: Int): List<Kid> {
    val names = kidNames.shuffled()
    return List(n) { i ->
        val nice = Math.random() < 0.55
        Kid(names[i], if (nice) niceDeeds.random() else naughtyDeeds.random(), nice)
    }
}

// MARK: - Elf friends in town

class Npc(val name: String, val look: ElfLook, val job: String, val lines: List<String>, val bestFriendGift: Item)

val npcs = listOf(
    Npc(
        "Jingle", ElfLook(false, 1, HairStyle.Short, 3, 2, Outfit.Overalls, 0, Hat.Beanie, 1), "Toy Maker",
        listOf(
            "Santa pays \$1,000 every day you finish ALL your jobs!",
            "Making toys and baking use energy. Eat something if you're tired!",
            "Tap the 📋 jobs button at the top to see what Santa needs today.",
            "New toys unlock when you level up. I can't wait to make robots!",
            "If you skip work, you don't get paid. Santa's strict about that!",
        ),
        Item.Garland,
    ),
    Npc(
        "Twinkle", ElfLook(true, 0, HairStyle.Pigtails, 4, 3, Outfit.Dress, 4, Hat.Elf, 3), "Party Planner",
        listOf(
            "I LOVE parties! Be my friend and I'll invite you to one!",
            "You can drag your furniture around. Tap Decorate at home!",
            "The Shop has the cutest decorations. Twinkle lights are my favorite.",
            "Your house can have two floors of Christmas cheer!",
        ),
        Item.Presents,
    ),
    Npc(
        "Pepper", ElfLook(true, 2, HairStyle.Bun, 1, 0, Outfit.Sweater, 1, Hat.None, 0, Accessory.Scarf), "Head Baker",
        listOf(
            "Cookies need wheat. Grow it in the Greenhouse!",
            "Berry cookies unlock at Level 2, and choc chip at Level 3. Yum!",
            "Elves LOVE cookies. Try giving one to a friend.",
            "Hot cocoa from the Shop gives you lots of energy.",
        ),
        Item.GingerbreadHouse,
    ),
    Npc(
        "Sprout", ElfLook(false, 3, HairStyle.Bob, 2, 1, Outfit.Tunic, 6, Hat.Antlers, 0, Accessory.Bell), "Reindeer Keeper",
        listOf(
            "Reindeer love carrots! Grow them in the Greenhouse.",
            "Feed AND brush every reindeer to finish the reindeer job.",
            "Rudolph joins the stable at Level 8. His nose glows!",
            "Seeds need water before they grow. 💧",
        ),
        Item.Poinsettia,
    ),
    Npc(
        "Holly", ElfLook(true, 1, HairStyle.Bob, 3, 2, Outfit.Overalls, 2, Hat.TopHat, 0), "Inventor",
        listOf(
            "EUREKA! I invented a cookie that eats itself. Wait, that's bad.",
            "Wrapped presents go on the sleigh first. I measured it! 📏",
            "Happy reindeer steer better. That's SCIENCE. 🔬",
            "My next invention: a snowball that comes back like a yo-yo!",
            "Pets get hungry over time, so check on them every day! 🐾",
        ),
        Item.ToyChest,
    ),
    Npc(
        "Frost", ElfLook(false, 0, HairStyle.Short, 6, 1, Outfit.Snowsuit, 6, Hat.Earmuffs, 2), "Snow Sculptor",
        listOf(
            "What do snowmen eat for breakfast? Frosted flakes! ❄️😂",
            "I built a snowman of Santa. He said it looked 'a bit chilly'.",
            "Snowball fight in the Snow Park? I'm SO in. Bring it! ⛄",
            "Why did the reindeer cross the road? To get to the other SLEIGH! 🦌",
            "The Town Tree needs ornaments, tinsel AND a star. Don't forget the star!",
        ),
        Item.Sled,
    ),
)

/** Twinkle's party: pick a theme, get a decoration. */
val partyThemes = listOf("Candy Land 🍭" to Item.GingerbreadHouse, "Winter Wonderland ⛄" to Item.Snowman, "Cozy Cabin 🕯️" to Item.Candles)

// MARK: - Pets (bought with in-game money only)

@Serializable enum class PetKind(val title: String, val price: Int, val sprite: String, val noise: String, val food: String, val jokes: List<String>) {
    Penguin("Penguin", 600, "pet_penguin", "Squawk!", "🐟", listOf("*waddles in a circle*", "*slides on its tummy* Wheee!", "*bows politely*", "I'm wearing a tuxedo because I'm FANCY.")),
    Puppy("Puppy", 700, "pet_puppy", "Woof!", "🦴", listOf("*chases own tail*", "*steals a sock* 🧦", "Is it walkies time?!", "*rolls over for tummy rubs*")),
    Kitten("Kitten", 700, "pet_kitten", "Mew!", "🐟", listOf("*knocks an ornament off the tree*", "*naps in a box* 📦", "*bats at the tinsel*", "Purrrrr...")),
    Bunny("Bunny", 650, "pet_bunny", "*thump thump*", "🥕", listOf("*binky jump!*", "*wiggles nose*", "*hides in a stocking*", "Carrot? CARROT?!")),
    Owl("Snowy Owl", 900, "pet_owl", "Hoo hoo!", "🐭", listOf("*turns head ALL the way around*", "Hoo is the best elf? YOU!", "*fluffs feathers*", "*winks slowly*")),
    Fox("Arctic Fox", 1000, "pet_fox", "Yip yip!", "🍗", listOf("*pounces into a snow pile*", "*curls up with tail as a blanket*", "*does a happy zoomie*", "Yip! I hid your cookie. Somewhere.")),
}

/** One pet you own. Meters are worked out from when you last fed, washed and played with it. */
@Serializable data class Pet(val id: Long, val kind: PetKind, val name: String, val fedAt: Long = 0, val washedAt: Long = 0, val pettedAt: Long = 0, val giftDay: Int = 0)

/** 0..1 meters that go down slowly over real time (full → empty in [hours]). */
fun meter(at: Long, now: Long, hours: Float) = if (at == 0L) 0.3f else (1f - (now - at) / (hours * 3_600_000f)).coerceIn(0f, 1f)
fun Pet.fullness(now: Long) = meter(fedAt, now, 8f)
fun Pet.cleanness(now: Long) = meter(washedAt, now, 24f)
fun Pet.happiness(now: Long) = (meter(pettedAt, now, 6f) * 0.5f + fullness(now) * 0.25f + cleanness(now) * 0.25f)

val petNames = listOf("Pip", "Biscuit", "Snowball", "Mochi", "Waffles", "Noodle", "Pickle", "Sprinkles", "Muffin", "Ziggy", "Bean", "Tinsel")

// MARK: - Advent calendar (1–24 December, real dates)

/** A surprise behind an advent door. [give] adds it to your save. */
class Surprise(val title: String, val sprite: String, val give: (Save) -> Save)

private fun item(i: Item) = Surprise(i.title, i.sprite) { it.copy(owned = it.owned.add(i, 1)) }
private fun food(f: Food, n: Int = 1) = Surprise(if (n > 1) "$n ${f.title}" else f.title, f.sprite) { it.copy(food = it.food.add(f, n)) }
private fun seeds(c: Crop, n: Int) = Surprise("$n ${c.title} seeds", c.sprite) { it.copy(seeds = it.seeds.add(c, n)) }
private fun hat(h: Hat) = Surprise(h.title, "gift") { it.copy(hats = it.hats + h) }
private fun outfit(o: Outfit) = Surprise(o.title, "gift") { it.copy(outfits = it.outfits + o) }

/** Door 1..24. Day 24 is the best one. */
val adventSurprises: List<Surprise> = listOf(
    food(Food.Candy, 2), item(Item.Stocking), food(Food.HotCocoa), item(Item.Wreath), seeds(Crop.Wheat, 6), item(Item.Candles),
    item(Item.Poinsettia), food(Food.Pie), hat(Hat.Earmuffs), item(Item.Garland), item(Item.Nutcracker), seeds(Crop.Carrot, 6),
    item(Item.Snowman), item(Item.Sled), outfit(Outfit.Pajamas), item(Item.Presents), seeds(Crop.Ginger, 3), item(Item.RockingChair),
    food(Food.HotCocoa, 2), item(Item.ToyChest), hat(Hat.TopHat), item(Item.GingerbreadHouse), outfit(Outfit.Snowsuit), hat(Hat.Crown),
)

/** Where each day's door sits in the 4 × 6 grid (mixed up, like a real calendar). */
val adventOrder = listOf(13, 2, 20, 7, 17, 10, 4, 24, 15, 1, 9, 22, 6, 19, 12, 3, 23, 8, 16, 11, 21, 5, 18, 14)

// MARK: - Sticker book: one sticker for everything you collect or do

class Sticker(val id: String, val title: String, val sprite: String)

val stickers: List<Sticker> = buildList {
    Cookie.entries.forEach { add(Sticker("cookie:${it.name}", "Baked a ${it.title}", it.sprite)) }
    Toy.entries.forEach { add(Sticker("toy:${it.name}", "Made a ${it.title}", it.sprite)) }
    reindeerNames.forEach { add(Sticker("deer:$it", "Pampered $it (fed, brushed & washed)", "reindeer")) }
    PetKind.entries.forEach { add(Sticker("pet:${it.name}", "Adopted a ${it.title}", it.sprite)) }
    Item.entries.forEach { add(Sticker("item:${it.name}", "Owned a ${it.title}", it.sprite)) }
    add(Sticker("wrap", "Wrapped a present", "gift"))
    add(Sticker("ginger", "Built a gingerbread house", "ginghouse"))
    add(Sticker("tree", "Decorated the Town Tree", "tree"))
    add(Sticker("snowball", "Snowball champion (10 hits)", "snowman"))
    add(Sticker("flight", "Finished a sleigh flight", "sleigh"))
    add(Sticker("friend", "Made a best friend", "candy"))
    add(Sticker("level8", "Became Santa's Best Elf", "santa"))
    add(Sticker("christmas", "Saved Christmas!", "presents"))
    add(Sticker("advent", "Opened an advent door", "candycane"))
    add(Sticker("movie", "Watched a movie at the Party Place", "popcorn"))
    add(Sticker("balloons", "Popped 25 balloons in one game", "balloon"))
    add(Sticker("memory", "Matched all the party cards", "juice_punch"))
}

/** Sticker milestones and their prizes. */
val stickerPrizes: List<Pair<Int, Surprise>> = listOf(
    10 to item(Item.Presents), 25 to hat(Hat.TopHat), 40 to item(Item.ToyChest), 60 to hat(Hat.Crown),
)

// MARK: - Activities (the icons on the main screen)

enum class Side { Left, Right, Hidden }   // Hidden places open from the buttons at the bottom of the main screen

/** Each icon on the main screen. [sprite] is its pixel-art picture ("" = drawn specially). */
enum class Place(val title: String, val sprite: String, val side: Side, val color: Color) {
    Greenhouse("Greenhouse", "wheat", Side.Left, hex(0x3FAF5A)),
    Bakery("Bakery", "cookie_sugar", Side.Left, hex(0xE0843A)),
    Stable("Stable", "reindeer", Side.Left, hex(0x9A5B34)),
    Workshop("Workshop", "teddy", Side.Left, hex(0xE5394B)),
    SantaList("Santa's List", "santa", Side.Left, hex(0xA02338)),
    Sleigh("Sleigh", "sleigh", Side.Left, hex(0x3D6FD6)),
    Park("Snow Park", "snowman", Side.Left, hex(0x5BA4D9)),
    Decorate("Decorate", "tree", Side.Right, hex(0x2E7D4A)),
    Shop("Shop", "presents", Side.Right, hex(0x9B6BE0)),
    Closet("Closet", "", Side.Right, hex(0xFF8FB0)),
    Friends("Friends", "", Side.Right, hex(0x2EB5A5)),
    Phone("Phone", "phone", Side.Right, hex(0x3D6FD6)),
    Snacks("Snacks", "cocoa_mug", Side.Right, hex(0xC98B5E)),
    Pets("Pets", "pet_penguin", Side.Right, hex(0xF2A93B)),
    Party("Party Place", "balloon", Side.Right, hex(0xFF5FA2)),
    Advent("Advent Calendar", "gift", Side.Hidden, hex(0xA02338)),
    Stickers("Sticker Book", "teddy", Side.Hidden, hex(0x9B6BE0)),
}

// MARK: - Party Place (opens at Level 7)

const val PARTY_LEVEL = 7

/** Fruit juice at the Party Place juice bar: costs a little elf money, gives energy. */
enum class Juice(val title: String, val price: Int, val energy: Int, val sprite: String) {
    Orange("Orange Juice", 10, 10, "juice_orange"),
    Apple("Apple Juice", 10, 10, "juice_apple"),
    Strawberry("Strawberry Juice", 15, 15, "juice_strawberry"),
    Grape("Grape Juice", 15, 15, "juice_grape"),
    Kiwi("Kiwi Juice", 20, 20, "juice_kiwi"),
    Punch("Fruit Punch", 25, 25, "juice_punch"),
}

/** What the elves say at the party. */
val partyLines = listOf(
    "This is the BEST party ever! 🎉",
    "Have you tried the fruit punch? It's so fizzy!",
    "I popped SO many balloons! 🎈",
    "Let's watch a movie together! I'll bring popcorn 🍿",
    "I love dancing! Look at my moves! 💃",
    "The kiwi juice is green AND yummy!",
    "Santa said we've been working super hard. Party time!",
    "Do you want to play the memory game with me?",
    "My favourite movie is the one with the snowman! ⛄",
    "Cheers! 🧃 *clink*",
)

/** One picture in a party movie: who's on screen, where they go, and what the narrator says. */
class Actor(val sprite: String, val x: Float, val h: Int, val up: Float = 0f, val dx: Float = 0f, val dy: Float = 0f, val flip: Boolean = false)
class Scene(val caption: String, val sky: Long, val ground: Long, val actors: List<Actor>, val snow: Boolean = true)

/** "@me" in an actor means your own elf; "@Twinkle" (etc.) means that elf friend. */
class Movie(val title: String, val price: Int, val poster: String, val scenes: List<Scene>)

private const val DAY_SKY = 0x9BC8EEL
private const val NIGHT_SKY = 0x1D2B53L
private const val SNOW = 0xF4F8FFL
private const val WOOD = 0xC98B5EL

val movies = listOf(
    Movie(
        "The Snowman Who Lost His Hat", 3, "snowman", listOf(
            Scene("Once upon a time, a happy snowman lived in Christmas Town.", DAY_SKY, SNOW, listOf(Actor("snowman", 0.5f, 120), Actor("tree", 0.15f, 110), Actor("tree", 0.85f, 90))),
            Scene("WHOOOSH! A big wind blew his hat away!", DAY_SKY, SNOW, listOf(Actor("snowman", 0.5f, 120), Actor("@Frost", 0.2f, 90, dx = 0.1f))),
            Scene("\"Don't worry!\" said the elves. \"We'll find it!\"", DAY_SKY, SNOW, listOf(Actor("@me", 0.3f, 100, dx = 0.15f), Actor("@Twinkle", 0.6f, 100, dx = 0.15f))),
            Scene("They looked behind the presents...", DAY_SKY, SNOW, listOf(Actor("presents", 0.6f, 70), Actor("@me", 0.25f, 100, dx = 0.1f))),
            Scene("...and under the Christmas tree...", DAY_SKY, SNOW, listOf(Actor("tree", 0.55f, 140), Actor("@Twinkle", 0.2f, 100, dx = 0.1f))),
            Scene("A puppy came running. It had the hat! Woof woof!", DAY_SKY, SNOW, listOf(Actor("pet_puppy", 0.9f, 60, dx = -0.5f), Actor("snowman", 0.25f, 120))),
            Scene("The snowman got his hat back and smiled all day. THE END ⛄", DAY_SKY, SNOW, listOf(Actor("snowman", 0.4f, 120, up = 0f), Actor("pet_puppy", 0.65f, 50), Actor("@me", 0.82f, 90))),
        ),
    ),
    Movie(
        "The Littlest Reindeer", 4, "reindeer", listOf(
            Scene("In Santa's stable lived the littlest reindeer of all.", NIGHT_SKY, WOOD, listOf(Actor("reindeer", 0.3f, 110), Actor("reindeer", 0.75f, 60, flip = true)), snow = false),
            Scene("The big reindeer could fly. The little one could only hop. Boing!", DAY_SKY, SNOW, listOf(Actor("reindeer", 0.5f, 60, dy = 30f))),
            Scene("So the elves helped! They baked special reindeer cookies.", DAY_SKY, WOOD, listOf(Actor("@me", 0.3f, 100), Actor("cookie_reindeer", 0.65f, 50)), snow = false),
            Scene("Crunch crunch! The little reindeer practised every day.", DAY_SKY, SNOW, listOf(Actor("reindeer", 0.2f, 60, up = 10f, dx = 0.5f, dy = 30f), Actor("@Pepper", 0.85f, 90))),
            Scene("Then... WHOOSH! Up, up into the sky!", DAY_SKY, SNOW, listOf(Actor("reindeer", 0.3f, 60, up = 20f, dx = 0.4f, dy = 90f))),
            Scene("On Christmas Eve, the littlest reindeer pulled Santa's sleigh!", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.1f, 50, up = 90f, dx = 0.55f, dy = 20f), Actor("reindeer", 0.3f, 50, up = 95f, dx = 0.55f, dy = 20f))),
            Scene("Even the littlest can do BIG things. THE END 🦌", NIGHT_SKY, SNOW, listOf(Actor("reindeer", 0.5f, 90), Actor("santa", 0.2f, 110))),
        ),
    ),
    Movie(
        "The Great Cookie Mix-Up", 5, "cookie_ginger", listOf(
            Scene("It was baking day at the Christmas Town bakery.", 0xFFE3C2L, WOOD, listOf(Actor("@Pepper", 0.3f, 100), Actor("cookie_sugar", 0.65f, 40), Actor("cookie_choco", 0.8f, 40)), snow = false),
            Scene("Oops! Pepper put in salt instead of sugar! 🧂", 0xFFE3C2L, WOOD, listOf(Actor("@Pepper", 0.4f, 100, dx = 0.05f)), snow = false),
            Scene("Then the gingerbread man jumped up and ran away!", 0xFFE3C2L, WOOD, listOf(Actor("ginger", 0.3f, 60, dx = 0.6f, dy = 20f)), snow = false),
            Scene("\"Come back!\" Everyone chased him through the snow.", DAY_SKY, SNOW, listOf(Actor("ginger", 0.45f, 50, dx = 0.5f), Actor("@me", 0.05f, 90, dx = 0.45f), Actor("@Jingle", -0.15f, 90, dx = 0.45f))),
            Scene("He hid inside a gingerbread house. Sneaky!", DAY_SKY, SNOW, listOf(Actor("ginghouse", 0.55f, 100), Actor("@me", 0.2f, 90))),
            Scene("\"I just wanted to see the Christmas lights!\" he said.", NIGHT_SKY, SNOW, listOf(Actor("ginger", 0.4f, 60), Actor("tree", 0.7f, 140))),
            Scene("So everyone saw the lights together, with new sugar cookies. THE END 🍪", NIGHT_SKY, SNOW, listOf(Actor("tree", 0.5f, 140), Actor("ginger", 0.25f, 50), Actor("@Pepper", 0.78f, 90), Actor("cookie_sugar", 0.1f, 30))),
        ),
    ),
    Movie(
        "Santa's Missing Sleigh Bells", 6, "santa", listOf(
            Scene("The night before Christmas Eve, Santa looked worried.", NIGHT_SKY, WOOD, listOf(Actor("santa", 0.4f, 130), Actor("fireplace", 0.8f, 90)), snow = false),
            Scene("\"My sleigh bells are GONE! The reindeer can't fly without them!\"", NIGHT_SKY, WOOD, listOf(Actor("santa", 0.3f, 130), Actor("reindeer", 0.75f, 90, flip = true)), snow = false),
            Scene("The elves searched the workshop...", 0xFFD9D2L, WOOD, listOf(Actor("@me", 0.25f, 100, dx = 0.2f), Actor("teddy", 0.75f, 50), Actor("train", 0.9f, 35)), snow = false),
            Scene("...the stable, and the snowy park...", DAY_SKY, SNOW, listOf(Actor("@Holly", 0.2f, 100, dx = 0.2f), Actor("snowman", 0.75f, 110))),
            Scene("Jingle jingle! Something rang inside a snow globe!", DAY_SKY, WOOD, listOf(Actor("snowglobe", 0.5f, 90), Actor("@me", 0.2f, 100)), snow = false),
            Scene("A cheeky penguin had borrowed them to make music! 🐧", DAY_SKY, SNOW, listOf(Actor("pet_penguin", 0.5f, 60, dy = 15f), Actor("rd_bells", 0.7f, 40))),
            Scene("Bells back on! Off flew Santa, ho ho ho! THE END 🔔", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.0f, 55, up = 100f, dx = 0.6f, dy = 20f), Actor("reindeer", 0.22f, 55, up = 105f, dx = 0.6f, dy = 20f))),
        ),
    ),
)

