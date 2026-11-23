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
/** Lexi's pay rule: every 2 jobs you finish pays \$2,000, and finishing the sleigh deliveries pays \$1,000 extra. */
const val PAY_PER_TWO_JOBS = 2000
const val SLEIGH_BONUS = 1000
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

class Npc(
    val name: String, val look: ElfLook, val job: String, val lines: List<String>, val bestFriendGift: Item,
    /** How they say who they are, their favourite thing, and how they say hello (for the phone chat). */
    val about: String = job, val favorite: String = "", val hello: String = "",
)

/** Mr. Silverbell, the old elf who runs the Christmas Town Bank. */
val bankerLook = ElfLook(girl = false, skin = 1, hairStyle = HairStyle.Short, hairColor = 6, eye = 0, outfit = Outfit.Sweater, outfitColor = 2, hat = Hat.TopHat, hatColor = 2)

val npcs = listOf(
    Npc(
        "Jingle", ElfLook(false, 1, HairStyle.Short, 3, 2, Outfit.Overalls, 0, Hat.Beanie, 1), "Toy Maker",
        listOf(
            "Santa pays \$2,000 for every 2 jobs you finish! And \$1,000 extra for the sleigh!",
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
    // ---- More of Christmas Town ----
    Npc(
        "Tinsel", ElfLook(girl = true, skin = 0, hairStyle = HairStyle.Bun, hairColor = 4, eye = 1, outfit = Outfit.Dress, outfitColor = 6, hat = Hat.None), "Shopkeeper",
        listOf(
            "Welcome to my shop! I've got clothes, hats, furniture and food!",
            "The Crown is my fanciest hat. It sparkles SO much! 👑",
            "New seeds come in every morning. Fresh from the snowy fields!",
            "I love when elves try on outfits. Twirl for me! 💃",
            "Saving up for something big? Mr. Silverbell's bank adds a little every night!",
        ),
        Item.Poinsettia, about = "I run the Shop on Main Street 🛍️", favorite = "Pretty hats! I have a hat for every day of the week 🎩", hello = "Welcome, welcome! 🛍️ Looking for anything special today?",
    ),
    Npc(
        "Mr. Silverbell", bankerLook, "Banker",
        listOf(
            "I've worked at the bank for 300 years! Elves live a very long time, you know.",
            "Money in the bank is safe, even from the cheekiest penguin! 🐧",
            "When I was a young elf, a candy cane cost just one penny!",
            "Saving up is clever. Then you can buy something really BIG later.",
            "Shh! Santa keeps his money in my bank too. 🤫",
        ),
        Item.Clock, about = "I look after everyone's savings at the Bank 🏦", favorite = "A good cup of tea and a tidy ledger ☕📒", hello = "Good day, young elf! 🎩 How are your savings?",
    ),
    Npc(
        "Maple", ElfLook(girl = true, skin = 2, hairStyle = HairStyle.Long, hairColor = 3, eye = 2, outfit = Outfit.Sweater, outfitColor = 1, hat = Hat.Beanie, hatColor = 0), "Mail Elf",
        listOf(
            "I carry ALL the letters kids send to Santa. My bag is SO heavy! ✉️",
            "One kid asked Santa for a pet dinosaur! 🦕",
            "I read the addresses really carefully so no letter gets lost.",
            "Snow or storm, the mail always gets through!",
            "Want to know a secret? Lots of kids draw pictures of the reindeer for Santa.",
        ),
        Item.Picture, about = "I'm the Mail Elf. I bring kids' letters to Santa ✉️", favorite = "Letters with drawings in them! 🖍️", hello = "Special delivery! 📬 It's a big hello for you!",
    ),
    Npc(
        "Nutmeg", ElfLook(girl = false, skin = 1, hairStyle = HairStyle.Short, hairColor = 0, eye = 2, outfit = Outfit.Overalls, outfitColor = 0, hat = Hat.Elf, hatColor = 5), "Gardener",
        listOf(
            "Water your plants and they grow big and strong! 💧",
            "Carrots are the reindeer's favourite. Grow lots!",
            "I talk to my plants. They're very good listeners. 🌱",
            "Did you know peppermint smells like candy canes?",
            "The Greenhouse is warm even when it snows outside!",
        ),
        Item.Candles, about = "I'm a gardener. I grow the yummiest veggies 🥕", favorite = "Watching the first little sprout pop up 🌱", hello = "Howdy! 🌻 I've got mud on my boots but a smile on my face!",
    ),
    Npc(
        "Buttons", ElfLook(girl = true, skin = 3, hairStyle = HairStyle.Bob, hairColor = 1, eye = 3, outfit = Outfit.Sweater, outfitColor = 3, hat = Hat.Earmuffs, hatColor = 4), "Tailor",
        listOf(
            "I sew all the elf outfits! Even Santa's big red coat. 🧵",
            "Pockets! Every outfit needs pockets!",
            "Do you like stripes or spots? I like BOTH.",
            "I once sewed a sweater for a penguin. It was very tiny.",
            "Your outfit looks super cute today!",
        ),
        Item.Rug, about = "I'm the tailor. I sew outfits for every elf 🧵", favorite = "Buttons, of course! I collect them 🔘", hello = "Ooh, hello! 🧵 Love your outfit today!",
    ),
    Npc(
        "Gingersnap", ElfLook(girl = false, skin = 0, hairStyle = HairStyle.Short, hairColor = 2, eye = 0, outfit = Outfit.Tunic, outfitColor = 5, hat = Hat.Santa), "Cocoa Cart",
        listOf(
            "Hot cocoa! Get your hot cocoa! ☕",
            "The secret is LOTS of marshmallows.",
            "I sell cocoa in the Town Square every day.",
            "Peppermint cocoa is my special recipe! 🍬",
            "Pepper is my cookie hero. Her cookies and my cocoa are a perfect match!",
        ),
        Item.GingerbreadHouse, about = "I run the hot cocoa cart in the Town Square ☕", favorite = "Marshmallows. Big fluffy ones 😋", hello = "Hiya! ☕ Want a marshmallow? I always have spares!",
    ),
    Npc(
        "Snowflake", ElfLook(girl = true, skin = 1, hairStyle = HairStyle.Pigtails, hairColor = 6, eye = 1, outfit = Outfit.Dress, outfitColor = 2, hat = Hat.Earmuffs, hatColor = 7), "Ice Skater",
        listOf(
            "I can spin on one skate! Wheeee! ⛸️",
            "The frozen pond in the park is perfect for skating.",
            "Falling down is part of learning. Just get back up!",
            "Every snowflake is different. Just like every elf! ❄️",
            "I'm practising a twirl for the Christmas show!",
        ),
        Item.Snowman, about = "I'm an ice skater! ⛸️ I twirl on the frozen pond", favorite = "Twirling so fast the world goes swirly 🌀", hello = "Hi hi! ⛸️ *twirls* Did you see that?!",
    ),
    Npc(
        "Bramble", ElfLook(girl = false, skin = 2, hairStyle = HairStyle.Short, hairColor = 1, eye = 0, outfit = Outfit.Overalls, outfitColor = 1, hat = Hat.Beanie, hatColor = 6), "Woodcarver",
        listOf(
            "I carve wooden toys. Rocking horses are my favourite! 🐴",
            "Measure twice, cut once. That's the woodcarver's rule.",
            "Smell that? Fresh pine wood! 🌲",
            "I made the chairs in the cinema. Comfy, right?",
            "Every toy starts as a little block of wood.",
        ),
        Item.RockingChair, about = "I'm a woodcarver. I make wooden toys and furniture 🪵", favorite = "The smell of fresh pine wood 🌲", hello = "Well hello there! 🪵 Mind the wood shavings!",
    ),
    Npc(
        "Poppy", ElfLook(girl = true, skin = 2, hairStyle = HairStyle.Long, hairColor = 5, eye = 3, outfit = Outfit.Dress, outfitColor = 1, hat = Hat.Elf, hatColor = 0), "Musician",
        listOf(
            "La la la! I play the jingle bells in the town band. 🎶",
            "Music makes everything more Christmassy!",
            "Can you hum Jingle Bells? I bet you can!",
            "I'm writing a brand new song about reindeer. 🦌🎵",
            "Clap along with me! Clap, clap, clap!",
        ),
        Item.Garland, about = "I'm a musician! 🎶 I play the jingle bells", favorite = "Singing carols in the snow 🎤", hello = "🎵 Hel-lo-o! 🎵 That's how I say hi!",
    ),
    Npc(
        "Pip", ElfLook(girl = false, skin = 3, hairStyle = HairStyle.Bob, hairColor = 4, eye = 2, outfit = Outfit.Snowsuit, outfitColor = 2, hat = Hat.Beanie, hatColor = 1), "Little Elf",
        listOf(
            "I'm the littlest elf in town! But I'm SUPER fast. 💨",
            "Sledding is the BEST. Wheeee! 🛷",
            "When I grow up I want to drive Santa's sleigh!",
            "Do you want to race? Ready, set, GO!",
            "I lost a tooth today! 🦷",
        ),
        Item.Sled, about = "I'm Pip, the littlest elf! I go to elf school 🎒", favorite = "Sledding down the BIGGEST hill 🛷", hello = "HI!!! 👋 Wanna play?!",
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
    add(Sticker("dance", "Had a dance party", "candy"))
    add(Sticker("friends10", "Made 10 friends in Christmas Town", "ginghouse"))
    add(Sticker("favour", "Did a favour for a friend", "gift"))
    add(Sticker("letter", "Wrote back to a letter", "mailbox"))
    add(Sticker("birthday", "Wished a friend happy birthday", "cake"))
    add(Sticker("skate", "Twirled 6 times on the ice", "snowman"))
    add(Sticker("sled", "Won the sled race against Pip", "sled"))
    add(Sticker("photo", "Took a photo with friends", "presents"))
    add(Sticker("restaurant", "Ate at Elf's Restaurant", "spaghetti"))
    add(Sticker("mybday", "Had a birthday party!", "cake"))
}

/** Sticker milestones and their prizes. */
val stickerPrizes: List<Pair<Int, Surprise>> = listOf(
    10 to item(Item.Presents), 25 to hat(Hat.TopHat), 40 to item(Item.ToyChest), 60 to hat(Hat.Crown),
)

// MARK: - Activities (the icons on the main screen)

enum class Side { Left, Right, Hidden }   // Hidden places open from the buttons at the bottom of the main screen   // Hidden places open from the buttons at the bottom of the main screen

/** Each icon on the main screen. [sprite] is its pixel-art picture ("" = drawn specially). */
enum class Place(val title: String, val sprite: String, val side: Side, val color: Color) {
    Greenhouse("Greenhouse", "wheat", Side.Left, hex(0x3FAF5A)),
    Bakery("Bakery", "cookie_sugar", Side.Left, hex(0xE0843A)),
    Stable("Stable", "reindeer", Side.Left, hex(0x9A5B34)),
    Workshop("Workshop", "teddy", Side.Left, hex(0xE5394B)),
    SantaList("Santa's List", "santa", Side.Left, hex(0xA02338)),
    Sleigh("Sleigh", "sleigh", Side.Left, hex(0x3D6FD6)),
    Park("Snow Park", "snowman", Side.Left, hex(0x5BA4D9)),
    Bank("Bank", "moneybag", Side.Left, hex(0xD9A21B)),
    Decorate("Decorate", "tree", Side.Right, hex(0x2E7D4A)),
    Shop("Shop", "presents", Side.Right, hex(0x9B6BE0)),
    Closet("Closet", "", Side.Right, hex(0xFF8FB0)),
    Friends("Friends", "", Side.Right, hex(0x2EB5A5)),
    Phone("Phone", "phone", Side.Right, hex(0x3D6FD6)),
    Snacks("Snacks", "cocoa_mug", Side.Right, hex(0xC98B5E)),
    Pets("Pets", "pet_penguin", Side.Right, hex(0xF2A93B)),
    Town("Town", "house", Side.Hidden, hex(0x3D6FD6)),
    House("House", "house", Side.Hidden, hex(0x9A5B34)),
    Mail("Mailbox", "mailbox", Side.Hidden, hex(0xE5394B)),
    Restaurant("Elf's Restaurant", "spaghetti", Side.Hidden, hex(0xC0392B)),   // on the Town map
    Birthday("Birthday Party", "cake", Side.Hidden, hex(0xFF8FB0)),   // visiting a friend (GameState.visiting)
    Party("Party", "balloon", Side.Right, hex(0xFF5FA2)),
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

/** Snacks and drinks from the cinema snack bar, to munch during the movie. */
enum class MovieSnack(val title: String, val price: Int, val energy: Int, val sprite: String) {
    Popcorn("Popcorn", 4, 6, "popcorn"),
    Candy("Candy", 2, 4, "candy"),
    Cocoa("Hot Cocoa", 3, 5, "cocoa_mug"),
    OrangeJuice("Orange Juice", 2, 4, "juice_orange"),
    GrapeJuice("Grape Juice", 2, 4, "juice_grape"),
    FruitPunch("Fruit Punch", 3, 5, "juice_punch"),
}

/** You can bring up to this many elf friends to a movie. */
const val MAX_MOVIE_FRIENDS = 3

/** Cinema rows, far (front, near the screen) to near (the back row). */
val cinemaRowSeats = listOf(7, 6, 5)

/** Seats other elves have already taken for this movie today. Always leaves plenty free. */
fun takenSeats(movie: Movie, day: Int): Set<Pair<Int, Int>> {
    val r = kotlin.random.Random(movie.title.hashCode() * 31 + day)
    val all = cinemaRowSeats.flatMapIndexed { row, n -> (0 until n).map { row to it } }
    return all.shuffled(r).take(7).toSet()
}

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
    Movie(
        "All Alone at the North Pole", 5, "pet_penguin", listOf(
            Scene("On Christmas Eve, everyone flew off on the sleigh... but Pip was asleep in the toy chest!", NIGHT_SKY, WOOD, listOf(Actor("toychest", 0.3f, 60), Actor("sleigh", 0.6f, 40, up = 120f, dx = 0.5f, dy = 30f)), snow = false),
            Scene("Pip woke up. \"Hello? Anybody?\" The whole workshop was empty!", NIGHT_SKY, WOOD, listOf(Actor("@Pip", 0.5f, 110), Actor("presents", 0.85f, 50)), snow = false),
            Scene("Then two cheeky penguins waddled in. They wanted Santa's cookies! 🍪", NIGHT_SKY, WOOD, listOf(Actor("pet_penguin", 0.9f, 55, dx = -0.3f), Actor("pet_penguin", 1.05f, 50, dx = -0.3f), Actor("cookie_sugar", 0.2f, 35)), snow = false),
            Scene("Pip had a plan! Marbles on the floor... and a slippery sled ramp...", NIGHT_SKY, WOOD, listOf(Actor("@Pip", 0.25f, 100), Actor("sled", 0.65f, 40)), snow = false),
            Scene("WHOOSH! The penguins slid right out the door and into a snowbank!", NIGHT_SKY, SNOW, listOf(Actor("pet_penguin", 0.2f, 50, dx = 0.7f, dy = 20f), Actor("pet_penguin", 0.05f, 45, dx = 0.7f, dy = 20f), Actor("snowman", 0.9f, 90))),
            Scene("Santa came home and found the cookies safe. \"Ho ho ho! Well done, Pip!\"", NIGHT_SKY, WOOD, listOf(Actor("santa", 0.35f, 130), Actor("@Pip", 0.65f, 100), Actor("cookie_sugar", 0.85f, 30)), snow = false),
            Scene("And the penguins got cookies too, because it's Christmas. THE END 🐧", DAY_SKY, SNOW, listOf(Actor("pet_penguin", 0.3f, 55), Actor("pet_penguin", 0.5f, 50), Actor("cookie_choco", 0.7f, 30), Actor("@Pip", 0.85f, 90))),
        ),
    ),
    Movie(
        "The Night We Met Santa", 6, "sleigh", listOf(
            Scene("Two little elves stayed up really late. They wanted to SEE Santa! 👀", NIGHT_SKY, WOOD, listOf(Actor("@me", 0.25f, 100), Actor("@Pip", 0.45f, 90), Actor("tree", 0.8f, 130)), snow = false),
            Scene("Thump! Something came down the chimney...", NIGHT_SKY, WOOD, listOf(Actor("fireplace", 0.5f, 100), Actor("presents", 0.8f, 50)), snow = false),
            Scene("\"Ho ho ho! You're still awake?\" It was SANTA!", NIGHT_SKY, WOOD, listOf(Actor("santa", 0.6f, 140), Actor("@me", 0.2f, 100), Actor("@Pip", 0.36f, 90)), snow = false),
            Scene("Oh no! The reindeer got spooked, and the presents fell out of the sleigh!", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.3f, 50), Actor("reindeer", 0.6f, 60, dx = 0.3f), Actor("gift", 0.2f, 30), Actor("gift", 0.8f, 30))),
            Scene("The elves helped find every present in the snow.", NIGHT_SKY, SNOW, listOf(Actor("@me", 0.25f, 100, dx = 0.1f), Actor("@Pip", 0.6f, 90, dx = 0.1f), Actor("gift", 0.45f, 30), Actor("presents", 0.85f, 50))),
            Scene("Then Santa let them ride in the sleigh to deliver them all! WHEEE! 🛷", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.05f, 55, up = 100f, dx = 0.6f, dy = 30f), Actor("reindeer", 0.27f, 55, up = 105f, dx = 0.6f, dy = 30f))),
            Scene("Back home in bed, they smiled. Best. Night. EVER. THE END 🎅", NIGHT_SKY, WOOD, listOf(Actor("@me", 0.35f, 100), Actor("@Pip", 0.6f, 90)), snow = false),
        ),
    ),
    Movie(
        "A Day in the Life of an Elf", 3, "teddy", listOf(
            Scene("Ring ring! The alarm clock rings. Time to get up, little elf! ⏰", DAY_SKY, WOOD, listOf(Actor("bed", 0.35f, 60), Actor("@me", 0.7f, 100, dy = 10f)), snow = false),
            Scene("First job: bake cookies! Stir, stir, stir! 🍪", 0xFFE3C2L, WOOD, listOf(Actor("@me", 0.3f, 100), Actor("cookie_sugar", 0.6f, 40), Actor("cookie_choco", 0.78f, 40)), snow = false),
            Scene("Then make toys in the Workshop! Bang, bang, bang! 🔨", 0xFFD9D2L, WOOD, listOf(Actor("@me", 0.25f, 100), Actor("teddy", 0.55f, 50), Actor("train", 0.8f, 40)), snow = false),
            Scene("Feed the reindeer their carrots. Munch munch! 🥕", DAY_SKY, SNOW, listOf(Actor("reindeer", 0.6f, 90), Actor("carrot", 0.35f, 30), Actor("@me", 0.15f, 100))),
            Scene("Fly the sleigh and deliver presents! Wheee! 🛷", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.05f, 55, up = 100f, dx = 0.6f, dy = 20f), Actor("reindeer", 0.27f, 55, up = 105f, dx = 0.6f, dy = 20f))),
            Scene("Then a party with friends! 🎉", NIGHT_SKY, WOOD, listOf(Actor("@me", 0.3f, 100), Actor("@Twinkle", 0.55f, 100), Actor("presents", 0.82f, 50)), snow = false),
            Scene("Night night, little elf. Tomorrow we do it all again! THE END 💤", NIGHT_SKY, WOOD, listOf(Actor("bed", 0.5f, 70)), snow = false),
        ),
    ),
    Movie(
        "The Biggest Little Elf", 5, "tree", listOf(
            Scene("One morning, Bramble woke up... and he was SUPER tall! 😲", DAY_SKY, WOOD, listOf(Actor("@Bramble", 0.5f, 170)), snow = false),
            Scene("BONK! He bumped his head on the door. \"Ouch!\"", DAY_SKY, WOOD, listOf(Actor("@Bramble", 0.5f, 170, dy = -5f), Actor("@Pip", 0.2f, 70)), snow = false),
            Scene("His chair was too small. His bed was too small. Even his hat was too small!", DAY_SKY, WOOD, listOf(Actor("@Bramble", 0.6f, 170), Actor("rockchair", 0.25f, 50)), snow = false),
            Scene("\"I don't fit anywhere!\" Bramble felt sad. 😢", DAY_SKY, SNOW, listOf(Actor("@Bramble", 0.5f, 170))),
            Scene("But oh no! The star fell off the very top of the Town Tree!", NIGHT_SKY, SNOW, listOf(Actor("tree", 0.5f, 150), Actor("@Pip", 0.2f, 70), Actor("@Poppy", 0.8f, 80))),
            Scene("Nobody could reach... except Bramble! He put the star right back on top! ⭐", NIGHT_SKY, SNOW, listOf(Actor("tree", 0.5f, 150), Actor("@Bramble", 0.72f, 170))),
            Scene("Being different made him special. Hooray for Bramble! THE END 🌟", NIGHT_SKY, SNOW, listOf(Actor("@Bramble", 0.5f, 170), Actor("@Pip", 0.2f, 70), Actor("@me", 0.82f, 90))),
        ),
    ),
    Movie(
        "The Pumpkin Who Found Christmas", 6, "pumpkin", listOf(
            Scene("Far, far away is spooky Pumpkin Town, where it's ALWAYS Halloween. 🎃", NIGHT_SKY, 0x3A2A4AL, listOf(Actor("pumpkin", 0.3f, 60), Actor("pumpkin", 0.7f, 45)), snow = false),
            Scene("One night, Jack the pumpkin found a sparkly snow globe...", NIGHT_SKY, 0x3A2A4AL, listOf(Actor("pumpkin", 0.35f, 60), Actor("snowglobe", 0.65f, 60)), snow = false),
            Scene("WHOOSH! He tumbled right into Christmas Town! Snow? What's SNOW?! ❄️", DAY_SKY, SNOW, listOf(Actor("pumpkin", 0.4f, 60, dy = 30f), Actor("tree", 0.8f, 130))),
            Scene("Jack wanted to make toys too. But his toys were a little bit... spooky! 👻", 0xFFD9D2L, WOOD, listOf(Actor("pumpkin", 0.3f, 60), Actor("robot", 0.6f, 50), Actor("drum", 0.82f, 40)), snow = false),
            Scene("\"It's okay!\" said the elves. \"We'll show you how!\" 🧸", 0xFFD9D2L, WOOD, listOf(Actor("@me", 0.25f, 100), Actor("@Jingle", 0.5f, 100), Actor("pumpkin", 0.78f, 55), Actor("teddy", 0.92f, 35)), snow = false),
            Scene("Santa laughed. \"Ho ho ho! A pumpkin elf!\" and gave Jack a tiny hat. 🎅", NIGHT_SKY, SNOW, listOf(Actor("santa", 0.35f, 130), Actor("pumpkin", 0.65f, 60))),
            Scene("Now every year, Halloween and Christmas visit each other! THE END 🎃🎄", NIGHT_SKY, SNOW, listOf(Actor("pumpkin", 0.25f, 55), Actor("tree", 0.55f, 130), Actor("@me", 0.82f, 90))),
        ),
    ),
    Movie(
        "Santa's Snowstorm Rescue", 4, "sleigh", listOf(
            Scene("On Christmas Eve, a GIANT snowstorm blew into the sky! 🌨️", NIGHT_SKY, SNOW, listOf(Actor("snowman", 0.3f, 90), Actor("tree", 0.75f, 120))),
            Scene("Clunk! Santa's sleigh got stuck in a snowdrift. Oh no!", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.45f, 50), Actor("santa", 0.75f, 110), Actor("reindeer", 0.15f, 60))),
            Scene("Holly the inventor zoomed in with her Snow-Zapper 3000! 🔬", NIGHT_SKY, SNOW, listOf(Actor("@Holly", 0.1f, 100, dx = 0.3f), Actor("sleigh", 0.7f, 50))),
            Scene("ZAP! The snow melted... but the reindeer couldn't see in the storm.", NIGHT_SKY, SNOW, listOf(Actor("reindeer", 0.3f, 60), Actor("reindeer", 0.6f, 55), Actor("@Holly", 0.85f, 90))),
            Scene("So the elves held up glowing snow globes to light the way! ✨", NIGHT_SKY, SNOW, listOf(Actor("@me", 0.2f, 100), Actor("snowglobe", 0.4f, 40), Actor("@Pip", 0.62f, 80), Actor("snowglobe", 0.8f, 40))),
            Scene("Up, up and away! Santa flew through the storm! 🛷", NIGHT_SKY, SNOW, listOf(Actor("sleigh", 0.05f, 55, up = 100f, dx = 0.6f, dy = 30f), Actor("reindeer", 0.27f, 55, up = 105f, dx = 0.6f, dy = 30f))),
            Scene("Every present arrived just in time. Teamwork! THE END 🎁", DAY_SKY, SNOW, listOf(Actor("presents", 0.3f, 60), Actor("santa", 0.55f, 120), Actor("@Holly", 0.8f, 90))),
        ),
    ),
    Movie(
        "That Snowy Christmas", 4, "snowman", listOf(
            Scene("It was the snowiest Christmas EVER in Christmas Town. ❄️", DAY_SKY, SNOW, listOf(Actor("snowman", 0.3f, 110), Actor("tree", 0.75f, 130))),
            Scene("So much snow! The roads were blocked and the sleigh got stuck.", DAY_SKY, SNOW, listOf(Actor("sleigh", 0.45f, 50), Actor("reindeer", 0.75f, 60, flip = true))),
            Scene("Santa was so tired, he mixed up the presents! Maple got a drum, and Poppy got a robot!", NIGHT_SKY, WOOD, listOf(Actor("santa", 0.25f, 130), Actor("drum", 0.6f, 40), Actor("robot", 0.82f, 50)), snow = false),
            Scene("Everyone was a bit sad... until Poppy started drumming a Christmas song! 🥁", DAY_SKY, SNOW, listOf(Actor("@Poppy", 0.4f, 110), Actor("drum", 0.62f, 40))),
            Scene("Maple built the robot, and it danced along! Beep boop! 🤖", DAY_SKY, SNOW, listOf(Actor("@Maple", 0.3f, 110), Actor("robot", 0.65f, 60, dy = 20f))),
            Scene("The whole town sang together in the snow. 🎶", DAY_SKY, SNOW, listOf(Actor("@Poppy", 0.2f, 100), Actor("@Maple", 0.42f, 100), Actor("@me", 0.64f, 100), Actor("tree", 0.88f, 120))),
            Scene("The best present was being together with friends. THE END ❄️", NIGHT_SKY, SNOW, listOf(Actor("tree", 0.5f, 140), Actor("@Poppy", 0.2f, 90), Actor("@Maple", 0.8f, 90))),
        ),
    ),
)


