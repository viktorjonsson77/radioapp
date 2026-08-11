package se.radioapp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.radioapp.app.cast.CastController
import se.radioapp.app.cast.CastUiState
import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.repository.ChannelRepository
import se.radioapp.app.domain.repository.FavoriteRepository

data class RadioUiState(
    val loading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val error: String? = null,
) {
    val favorites: List<Channel> get() = channels.filter { it.id in favoriteIds }
}

class RadioViewModel(
    private val channels: ChannelRepository,
    private val favorites: FavoriteRepository,
    private val metadata: SrMetadataProvider,
    private val castController: CastController,
) : ViewModel() {
    private val loadedChannels = MutableStateFlow<List<Channel>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val loadError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RadioUiState> = combine(
        loadedChannels,
        favorites.favoriteIds,
        loading,
        loadError,
    ) { channelList, favoriteIds, isLoading, error ->
        RadioUiState(isLoading, channelList, favoriteIds, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RadioUiState())

    val castState: StateFlow<CastUiState> = castController.state

    init {
        viewModelScope.launch {
            runCatching { channels.getChannels() }
                .onSuccess { loadedChannels.value = it }
                .onFailure { loadError.value = "Kanallistan kunde inte läsas: ${it.message}" }
            loading.value = false
        }
    }

    fun play(channel: Channel) {
        viewModelScope.launch {
            val program = metadata.nowPlaying(channel.id).getOrNull()
            castController.playChannel(channel, program)
        }
    }

    fun toggleFavorite(channel: Channel) {
        if (!channel.isFavoriteCapable) return
        viewModelScope.launch {
            favorites.setFavorite(channel.id, channel.id !in uiState.value.favoriteIds)
        }
    }

    fun togglePlayback() = castController.togglePlayback()

    fun stop() = castController.stop()
}
