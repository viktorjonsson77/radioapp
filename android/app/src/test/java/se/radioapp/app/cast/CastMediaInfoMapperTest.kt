package se.radioapp.app.cast

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.NowPlayingMetadata
import se.radioapp.app.domain.model.StreamFormat
import se.radioapp.app.domain.model.StreamQuality

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CastMediaInfoMapperTest {
    private val p1 = Channel(
        id = "p1",
        srChannelId = 132,
        name = "P1",
        shortName = "P1",
        description = "News",
        streamUrl = "https://live1.sr.se/p1-aac-128",
        streamQuality = StreamQuality.AAC_128,
        streamFormat = StreamFormat.AAC,
        imageUrl = "https://static-cdn.sr.se/images/132/p1.jpg",
        category = ChannelCategory.NATIONAL,
        isLocal = false,
        isFavoriteCapable = true,
    )

    @Test fun mapsStandardLiveP1MediaInfoWithoutCustomReceiverPayload() {
        val media = CastMediaInfoMapper.map(p1)

        assertEquals(MediaInfo.STREAM_TYPE_LIVE, media.streamType)
        assertEquals("audio/aac", media.contentType)
        assertEquals("https://live1.sr.se/p1-aac-128", media.contentId)
        assertEquals("radioapp://channel/p1", media.entity)
        assertNull(media.customData)
        assertEquals("P1", media.metadata?.getString(MediaMetadata.KEY_TITLE))
        assertEquals("Sveriges Radio", media.metadata?.getString(MediaMetadata.KEY_ARTIST))
        assertEquals("LIVE · Sveriges Radio", media.metadata?.getString(MediaMetadata.KEY_SUBTITLE))
        assertEquals(1, media.metadata?.images?.size)
    }

    @Test fun mapsCurrentProgramMetadataWhenAvailable() {
        val program = NowPlayingMetadata(
            channelId = "p1",
            programId = 123,
            programName = "P1 Morgon",
            programDescription = "Aktuellt program",
            imageUrl = "https://static-cdn.sr.se/images/program.jpg",
            startsAt = Instant.parse("2026-08-11T06:00:00Z"),
            endsAt = Instant.parse("2026-08-11T08:00:00Z"),
            nextProgram = null,
            updatedAt = Instant.parse("2026-08-11T06:30:00Z"),
        )

        val media = CastMediaInfoMapper.map(p1, program)

        assertEquals("P1 Morgon", media.metadata?.getString(MediaMetadata.KEY_TITLE))
        assertEquals("P1", media.metadata?.getString(MediaMetadata.KEY_ARTIST))
        assertEquals("https://static-cdn.sr.se/images/program.jpg", media.metadata?.images?.first()?.url?.toString())
    }

    @Test fun mapsPreparedOfficialMp3AlternativeContentType() {
        val media = CastMediaInfoMapper.map(
            p1.copy(
                streamUrl = "https://live1.sr.se/p1-mp3-96",
                streamQuality = StreamQuality.MP3_96,
                streamFormat = StreamFormat.MP3,
            ),
        )

        assertEquals("audio/mpeg", media.contentType)
    }
}