// MARK: - Bank

/** Mr. Silverbell, the old elf who runs the Christmas Town Bank. */
const val BANKER = "Mr. Silverbell"

/** Every night the bank adds a little extra to your savings: $5 for every $100 (up to $500 a night). */
fun bankInterest(saved: Int) = (saved / 100 * 5).coerceAtMost(500)

val bankerHellos = listOf(
    "Good day to you, young elf! Welcome to the Christmas Town Bank. How may I help you today?",
    "Ah, hello again! Lovely to see you. What can I do for you today?",
    "Welcome, welcome! Mind the snow on your boots. How can I help?",
    "Hello there! My, what a hard-working elf you are. What would you like to do today?",
)
val bankerChats = listOf(
    "I've worked at this bank for 300 years! Elves live a very long time, you know. 🎩",
    "Money in the bank is safe, even from the cheekiest penguin! 🐧",
    "Every night I add a little extra to your savings. \$5 for every \$100! That's called interest.",
    "When I was a young elf, a candy cane cost just one penny! 🍬",
    "Saving up is clever! Then you can buy something really BIG later. 🏠",
    "Santa keeps his money here too. But shhh, that's a secret! 🤫",
)

// MARK: - Friends' houses

/** What a friend's house looks like inside (and its outside colours on the Town map). */
class FriendHome(val wall: Long, val stripe: Long, val outside: Long, val placed: List<Placed>, val pet: PetKind?)

