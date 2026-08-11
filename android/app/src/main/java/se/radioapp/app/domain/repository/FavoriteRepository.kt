package se.radioapp.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    val favoriteIds: Flow<Set<String>>
    suspend fun setFavorite(channelId: String, favorite: Boolean)
}
