package se.radioapp.app.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.radioapp.app.domain.repository.FavoriteRepository
import se.radioapp.app.domain.repository.RadioSettingsRepository
import se.radioapp.app.data.settings.RadioSettings

private val Context.radioPreferences by preferencesDataStore("radio_preferences")

class DataStoreFavoriteRepository(private val context: Context) : FavoriteRepository, RadioSettingsRepository {
    override val favoriteIds: Flow<Set<String>> = context.radioPreferences.data.map { preferences ->
        preferences[FAVORITES] ?: DEFAULT_FAVORITES
    }

    override suspend fun setFavorite(channelId: String, favorite: Boolean) {
        context.radioPreferences.edit { preferences ->
            val current = preferences[FAVORITES] ?: DEFAULT_FAVORITES
            preferences[FAVORITES] = FavoriteLogic.update(current, channelId, favorite)
        }
    }

    override val defaultP4ChannelId: Flow<String> = context.radioPreferences.data.map { preferences ->
        preferences[DEFAULT_P4] ?: RadioSettings.DEFAULT_P4_CHANNEL_ID
    }

    override suspend fun setDefaultP4ChannelId(channelId: String) {
        context.radioPreferences.edit { preferences -> preferences[DEFAULT_P4] = channelId }
    }

    companion object {
        val DEFAULT_FAVORITES = setOf("p1", "p3", "p4-malmo")
        private val FAVORITES = stringSetPreferencesKey("favorite_channel_ids")
        private val DEFAULT_P4 = stringPreferencesKey("default_p4_channel_id")
    }
}

object FavoriteLogic {
    fun update(current: Set<String>, channelId: String, favorite: Boolean): Set<String> =
        if (favorite) current + channelId else current - channelId
}