private val homeColors = listOf(
    0xE3F0D8L to 0xD3E6C4L, 0xF6D7E3L to 0xEFC3D3L, 0xFFF1C9L to 0xF6E2A6L,
    0xD9D4F7L to 0xC6BFF0L, 0xD7ECF6L to 0xC2E0EFL, 0xFBE0CCL to 0xF4CDB0L,
)
private val outsideColors = listOf(0xF4E3C3L, 0xE8D5F2L, 0xD5EBDDL, 0xF7D9D9L, 0xD6E6F5L, 0xFFF0B8L)

/** Every friend's house is a little different, but always the same for that friend. */
fun homeOf(npc: Npc): FriendHome {
    val r = kotlin.random.Random(npc.name.hashCode())
    val (wall, stripe) = homeColors[r.nextInt(homeColors.size)]
    val floorItems = Item.entries.filter { it.spot == Spot.Floor && it != Item.Bed }.shuffled(r).take(3)
    val wallItems = Item.entries.filter { it.spot == Spot.Wall }.shuffled(r).take(2)
    val placed = buildList {
        if (r.nextBoolean()) add(Placed(0, Item.Rug, 0, 0.5f))
        floorItems.zip(listOf(0.1f, 0.3f, 0.9f)).forEachIndexed { i, (item, x) -> add(Placed(1L + i, item, 0, x)) }
        wallItems.zip(listOf(0.12f, 0.88f)).forEachIndexed { i, (item, x) -> add(Placed(10L + i, item, 0, x, 0.1f)) }
    }
    return FriendHome(wall, stripe, outsideColors[r.nextInt(outsideColors.size)], placed, if (r.nextInt(3) > 0) PetKind.entries.random(r) else null)
}

