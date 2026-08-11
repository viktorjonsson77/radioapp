package se.radioapp.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import se.radioapp.app.cast.CastOptionsProvider
import se.radioapp.app.cast.CastUiState
import se.radioapp.app.domain.model.Channel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    state: RadioUiState,
    castState: CastUiState,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onTogglePlayback: () -> Unit,
    onStop: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(castState.message) {
        castState.message?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("RadioApp", fontWeight = FontWeight.Bold) },
                actions = { CastButton() },
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionTitle("Spelas nu")
                    NowPlaying(castState, onTogglePlayback, onStop)
                    if (!CastOptionsProvider.isCustomReceiverConfigured) {
                        Text(
                            "Custom receiver not configured",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                item { Spacer(Modifier.height(4.dp)); SectionTitle("Favoriter") }
                if (state.favorites.isEmpty()) {
                    item { Text("Inga favoriter ännu", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(state.favorites, key = { "favorite-${it.id}" }) { channel ->
                        ChannelCard(channel, true, castState.currentChannel?.id == channel.id, onPlay, onToggleFavorite)
                    }
                }
                item { Spacer(Modifier.height(4.dp)); SectionTitle("Kanaler") }
                items(state.channels, key = { it.id }) { channel ->
                    ChannelCard(channel, channel.id in state.favoriteIds, castState.currentChannel?.id == channel.id, onPlay, onToggleFavorite)
                }
                item {
                    Text(
                        "Standard P4-kanal: P4 Malmöhus",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CastButton() {
    AndroidView(
        modifier = Modifier.size(48.dp).padding(8.dp),
        factory = { context ->
            MediaRouteButton(context).also { CastButtonFactory.setUpMediaRouteButton(context, it) }
        },
    )
}

@Composable
private fun NowPlaying(state: CastUiState, onTogglePlayback: () -> Unit, onStop: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                state.currentChannel?.name ?: "Ingen kanal spelas",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (state.connected) "Ansluten till ${state.receiverName ?: "Cast-enhet"}" else "Inte ansluten",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.currentChannel != null) {
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePlayback) {
                        Icon(
                            if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (state.isPlaying) "Pausa" else "Spela",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, "Stoppa", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    favorite: Boolean,
    playing: Boolean,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onPlay(channel) },
        colors = CardDefaults.cardColors(
            containerColor = if (playing) MaterialTheme.colorScheme.surfaceVariant else Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(channel.shortName.take(4), color = Color.White, fontWeight = FontWeight.Black)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(channel.name, fontWeight = FontWeight.Bold)
                Text(
                    if (playing) "LIVE · Spelas nu" else channel.description,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onToggleFavorite(channel) }) {
                Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorit")
            }
            Icon(Icons.Default.PlayArrow, "Spela ${channel.name}", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}
