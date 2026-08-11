package se.radioapp.app.ui

import android.content.Context
import android.view.ContextThemeWrapper
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import coil3.compose.AsyncImage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import se.radioapp.app.R
import se.radioapp.app.cast.CastOptionsProvider
import se.radioapp.app.cast.CastReceiverMode
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
    var p4Expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
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
                    NowPlaying(castState, state, onTogglePlayback, onStop)
                    val receiverConfiguration = CastOptionsProvider.configuration
                    Text(
                        "Receiver: ${receiverConfiguration.receiverLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    if (receiverConfiguration.mode == CastReceiverMode.DEFAULT_MEDIA_RECEIVER) {
                        Text(
                            "TEMPORARY TEST MODE · Inte RadioApps Custom Receiver",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    receiverConfiguration.configurationError?.let { error ->
                        Text(
                            error,
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
                item { Spacer(Modifier.height(4.dp)); SectionTitle("Nationella kanaler") }
                items(state.nationalChannels, key = { "national-${it.id}" }) { channel ->
                    ChannelCard(channel, channel.id in state.favoriteIds, castState.currentChannel?.id == channel.id, onPlay, onToggleFavorite)
                }
                item {
                    Text(
                        if (p4Expanded) "P4 lokalt · Dölj" else "P4 lokalt · Visa ${state.p4Channels.size} kanaler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().clickable { p4Expanded = !p4Expanded }.padding(vertical = 8.dp),
                    )
                }
                if (p4Expanded) {
                    items(state.p4Channels, key = { "p4-${it.id}" }) { channel ->
                        ChannelCard(channel, channel.id in state.favoriteIds, castState.currentChannel?.id == channel.id, onPlay, onToggleFavorite)
                    }
                }
                item { Spacer(Modifier.height(4.dp)); SectionTitle("Övriga kanaler") }
                items(state.otherChannels, key = { "other-${it.id}" }) { channel ->
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
            val buttonContext = mediaRouteButtonContext(context)
            MediaRouteButton(buttonContext).also {
                CastButtonFactory.setUpMediaRouteButton(buttonContext, it)
            }
        },
    )
}

internal fun mediaRouteButtonContext(context: Context): Context =
    ContextThemeWrapper(context, R.style.Theme_RadioApp_MediaRouteButton)

@Composable
private fun NowPlaying(state: CastUiState, uiState: RadioUiState, onTogglePlayback: () -> Unit, onStop: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            val channel = state.currentChannel
            val program = uiState.nowPlaying?.takeIf { it.channelId == channel?.id }
            if (channel != null) {
                AsyncImage(
                    model = program?.imageUrl ?: channel.imageUrl,
                    contentDescription = program?.programName ?: channel.name,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    fallback = painterResource(R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(Color.White),
                )
            }
            Column(Modifier.weight(1f).padding(start = if (channel == null) 0.dp else 16.dp)) {
                Text(
                    channel?.name ?: "Ingen kanal spelas",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (channel != null) {
                    Text(program?.programName ?: "Sveriges Radio", color = Color.White.copy(alpha = 0.92f))
                    program?.let { metadata ->
                        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                        metadata.programDescription?.let { description ->
                            Text(
                                description,
                                color = Color.White.copy(alpha = 0.82f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                            )
                        }
                        if (metadata.startsAt != null && metadata.endsAt != null) {
                            Text("${formatter.format(metadata.startsAt)}–${formatter.format(metadata.endsAt)}", color = Color.White.copy(alpha = 0.76f))
                        }
                        metadata.nextProgram?.takeIf { it.startsAt != null }?.let { next ->
                            Text("Nästa ${formatter.format(next.startsAt)}: ${next.name}", color = Color.White.copy(alpha = 0.76f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    if (state.connected) "Ansluten till ${state.receiverName ?: "Cast-enhet"}" else "Inte ansluten",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall,
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
            AsyncImage(
                model = channel.imageUrl,
                contentDescription = channel.name,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground),
                fallback = painterResource(R.drawable.ic_launcher_foreground),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary),
            )
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