/** You can have this many friends over at your house at once. */
const val MAX_HOME_GUESTS = 3

// MARK: - Workmates: the friend who works next to you at each job

class Workmate(val place: Place, val name: String, val tips: List<String>, val cheer: String, val help: String)

/** Built on first use (lazy) so it never runs before the Place list exists. */
val workmates: List<Workmate> by lazy { listOf(
    Workmate(
        Place.Greenhouse, "Nutmeg",
        listOf("Plant a seed, then water it! Thirsty plants don't grow 💧", "Carrots are for the reindeer, wheat is for cookies 🌾🥕", "Pick crops when they're ready, then plant again!", "Out of seeds? Tinsel sells them in the Shop 🛍️"),
        "Look at those crops grow! 🌱", "I picked extra for you! +2 wheat, +2 carrots 🌾🥕",
    ),
    Workmate(
        Place.Bakery, "Pepper",
        listOf("Stir quickly for a perfect batch! 🥄", "Every cookie needs wheat. Berries and choc chips make them extra yummy!", "Decorate them pretty, Santa loves sprinkles ✨", "Tired? A cookie gives you energy, but save some for Santa!"),
        "Perfect batch, sweetie! 🍪", "I baked one just for you! +1 cookie 🍪",
    ),
    Workmate(
        Place.Stable, "Sprout",
        listOf("Every reindeer needs feeding AND brushing 🦌", "Rub back and forth to brush. They love it!", "Out of carrots? Grow some in the Greenhouse 🥕", "Happy reindeer fly better on the sleigh!"),
        "The reindeer love you! 🦌💕", "I brought carrots from home! +3 carrots 🥕",
    ),
    Workmate(
        Place.Workshop, "Jingle",
        listOf("Tap fast to fill, then join the dots 1 to 5! 🧵", "Pick a colour you love for the paint 🎨", "Made toys go on the sleigh, or wrap them first 🎁", "New toys unlock when you level up!"),
        "Toys done! High five! ✋", "I made a spare teddy for you! +1 teddy 🧸",
    ),
    Workmate(
        Place.SantaList, "Maple",
        listOf("Read what each kid did really carefully ✉️", "Helping, sharing and being kind are NICE 😇", "Being mean or messy is a bit naughty 😈", "Santa checks his list twice. So do we!"),
        "Checked it twice! ✉️✅", "A kid sent you a thank-you letter! It makes you smile. +15 ⚡",
    ),
    Workmate(
        Place.Sleigh, "Holly",
        listOf("Steer around the clouds! ☁️", "Drop presents right down the chimneys 🎁", "Happy reindeer on your team make flying easier!", "I tuned the sleigh bells myself. Hear them jingle? 🔔"),
        "Smooth flying! 🛷✨", "I made you a power snack for the flight! +15 ⚡",
    ),
) }

