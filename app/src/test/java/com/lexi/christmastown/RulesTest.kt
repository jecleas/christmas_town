package com.lexi.christmastown

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lexi's rules, and making sure old saves still load after the expansion. */
class RulesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test fun startsWithAThousandDollars() = assertEquals(1000, Save().money)

    @Test fun payIsTwoThousandPerTwoJobsPlusSleighBonus() {
        assertEquals(2000, PAY_PER_TWO_JOBS)
        assertEquals(1000, SLEIGH_BONUS)
    }

    @Test fun bankAddsFiveDollarsPerHundredUpTo500() {
        assertEquals(0, bankInterest(99))
        assertEquals(50, bankInterest(1000))
        assertEquals(500, bankInterest(1_000_000))
        // an old save has nothing in the bank and no pay counted yet today
        val old = json.decodeFromString<Save>("""{"name":"Lexi","money":4321}""")
        assertEquals(0, old.bankSaved)
        assertEquals(0, old.paidPairs)
    }

    @Test fun lotsOfFriendsEachWithTheirOwnHouse() {
        assertTrue(npcs.size >= 16)
        assertEquals(npcs.size, npcs.map { it.name }.toSet().size)
        for (n in npcs) {
            assertTrue(n.lines.isNotEmpty())
            assertEquals(homeOf(n).placed.size, homeOf(n).placed.size)   // same house every time
            assertTrue(homeOf(n).placed.isNotEmpty())
        }
        assertTrue(npcs.any { it.name == BANKER })
    }

    @Test fun moviesCostThreeToSixDollars() {
        assertTrue(movies.size >= 7)
        assertTrue(movies.all { it.price in 3..6 && it.scenes.isNotEmpty() })
        // every elf in a movie is a real friend
        val cast = movies.flatMap { m -> m.scenes.flatMap { it.actors } }.map { it.sprite }.filter { it.startsWith("@") && it != "@me" }
        assertTrue(cast.all { name -> npcs.any { it.name == name.drop(1) } })
    }

    @Test fun townPlacesAreInTown() {
        // Lexi wanted every place back on the main screen (the Town map is extra)
        for (p in listOf(Place.Shop, Place.Bank, Place.Party, Place.Park, Place.Friends)) assertTrue(p.side != Side.Hidden)
        val old = json.decodeFromString<Save>("""{"name":"Lexi"}""")
        assertTrue(old.homeGuests.isEmpty())
    }

    @Test fun everyWorkplaceHasAWorkmateFriend() {
        val places = listOf(Place.Greenhouse, Place.Bakery, Place.Stable, Place.Workshop, Place.SantaList, Place.Sleigh)
        for (p in places) assertTrue(workmateAt(p) != null)
        assertTrue(workmates.all { w -> npcs.any { it.name == w.name } && w.tips.isNotEmpty() })
    }

    @Test fun christmasEveAfterTenWorkdays() = assertEquals(10, CHRISTMAS_EVE_WORKDAYS)

    @Test fun eightLevelsReachedBeforeChristmasEve() {
        assertEquals(1, levelFor(0))
        assertEquals(MAX_LEVEL, 8)
        assertTrue((0..20).all { levelFor(it) in 1..MAX_LEVEL })
        assertTrue((0 until 20).all { levelFor(it) <= levelFor(it + 1) })
        assertTrue(levelFor(CHRISTMAS_EVE_WORKDAYS - 1) == MAX_LEVEL)
    }

    @Test fun rudolphOnlyAtTheTop() {
        assertEquals("Rudolph", reindeerNames.last())
        for (l in 1 until MAX_LEVEL) assertTrue("Rudolph" !in reindeerNames.take(reindeerCount(l)))
        assertTrue("Rudolph" in reindeerNames.take(reindeerCount(MAX_LEVEL)))
    }

    @Test fun everyLevelUnlocksSomethingNew() {
        for (l in 6..8) {
            assertTrue(Toy.entries.any { it.unlockLevel == l })
            assertTrue(Cookie.entries.any { it.unlockLevel == l })
            assertTrue(reindeerCount(l) > reindeerCount(l - 1))
        }
    }

    @Test fun deliveriesGetHarder() {
        for (l in 4..8) assertTrue(flightFor(l, false).cloudEvery <= flightFor(l - 1, false).cloudEvery)
        assertTrue(flightFor(8, false).foggy)
    }

    @Test fun jobsExistForEveryLevel() {
        for (l in 1..MAX_LEVEL) assertTrue(jobsFor(l).all { it.goal > 0 })
        assertTrue(jobsFor(6).any { it.kind == JobKind.Wrap })
        assertTrue(jobsFor(7).any { it.kind == JobKind.Tree })
    }

    @Test fun oldSaveStillLoads() {
        // A save from before the expansion: none of the new fields.
        val old = """{"name":"Lexi","hasElf":true,"money":4321,"day":7,"workdays":6,"toys":{"Teddy":2},"jobs":[{"kind":"Bake","goal":4,"done":1}]}"""
        val s = json.decodeFromString<Save>(old)
        assertEquals(4321, s.money)
        assertEquals(0, s.wrapped)
        assertTrue(s.pets.isEmpty())
        assertTrue(s.reindeerDecos.isEmpty())
        // and round-trips with the new fields
        val again = json.decodeFromString<Save>(json.encodeToString(s.copy(pets = listOf(Pet(1, PetKind.Penguin, "Pip")), decos = setOf(Deco.Scarf))))
        assertEquals("Pip", again.pets.single().name)
    }

    @Test fun adventHas24DoorsInAMixedUpGrid() {
        assertEquals(24, adventSurprises.size)
        assertEquals((1..24).toSet(), adventOrder.toSet())
        assertEquals(24, adventOrder.size)
    }

    @Test fun adventGiftsAreNeverMoney() {
        val before = Save()
        for (g in adventSurprises) assertEquals(before.money, g.give(before).money)
        for ((_, p) in stickerPrizes) assertEquals(before.money, p.give(before).money)
    }

    @Test fun stickersAreUniqueAndEarnedFromTheSave() {
        assertEquals(stickers.size, stickers.map { it.id }.toSet().size)
        val s = Save(pets = listOf(Pet(1, PetKind.Owl, "Hoot")), wrapped = 1).withStickers()
        assertTrue("pet:Owl" in s.stickers && "wrap" in s.stickers && "item:Tree" in s.stickers)
        assertTrue(stickerPrizes.all { it.first <= stickers.size })
    }

    @Test fun houseStartsWithTwoStoreysAndUpgradesCostMoney() {
        assertEquals(2, Save().floors)
        assertEquals(START_FLOORS, rooms.count { it.price == 0 })
        assertTrue(rooms.drop(START_FLOORS).all { it.price > 0 })
        // an old save (no floors / mainPet fields) still loads as a two-storey house
        val old = json.decodeFromString<Save>("""{"name":"Lexi","placed":[{"id":1,"item":"Bed","floor":1,"x":0.5}]}""")
        assertEquals(2, old.floors)
        assertEquals(0L, old.mainPet)
    }
}
