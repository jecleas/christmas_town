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

    @Test fun payIsAThousand() = assertEquals(1000, PAY)

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
}