fun workmateAt(p: Place) = workmates.firstOrNull { it.place == p }

/** Which workplace a job happens at. */
fun placeFor(kind: JobKind) = when (kind) {
    JobKind.Bake -> Place.Bakery
    JobKind.Toys, JobKind.Wrap -> Place.Workshop
    JobKind.Reindeer -> Place.Stable
    JobKind.SortList -> Place.SantaList
    JobKind.Deliver -> Place.Sleigh
    JobKind.Tree -> Place.Park
}

// MARK: - Talking and treats

/** Treats you can serve a friend at your house (or share at theirs). */
enum class Treat(val title: String, val sprite: String) { Cookie("Cookie", "cookie_sugar"), Tea("Tea", "teacup"), Cocoa("Cocoa", "cocoa_mug") }

val treatThanks = listOf(
    "Mmm, thank you! This is SO yummy! 😋", "Aww, you're the best host ever! 💖", "Ooh, my favourite! Thank you! ✨",
    "*sip* Ahh, warm and cozy. Thank you! ☕", "You make the best treats in Christmas Town! 🎄",
)
val howAreYouAnswers = listOf(
    "I'm super! Thanks for asking 😊", "A little sleepy, but SO happy to see you! 😴💖", "Great! I just had the yummiest cocoa ☕",
    "Busy busy! But never too busy for you 💕", "Fantastic! It snowed on my nose this morning ❄️",
)
val friendAnswers = listOf("Aww, YOU'RE a great friend too! 🥰", "That made my whole day! 💖", "Best friends forever! 🤝✨", "*happy dance* 💃 Thank you!")

