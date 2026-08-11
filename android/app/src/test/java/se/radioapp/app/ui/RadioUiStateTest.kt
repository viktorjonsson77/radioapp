package se.radioapp.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import se.radioapp.app.cast.CastUiState
import se.radioapp.app.data.favorites.DataStoreFavoriteRepository
import se.radioapp.app.data.settings.RadioSettings
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.ChannelRegion
import se.radioapp.app.domain.model.StreamFormat
import se.radioapp.app.domain.model.StreamQuality

class RadioUiStateTest {
    @Test fun developmentDefaultsRemainStable() {
        assertEquals(setOf("p1", "p3", "p4-malmo"), DataStoreFavoriteRepository.DEFAULT_FAVORITES)
        assertEquals("p4-malmo", RadioSettings.DEFAULT_P4_CHANNEL_ID)
    }

    @Test fun unknownFavoriteIdsDoNotCreateMissingFavoriteRows() {
        val state = RadioUiState(channels = emptyList(), favoriteIds = setOf("retired-channel"))
        assertEquals(emptyList<Any>(), state.favorites)
    }

    @Test fun selectedDefaultP4IsResolvedByStableChannelId() {
        val malmo = channel("p4-malmo", ChannelCategory.LOCAL_P4, "Malmöhus")
        val stockholm = channel("p4-stockholm", ChannelCategory.LOCAL_P4, "Stockholm")
        val state = RadioUiState(
            channels = listOf(malmo, stockholm),
            defaultP4ChannelId = stockholm.id,
        )

        assertEquals(stockholm, state.defaultP4Channel)
        assertEquals("Standard · Lokalt", channelSubtitle(stockholm, state.defaultP4ChannelId))
    }

    @Test fun missingStoredDefaultP4FallsBackGracefully() {
        val stockholm = channel("p4-stockholm", ChannelCategory.LOCAL_P4, "Stockholm")
        assertEquals(stockholm, RadioSettings.resolveDefaultP4(listOf(stockholm), "retired-p4"))
    }

    @Test fun nowPlayingUsesChannelFallbackWhenMetadataIsUnavailable() {
        val p3 = channel("p3", ChannelCategory.NATIONAL)
        val presentation = nowPlayingText(p3, metadata = null)

        assertEquals("P3", presentation.channelName)
        assertEquals("Sveriges Radio", presentation.programName)
        assertTrue(presentation.isLive)
        assertNull(presentation.description)
    }

    @Test fun idleAndSelectedChannelStatesAreExplicit() {
        val p1 = channel("p1", ChannelCategory.NATIONAL)

        assertFalse(nowPlayingText(null, null).isLive)
        assertEquals("Ingen kanal spelas", nowPlayingText(null, null).channelName)
        assertTrue(isSelectedChannel(p1, CastUiState(currentChannel = p1)))
        assertFalse(isSelectedChannel(p1, CastUiState()))
    }

    @Test fun castDestinationOnlyAppearsForConnectedSession() {
        assertNull(castDestinationText(CastUiState()))
        assertEquals(
            "Spelar på Nest Hub vardagsrum",
            castDestinationText(CastUiState(connected = true, receiverName = "Nest Hub vardagsrum")),
        )
        assertEquals("Spelar på Cast-enhet", castDestinationText(CastUiState(connected = true)))
    }

    @Test fun channelPlaybackActionForwardsTheOriginalChannelAndStream() {
        val p1 = channel("p1", ChannelCategory.NATIONAL)
        var received: Channel? = null

        dispatchChannelPlayback(p1) { received = it }

        assertTrue(received === p1)
        assertEquals(p1.streamUrl, received?.streamUrl)
    }

    private fun channel(
        id: String,
        category: ChannelCategory,
        regionName: String? = null,
    ) = Channel(
        id = id,
        srChannelId = id.hashCode(),
        name = id.split('-').joinToString(" ") { it.replaceFirstChar(Char::uppercase) },
        shortName = id,
        description = "Sveriges Radio",
        streamUrl = "https://example.test/$id.aac",
        streamQuality = StreamQuality.AAC_128,
        streamFormat = StreamFormat.AAC,
        imageUrl = null,
        category = category,
        region = regionName?.let { ChannelRegion(it, it.lowercase()) },
        isLocal = category == ChannelCategory.LOCAL_P4,
        isFavoriteCapable = true,
    )
}
