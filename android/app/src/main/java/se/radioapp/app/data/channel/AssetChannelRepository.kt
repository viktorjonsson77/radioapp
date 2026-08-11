package se.radioapp.app.data.channel

import android.content.Context
import kotlinx.serialization.json.Json
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCatalog
import se.radioapp.app.domain.model.StreamFormat
import se.radioapp.app.domain.model.StreamQuality
import se.radioapp.app.domain.repository.ChannelRepository

class AssetChannelRepository(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = false },
) : ChannelRepository {
    @Volatile private var cache: List<Channel>? = null

    override suspend fun getChannels(): List<Channel> = cache ?: synchronized(this) {
        cache ?: context.assets.open(CHANNEL_FILE).bufferedReader().use { reader ->
            json.decodeFromString<ChannelCatalog>(reader.readText()).also { catalog ->
                require(catalog.schemaVersion == 2) { "Unsupported channel schema ${catalog.schemaVersion}" }
                require(catalog.channels.map(Channel::id).distinct().size == catalog.channels.size) {
                    "Channel IDs must be unique"
                }
                require(catalog.channels.map(Channel::srChannelId).distinct().size == catalog.channels.size) {
                    "SR channel IDs must be unique"
                }
                require(catalog.channels.map(Channel::streamUrl).distinct().size == catalog.channels.size) {
                    "Stream URLs must be unique"
                }
                require(catalog.channels.all { it.name.isNotBlank() && it.streamUrl.startsWith("https://live1.sr.se/") }) {
                    "Channels must have names and official HTTPS streams"
                }
                require(catalog.channels.all { channel ->
                    if (channel.category.name == "LOCAL_P4") channel.region != null && channel.isLocal
                    else channel.region == null && !channel.isLocal
                }) { "P4 regional metadata is inconsistent" }
                require(catalog.channels.all { channel ->
                    if (channel.streamQuality == StreamQuality.MP3_96) channel.streamFormat == StreamFormat.MP3
                    else channel.streamFormat == StreamFormat.AAC
                }) { "Stream quality and format are inconsistent" }
            }.channels.also { cache = it }
        }
    }

    override suspend fun getChannel(id: String): Channel? = getChannels().firstOrNull { it.id == id }

    companion object { const val CHANNEL_FILE = "channels.json" }
}
