package se.radioapp.app.data.channel

import android.content.Context
import kotlinx.serialization.json.Json
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCatalog
import se.radioapp.app.domain.repository.ChannelRepository

class AssetChannelRepository(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = false },
) : ChannelRepository {
    @Volatile private var cache: List<Channel>? = null

    override suspend fun getChannels(): List<Channel> = cache ?: synchronized(this) {
        cache ?: context.assets.open(CHANNEL_FILE).bufferedReader().use { reader ->
            json.decodeFromString<ChannelCatalog>(reader.readText()).also { catalog ->
                require(catalog.schemaVersion == 1) { "Unsupported channel schema ${catalog.schemaVersion}" }
                require(catalog.channels.map(Channel::id).distinct().size == catalog.channels.size) {
                    "Channel IDs must be unique"
                }
            }.channels.also { cache = it }
        }
    }

    override suspend fun getChannel(id: String): Channel? = getChannels().firstOrNull { it.id == id }

    companion object { const val CHANNEL_FILE = "channels.json" }
}
