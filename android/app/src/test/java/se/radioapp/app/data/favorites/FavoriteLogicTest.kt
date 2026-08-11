package se.radioapp.app.data.favorites

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteLogicTest {
    @Test
    fun addsAndRemovesWithoutAffectingOtherFavorites() {
        val initial = setOf("p1", "p3")
        assertEquals(setOf("p1", "p2", "p3"), FavoriteLogic.update(initial, "p2", true))
        assertEquals(setOf("p1"), FavoriteLogic.update(initial, "p3", false))
    }
}
