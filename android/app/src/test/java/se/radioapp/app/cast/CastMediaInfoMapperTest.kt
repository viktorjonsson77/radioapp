package se.radioapp.app.cast

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.StreamFormat
import se.radioapp.app.domain.model.StreamQuality

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CastMediaInfoMapperTest {
    @Test
    fun mapsLiveAacChannelWithSrMetadata() {
        val media = CastMediaInfoMapper.map(
            Channel(
                id = "p3",
                srChannelId = 164,
                name = "P3",
                shortName = "P3",
                description = "Music",
                streamUrl = "https://live1.sr.se/p3-aac-128",
                streamQuality = StreamQuality.AAC_128,
                streamFormat = StreamFormat.AAC,
                category = ChannelCategory.NATIONAL,
                isLocal = false,
                isFavoriteCapable = true,
            ),
        )

        assertEquals(MediaInfo.STREAM_TYPE_LIVE, media.streamType)
        assertEquals("audio/aac", media.contentType)
        assertEquals("https://live1.sr.se/p3-aac-128", media.contentId)
        assertEquals("radioapp://channel/p3", media.entity)
        assertEquals("P3", media.metadata?.getString(MediaMetadata.KEY_TITLE))
        assertEquals("Sveriges Radio", media.metadata?.getString(MediaMetadata.KEY_ARTIST))
    }
}
