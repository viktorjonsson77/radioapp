package se.radioapp.app.data.settings

import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory

data class RadioSettings(val defaultP4ChannelId: String = DEFAULT_P4_CHANNEL_ID) {
    companion object {
        const val DEFAULT_P4_CHANNEL_ID = "p4-malmo"

        fun resolveDefaultP4(channels: List<Channel>, preferredId: String): Channel? {
            val p4Channels = channels.filter { it.category == ChannelCategory.LOCAL_P4 }
            return p4Channels.firstOrNull { it.id == preferredId }
                ?: p4Channels.firstOrNull { it.id == DEFAULT_P4_CHANNEL_ID }
                ?: p4Channels.firstOrNull()
        }
    }
}
