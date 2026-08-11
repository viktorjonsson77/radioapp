package se.radioapp.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import se.radioapp.app.data.favorites.DataStoreFavoriteRepository
import se.radioapp.app.data.settings.RadioSettings

class RadioUiStateTest {
    @Test fun developmentDefaultsRemainStable() {
        assertEquals(setOf("p1", "p3", "p4-malmo"), DataStoreFavoriteRepository.DEFAULT_FAVORITES)
        assertEquals("p4-malmo", RadioSettings.DEFAULT_P4_CHANNEL_ID)
    }

    @Test fun unknownFavoriteIdsDoNotCreateMissingFavoriteRows() {
        val state = RadioUiState(channels = emptyList(), favoriteIds = setOf("retired-channel"))
        assertEquals(emptyList<Any>(), state.favorites)
    }
}
