package se.radioapp.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface RadioSettingsRepository {
    val defaultP4ChannelId: Flow<String>
    suspend fun setDefaultP4ChannelId(channelId: String)
}
