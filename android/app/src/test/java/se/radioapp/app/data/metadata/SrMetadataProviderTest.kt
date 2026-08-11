package se.radioapp.app.data.metadata

import java.net.SocketTimeoutException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.NowPlayingMetadata
import se.radioapp.app.domain.model.StreamFormat
import se.radioapp.app.domain.model.StreamQuality

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SrMetadataProviderTest {
    private val channel = Channel(
        id = "p3", srChannelId = 164, name = "P3", shortName = "P3", description = "P3",
        streamUrl = "https://live1.sr.se/p3-aac-128", streamQuality = StreamQuality.AAC_128,
        streamFormat = StreamFormat.AAC, imageUrl = "https://static-cdn.sr.se/p3.jpg",
        category = ChannelCategory.NATIONAL, isLocal = false, isFavoriteCapable = true,
    )
    private val fixture = """
        {"channel":{"id":164,"name":"P3","currentscheduledepisode":{
        "title":"Morgonpasset i P3","description":"Aktuellt morgonprogram.",
        "starttimeutc":"/Date(1786428000000)/","endtimeutc":"/Date(1786435200000)/",
        "program":{"id":2024,"name":"Morgonpasset i P3"},
        "socialimage":"https://static-cdn.sr.se/images/2024/program.jpg"},
        "nextscheduledepisode":{"title":"P3 Nyheter","starttimeutc":"/Date(1786435200000)/",
        "endtimeutc":"/Date(1786435500000)/","program":{"id":1646,"name":"P3 Nyheter"}}}}
    """.trimIndent()

    @Test fun parsesCurrentNextTimesAndImage() {
        val result = SrMetadataParser.parse(Json.parseToJsonElement(fixture).jsonObject, "p3", Instant.EPOCH)!!
        assertEquals("Morgonpasset i P3", result.programName)
        assertEquals("Aktuellt morgonprogram.", result.programDescription)
        assertEquals(2024, result.programId)
        assertEquals("P3 Nyheter", result.nextProgram?.name)
        assertEquals("https://static-cdn.sr.se/images/2024/program.jpg", result.imageUrl)
    }

    @Test fun acceptsMissingOptionalFieldsAndRejectsMalformedShape() {
        val missing = fixture.replace(",\"description\":\"Aktuellt morgonprogram.\"", "")
            .replace("\"socialimage\":\"https://static-cdn.sr.se/images/2024/program.jpg\"", "\"unused\":\"\"")
        val result = SrMetadataParser.parse(Json.parseToJsonElement(missing).jsonObject, "p3", Instant.EPOCH)!!
        assertNull(result.programDescription)
        assertNull(result.imageUrl)
        assertNull(SrMetadataParser.parse(Json.parseToJsonElement("{}").jsonObject, "p3", Instant.EPOCH))
    }

    @Test fun networkAndTimeoutAreResultFailures() = runTest {
        val offline = HttpSrMetadataProvider(SrApiClient { throw IllegalStateException("offline") })
        assertTrue(offline.nowPlaying(channel).isFailure)
        val timeout = HttpSrMetadataProvider(SrApiClient { throw SocketTimeoutException("timeout") })
        assertTrue(timeout.nowPlaying(channel).isFailure)
    }

    @Test fun cachesAndFallsBackToStillRelevantStaleMetadata() = runTest {
        val clock = MutableClock(Instant.parse("2026-08-11T06:00:00Z"))
        var calls = 0
        val delegate = SrMetadataProvider {
            calls++
            if (calls == 1) Result.success(metadata(clock.instant())) else Result.failure(IllegalStateException("offline"))
        }
        val cache = CachingSrMetadataProvider(delegate, clock)
        assertEquals("Morgonpasset i P3", cache.nowPlaying(channel).getOrNull()?.programName)
        assertEquals("Morgonpasset i P3", cache.nowPlaying(channel).getOrNull()?.programName)
        assertEquals(1, calls)
        clock.current = Instant.parse("2026-08-11T06:06:00Z")
        assertEquals("Morgonpasset i P3", cache.nowPlaying(channel).getOrNull()?.programName)
        assertEquals(2, calls)
        clock.current = Instant.parse("2026-08-11T08:03:00Z")
        assertTrue(cache.nowPlaying(channel).isFailure)
    }

    private fun metadata(now: Instant) = NowPlayingMetadata(
        channelId = "p3", programId = 2024, programName = "Morgonpasset i P3",
        programDescription = null, imageUrl = null, startsAt = now,
        endsAt = Instant.parse("2026-08-11T08:00:00Z"), nextProgram = null, updatedAt = now,
    )
}

private class MutableClock(var current: Instant) : Clock() {
    override fun instant(): Instant = current
    override fun getZone(): ZoneId = ZoneId.of("UTC")
    override fun withZone(zone: ZoneId): Clock = this
}
