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
}

@Serializable enum class Hat(val title: String, val price: Int, val sprite: String?) {
    None("No Hat", 0, null), Elf("Elf Hat", 0, "hat_elf"), Beanie("Beanie", 300, "hat_beanie"),
    Santa("Santa Hat", 600, "hat_santa"), Antlers("Antlers", 500, "hat_antlers"), Crown("Crown", 1500, "hat_crown"),
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
}

const val HARVEST = 3

@Serializable enum class Cookie(val title: String, val recipe: Map<Crop, Int>, val unlockLevel: Int, val energy: Int, val sprite: String) {
    Sugar("Sugar Cookie", mapOf(Crop.Wheat to 2), 1, 15, "cookie_sugar"),
    BerryJam("Berry Cookie", mapOf(Crop.Wheat to 2, Crop.Berry to 1), 2, 20, "cookie_berry"),
    Choco("Choc Chip Cookie", mapOf(Crop.Wheat to 2, Crop.Cocoa to 1), 3, 25, "cookie_choco"),
}

@Serializable enum class Toy(val title: String, val unlockLevel: Int, val sprite: String, val steps: List<String>) {
    Teddy("Teddy Bear", 1, "teddy", listOf("Stuff the fluff", "Sew it up", "Tie a bow")),
    CandyCane("Candy Cane", 1, "candycane", listOf("Pull the sugar", "Add red stripes", "Bend the hook")),
    Train("Toy Train", 2, "train", listOf("Build the engine", "Add the wheels", "Paint it red")),
    Doll("Doll", 3, "doll", listOf("Make the face", "Brush the hair", "Sew the dress")),
    Drum("Drum", 4, "drum", listOf("Stretch the top", "Paint the stripes", "Add the sticks")),
    Robot("Robot", 5, "robot", listOf("Bolt the body", "Add the lights", "Switch it on")),
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

// MARK: - Reindeer

val reindeerNames = listOf("Dasher", "Dancer", "Prancer", "Vixen", "Comet", "Cupid", "Donner", "Blitzen", "Rudolph")
fun reindeerCount(level: Int) = listOf(2, 4, 6, 8, 9)[(level - 1).coerceIn(0, 4)]

// MARK: - Levels & jobs

val levelTitles = listOf("Helper Elf", "Cookie Elf", "Toy Elf", "Sleigh Elf", "Santa's Best Elf")
const val MAX_LEVEL = 5
const val PAY = 1000
const val CHRISTMAS_EVE_WORKDAYS = 10

/** Workdays needed to reach each level. */
fun levelFor(workdays: Int) = when {
    workdays >= 8 -> 5
    workdays >= 6 -> 4
    workdays >= 4 -> 3
    workdays >= 2 -> 2
    else -> 1
}

@Serializable enum class JobKind(val emoji: String) { Bake("🍪"), Toys("🧸"), Reindeer("🦌"), SortList("📜"), Deliver("🛷") }

@Serializable data class Job(val kind: JobKind, val goal: Int, val done: Int = 0) {
    val finished get() = done >= goal
    val text get() = when (kind) {
        JobKind.Bake -> "Bake $goal cookies"
        JobKind.Toys -> "Make $goal toys"
        JobKind.Reindeer -> "Feed & brush every reindeer"
        JobKind.SortList -> "Sort $goal kids on Santa's list"
        JobKind.Deliver -> "Deliver $goal presents"
    }
}

fun jobsFor(level: Int): List<Job> = buildList {
    add(Job(JobKind.Bake, listOf(2, 3, 3, 4, 4)[level - 1]))
    add(Job(JobKind.Toys, listOf(2, 3, 4, 5, 6)[level - 1]))
    add(Job(JobKind.Reindeer, 1))
    if (level >= 2) add(Job(JobKind.SortList, 5))
    if (level >= 3) add(Job(JobKind.Deliver, listOf(0, 0, 4, 5, 6)[level - 1]))
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
            "Rudolph joins the stable at Level 5. His nose glows!",
            "Seeds need water before they grow. 💧",
        ),
        Item.Poinsettia,
    ),
)

/** Twinkle's party: pick a theme, get a decoration. */
val partyThemes = listOf("Candy Land 🍭" to Item.GingerbreadHouse, "Winter Wonderland ⛄" to Item.Snowman, "Cozy Cabin 🕯️" to Item.Candles)

// MARK: - Activities (the icons on the main screen)

enum class Side { Left, Right }

/** Each icon on the main screen. [sprite] is its pixel-art picture ("" = drawn specially). */
enum class Place(val title: String, val sprite: String, val side: Side, val color: Color) {
    Greenhouse("Greenhouse", "wheat", Side.Left, hex(0x3FAF5A)),
    Bakery("Bakery", "cookie_sugar", Side.Left, hex(0xE0843A)),
    Stable("Stable", "reindeer", Side.Left, hex(0x9A5B34)),
    Workshop("Workshop", "teddy", Side.Left, hex(0xE5394B)),
    SantaList("Santa's List", "santa", Side.Left, hex(0xA02338)),
    Sleigh("Sleigh", "sleigh", Side.Left, hex(0x3D6FD6)),
    Decorate("Decorate", "tree", Side.Right, hex(0x2E7D4A)),
    Shop("Shop", "presents", Side.Right, hex(0x9B6BE0)),
    Closet("Closet", "", Side.Right, hex(0xFF8FB0)),
    Friends("Friends", "", Side.Right, hex(0x2EB5A5)),
    Snacks("Snacks", "cocoa_mug", Side.Right, hex(0xC98B5E)),
}