// MARK: - Favours, letters, birthdays

/** Things a friend might ask you to bring them. */
@Serializable enum class Want(val one: String, val many: String, val sprite: String, val why: String) {
    Cookie("cookie", "cookies", "cookie_sugar", "I'm having friends over and I ran out of cookies!"),
    Toy("toy", "toys", "teddy", "My little cousin is visiting and has nothing to play with!"),
    Carrot("carrot", "carrots", "carrot", "My bunny is SO hungry!"),
    Wheat("wheat", "wheat", "wheat", "I want to bake bread, but I have no wheat!"),
    Cocoa("hot cocoa", "hot cocoas", "cocoa_mug", "Brrr! I'm freezing!"),
}

/** A friend asked you for help. Bring them what they need for a reward. */
@Serializable data class Favour(val from: String, val want: Want, val amount: Int, val done: Boolean = false) {
    val ask get() = "${want.why} Can you bring me $amount ${if (amount == 1) want.one else want.many}? 🙏"
}
const val FAVOUR_REWARD = 300

/** A letter in your mailbox. */
@Serializable data class Letter(val from: String, val text: String, val day: Int, val read: Boolean = false, val replied: Boolean = false)

val letterTemplates = listOf(
    "Dear %me, thank you for being my friend! You're the BEST! 💖",
    "Hi %me! Want to come over to my house today? I'll make cocoa! ☕",
    "%me! I drew you a picture of a reindeer! 🦌 (It's a bit wobbly.)",
    "Dear %me, everyone says you work SO hard. Santa is really proud! 🎅",
    "Guess what, %me? It snowed on my nose this morning! ❄️ Hee hee!",
    "Hi %me! Let's go to the Party Place and dance together! 💃",
    "Dear %me, I made a snowman and named him after you! ⛄",
)
val letterReplies = listOf("Thank you! 💖", "See you soon! 👋", "You're the best! ✨")

