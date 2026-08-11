package se.radioapp.app.data.metadata

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.NextProgramMetadata
import se.radioapp.app.domain.model.NowPlayingMetadata

fun interface SrApiClient { suspend fun get(url: String): String }

class UrlConnectionSrApiClient(
    private val connectTimeoutMs: Int = 4_000,
    private val readTimeoutMs: Int = 4_000,
) : SrApiClient {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode in 200..299) { "SR API returned HTTP ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

class HttpSrMetadataProvider(
    private val client: SrApiClient = UrlConnectionSrApiClient(),
    private val clock: Clock = Clock.systemUTC(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : SrMetadataProvider {
    override suspend fun nowPlaying(channel: Channel): Result<NowPlayingMetadata?> = runCatching {
        val url = "$ENDPOINT?channelid=${channel.srChannelId}&format=json"
        SrMetadataParser.parse(json.parseToJsonElement(client.get(url)).jsonObject, channel.id, clock.instant())
    }.onFailure { Log.w(TAG, "SR metadata unavailable for ${channel.id}", it) }

    companion object {
        const val ENDPOINT = "https://api.sr.se/api/v2/scheduledepisodes/rightnow"
        private const val TAG = "SrMetadataProvider"
    }
}

object SrMetadataParser {
    private val microsoftDate = Regex("^/Date\\(([-0-9]+)(?:[+-][0-9]{4})?\\)/$")

    fun parse(root: JsonObject, channelId: String, updatedAt: Instant): NowPlayingMetadata? {
        val channel = root.objectOrNull("channel") ?: return null
        val current = channel.objectOrNull("currentscheduledepisode") ?: return null
        val title = current.stringOrNull("title")?.takeIf(String::isNotBlank)
            ?: current.objectOrNull("program")?.stringOrNull("name")?.takeIf(String::isNotBlank)
            ?: return null
        val program = current.objectOrNull("program")
        return NowPlayingMetadata(
            channelId = channelId,
            programId = program?.intOrNull("id"),
            programName = title,
            programDescription = current.stringOrNull("description")?.takeIf(String::isNotBlank),
            imageUrl = (current.stringOrNull("socialimage") ?: current.stringOrNull("imageurl"))
                ?.takeIf { it.startsWith("https://") },
            startsAt = parseInstant(current.stringOrNull("starttimeutc")),
            endsAt = parseInstant(current.stringOrNull("endtimeutc")),
            nextProgram = channel.objectOrNull("nextscheduledepisode")?.toNextProgram(),
            updatedAt = updatedAt,
        )
    }

    private fun JsonObject.toNextProgram(): NextProgramMetadata? {
        val program = objectOrNull("program")
        val name = stringOrNull("title")?.takeIf(String::isNotBlank)
            ?: program?.stringOrNull("name")?.takeIf(String::isNotBlank)
            ?: return null
        return NextProgramMetadata(
            programId = program?.intOrNull("id"),
            name = name,
            startsAt = parseInstant(stringOrNull("starttimeutc")),
            endsAt = parseInstant(stringOrNull("endtimeutc")),
        )
    }

    private fun parseInstant(value: String?): Instant? {
        if (value == null) return null
        microsoftDate.matchEntire(value)?.groupValues?.get(1)?.toLongOrNull()?.let {
            return runCatching { Instant.ofEpochMilli(it) }.getOrNull()
        }
        return runCatching { Instant.parse(value) }.getOrNull()
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? = get(key) as? JsonObject
    private fun JsonObject.stringOrNull(key: String): String? = get(key)?.jsonPrimitive?.content
    private fun JsonObject.intOrNull(key: String): Int? = get(key)?.jsonPrimitive?.intOrNull
}

class CachingSrMetadataProvider(
    private val delegate: SrMetadataProvider,
    private val clock: Clock = Clock.systemUTC(),
    private val minTtl: Duration = Duration.ofSeconds(30),
    private val maxTtl: Duration = Duration.ofMinutes(5),
    private val defaultTtl: Duration = Duration.ofMinutes(2),
) : SrMetadataProvider {
    private data class Entry(
        val value: NowPlayingMetadata?,
        val refreshAt: Instant,
        val staleUntil: Instant,
    )

    private val cache = mutableMapOf<String, Entry>()

    override suspend fun nowPlaying(channel: Channel): Result<NowPlayingMetadata?> {
        val now = clock.instant()
        val cached = synchronized(cache) { cache[channel.id] }
        if (cached != null && now.isBefore(cached.refreshAt)) return Result.success(cached.value)

        val result = delegate.nowPlaying(channel)
        if (result.isSuccess) {
            val value = result.getOrNull()
            val ttl = value?.endsAt?.let { Duration.between(now, it.minusSeconds(15)) }
                ?.coerceIn(minTtl, maxTtl) ?: defaultTtl
            val staleUntil = value?.endsAt?.plusSeconds(120)
                ?.coerceAtMost(now.plus(Duration.ofMinutes(30))) ?: now.plus(defaultTtl)
            synchronized(cache) { cache[channel.id] = Entry(value, now.plus(ttl), staleUntil) }
            return result
        }
        return if (cached != null && !now.isAfter(cached.staleUntil)) Result.success(cached.value) else result
    }

    private fun Duration.coerceIn(minimum: Duration, maximum: Duration): Duration =
        when { this < minimum -> minimum; this > maximum -> maximum; else -> this }
}
