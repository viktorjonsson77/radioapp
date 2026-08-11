package se.radioapp.app.ui

import se.radioapp.app.cast.CastUiState
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.NowPlayingMetadata

data class NowPlayingText(
    val channelName: String,
    val programName: String?,
    val description: String?,
    val isLive: Boolean,
)

fun nowPlayingText(channel: Channel?, metadata: NowPlayingMetadata?): NowPlayingText {
    val current = metadata?.takeIf { it.channelId == channel?.id }
    return NowPlayingText(
        channelName = channel?.name ?: "Ingen kanal spelas",
        programName = channel?.let { current?.programName ?: "Sveriges Radio" },
        description = current?.programDescription,
        isLive = channel != null,
    )
}

fun castDestinationText(state: CastUiState): String? =
    if (state.connected) "Spelar på ${state.receiverName ?: "Cast-enhet"}" else null

fun isSelectedChannel(channel: Channel, state: CastUiState): Boolean =
    state.currentChannel?.id == channel.id

internal fun dispatchChannelPlayback(channel: Channel, onPlay: (Channel) -> Unit) {
    onPlay(channel)
}

fun channelSubtitle(channel: Channel, defaultP4ChannelId: String): String = when {
    channel.category == ChannelCategory.LOCAL_P4 && channel.id == defaultP4ChannelId -> "Standard · Lokalt"
    channel.category == ChannelCategory.LOCAL_P4 -> channel.region?.name?.let { "Lokalt · $it" } ?: "Lokalt"
    else -> "Sveriges Radio"
}