/** Every friend has a birthday on one day of each 28-day month. */
fun birthdayOf(npc: Npc) = 1 + (npc.name.hashCode().let { if (it < 0) -it else it } % 28)
fun isBirthday(npc: Npc, day: Int) = (day - 1) % 28 + 1 == birthdayOf(npc)

// MARK: - Photo booth

/** A photo from the Party Place photo booth: you plus friends, with silly props. */
@Serializable data class Photo(val people: List<String>, val prop: Int, val bg: Int, val day: Int)
val photoProps = listOf("🎩", "👑", "🕶️", "🎀", "🦌", "🎉")
val photoBackdrops = listOf(0xFF8FB0L, 0x9BC8EEL, 0xFFE08AL, 0xB79CFFL, 0x9FE0B0L)
const val MAX_PHOTOS = 12

// MARK: - Elf's Restaurant (Lexi's menu!)

enum class Dish(val title: String, val price: Int, val energy: Int, val sprite: String) {
    Spaghetti("Spaghetti", 12, 15, "spaghetti"),
    Ratatouille("Ratatouille", 14, 15, "ratatouille"),
    StrawberryLasagna("Strawberry Lasagna", 15, 18, "lasagna"),
    SugarPlums("Sugar Plums", 6, 8, "sugarplums"),
    Gingerbread("Gingerbread", 5, 8, "cookie_ginger"),
    ApplePie("Apple Pie", 8, 10, "pie"),
    Tanghulu("Christmas Tanghulu", 7, 8, "tanghulu"),
    HotChocolate("Hot Chocolate", 4, 6, "cocoa_mug"),
}
const val WAITER = "Gingersnap"
val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
val daysInMonth = listOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
const val MAX_PARTY_GUESTS = 5
