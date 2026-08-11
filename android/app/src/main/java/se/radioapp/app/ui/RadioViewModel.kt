package se.radioapp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.radioapp.app.cast.CastController
import se.radioapp.app.cast.CastUiState
import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ChannelCategory
import se.radioapp.app.domain.model.NowPlayingMetadata
import se.radioapp.app.domain.repository.ChannelRepository
import se.radioapp.app.domain.repository.FavoriteRepository

data class RadioUiState(
    val loading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val nowPlaying: NowPlayingMetadata? = null,
    val error: String? = null,
) {
    val favorites: List<Channel> get() = channels.filter { it.id in favoriteIds }
    val nationalChannels: List<Channel> get() = channels.filter { it.category == ChannelCategory.NATIONAL }
    val p4Channels: List<Channel> get() = channels.filter { it.category == ChannelCategory.LOCAL_P4 }
    val otherChannels: List<Channel> get() = channels.filter { it.category != ChannelCategory.NATIONAL && it.category != ChannelCategory.LOCAL_P4 }
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
    private val nowPlaying = MutableStateFlow<NowPlayingMetadata?>(null)
    private var metadataJob: Job? = null

    val uiState: StateFlow<RadioUiState> = combine(
        loadedChannels,
        favorites.favoriteIds,
        loading,
        loadError,
    ) { channelList, favoriteIds, isLoading, error ->
        RadioUiState(
            loading = isLoading,
            channels = channelList,
            favoriteIds = favoriteIds.intersect(channelList.mapTo(mutableSetOf()) { it.id }),
            error = error,
        )
    }.combine(nowPlaying) { state, program -> state.copy(nowPlaying = program) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RadioUiState())

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
        castController.playChannel(channel, nowPlaying.value?.takeIf { it.channelId == channel.id })
        metadataJob?.cancel()
        nowPlaying.value = null
        metadataJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                val program = metadata.nowPlaying(channel).getOrNull()
                val currentId = castController.state.value.currentChannel?.id
                if (currentId == null || currentId == channel.id) nowPlaying.value = program else break
                val nowMillis = System.currentTimeMillis()
                val untilEnd = program?.endsAt?.toEpochMilli()?.minus(nowMillis)?.minus(15_000)
                delay((untilEnd ?: 120_000).coerceIn(30_000, 300_000))
            }
        }
    }

    fun toggleFavorite(channel: Channel) {
        if (!channel.isFavoriteCapable) return
        viewModelScope.launch {
            favorites.setFavorite(channel.id, channel.id !in uiState.value.favoriteIds)
        }
    }

    fun togglePlayback() = castController.togglePlayback()

    fun stop() {
        metadataJob?.cancel()
        nowPlaying.value = null
        castController.stop()
    }
}
