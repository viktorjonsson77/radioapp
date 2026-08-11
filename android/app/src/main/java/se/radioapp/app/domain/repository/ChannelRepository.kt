package se.radioapp.app.domain.repository

import se.radioapp.app.domain.model.Channel

interface ChannelRepository {
    suspend fun getChannels(): List<Channel>
    suspend fun getChannel(id: String): Channel?
}
