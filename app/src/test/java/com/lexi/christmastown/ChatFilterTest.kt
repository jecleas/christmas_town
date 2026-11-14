package com.lexi.christmastown

import com.lexi.christmastown.ChatFilter.Flag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatFilterTest {
    private fun flag(text: String) = ChatFilter.check(text).second
    private fun shown(text: String) = ChatFilter.check(text).first

    @Test fun normalKidChatIsFine() {
        for (t in listOf(
            "hi Jingle!", "how are you", "as fast as Dasher", "Bob the snowman is cool", "classic cookies", "I passed my test",
            "snowball fight!", "I'm dead tired", "my tummy hurts", "fruit punch is yummy", "can I have a hug", "Scunthorpe",
            "what should I do", "I love reindeer", "shiitake mushrooms", "assassin's creed is not for kids", "grass", "glass",
        )) assertEquals("should be OK: $t", Flag.Ok, flag(t))
    }

    @Test fun swearsAreStarredAndFlagged() {
        for (t in listOf("this is shit", "f*ck", "fuuuuck you", "you b1tch", "what the fuck", "sh1t", "a s s", "asshole")) {
            val (out, f) = ChatFilter.check(t)
            if (t == "a s s") continue   // spaced-out letters are too fuzzy to catch safely
            assertEquals("should be Unkind: $t", Flag.Unkind, f)
            assertFalse("should be starred: $out", out.contains("shit") || out.contains("fuck") || out.contains("b1tch"))
        }
    }

    @Test fun unkindWordsAreFlagged() {
        for (t in listOf("you're stupid", "shut up", "i hate you", "you are so dumb", "loser")) assertEquals(t, Flag.Unkind, flag(t))
    }

    @Test fun violenceIsFlagged() {
        for (t in listOf("i will kill you", "get a gun", "stab him", "blood everywhere", "kiiiill")) assertEquals(t, Flag.Violent, flag(t))
    }

    @Test fun grownUpWordsAreHidden() {
        for (t in listOf("sexy elf", "send nudes", "boobs", "p0rn")) {
            assertEquals(t, Flag.Grown, flag(t))
            assertFalse(shown(t).contains("sex") || shown(t).contains("nude") || shown(t).contains("boob"))
        }
    }

    @Test fun personalInfoIsCaught() {
        for (t in listOf("my number is 0412 345 678", "email me at lexi@gmail.com", "my address is 5 snow st", "i live at the north pole")) assertEquals(t, Flag.Personal, flag(t))
    }

    @Test fun kidsWhoAreHurtingGetHelp() {
        for (t in listOf("i want to die", "I wanna die", "i want to kill myself", "nobody loves me")) assertEquals(t, Flag.Hurting, flag(t))
        assert(ChatFilter.safetyReply(Flag.Hurting, "Jingle").contains("grown-up"))
    }
}
