package se.radioapp.app.ui

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import coil3.compose.AsyncImage
import com.google.android.gms.cast.framework.CastButtonFactory
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import se.radioapp.app.BuildConfig
import se.radioapp.app.R
import se.radioapp.app.cast.CastOptionsProvider
import se.radioapp.app.cast.CastReceiverMode
import se.radioapp.app.cast.CastUiState
import se.radioapp.app.domain.model.Channel

private val RadioShape = RoundedCornerShape(24.dp)
private val ItemShape = RoundedCornerShape(18.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    state: RadioUiState,
    castState: CastUiState,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onSetDefaultP4: (Channel) -> Unit,
    onTogglePlayback: () -> Unit,
    onStop: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    var p4Expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var p4PickerOpen by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(castState.message) {
        castState.message?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RadioApp", fontWeight = FontWeight.Black)
                        Text(
                            "Sveriges Radio live",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = { CastButton() },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle("Spelas nu")
                NowPlayingCard(castState, state, onTogglePlayback, onStop)
            }

            if (state.loading) {
                item { CatalogLoadingState() }
            } else if (state.error != null) {
                item { CatalogErrorState() }
            } else {
                item {
                    SectionTitle("Favoriter")
                    FavoriteRow(state, castState, onPlay, onToggleFavorite)
                }
                item {
                    SectionTitle("Nationella")
                    ChannelGroup(
                        state.nationalChannels,
                        state,
                        castState,
                        onPlay,
                        onToggleFavorite,
                    )
                }
                item {
                    P4Section(
                        state = state,
                        castState = castState,
                        expanded = p4Expanded,
                        onExpandedChange = { p4Expanded = !p4Expanded },
                        onOpenPicker = { p4PickerOpen = true },
                        onPlay = onPlay,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
                item {
                    SectionTitle("Övriga kanaler")
                    ChannelGroup(
                        state.otherChannels,
                        state,
                        castState,
                        onPlay,
                        onToggleFavorite,
                    )
                }
            }

            if (BuildConfig.DEBUG) {
                item { DeveloperInfo(state.error) }
            }
        }
    }

    if (p4PickerOpen) {
        DefaultP4Dialog(
            channels = state.p4Channels,
            selectedId = state.defaultP4Channel?.id,
            onSelect = {
                onSetDefaultP4(it)
                p4PickerOpen = false
            },
            onDismiss = { p4PickerOpen = false },
        )
    }
}

@Composable
private fun NowPlayingCard(
    castState: CastUiState,
    uiState: RadioUiState,
    onTogglePlayback: () -> Unit,
    onStop: () -> Unit,
) {
    val channel = castState.currentChannel
    val metadata = uiState.nowPlaying?.takeIf { it.channelId == channel?.id }
    val text = nowPlayingText(channel, metadata)
    val destination = castDestinationText(castState)
    val active = channel != null
    val container = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RadioShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (active) 4.dp else 0.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                imageUrl = metadata?.imageUrl ?: channel?.imageUrl,
                description = metadata?.programName ?: channel?.name ?: "RadioApp",
                modifier = Modifier.size(if (active) 116.dp else 88.dp),
                background = if (active) Color.White else MaterialTheme.colorScheme.surface,
            )
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text.channelName,
                    color = foreground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!active) {
                    Text(
                        "Välj en kanal för att börja lyssna",
                        color = foreground.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    Text(
                        text.programName.orEmpty(),
                        color = foreground.copy(alpha = 0.94f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    metadata?.programDescription?.let { description ->
                        Text(
                            description,
                            color = foreground.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    ProgramTiming(metadata, foreground)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        LiveBadge(foreground)
                        destination?.let {
                            Text(
                                it,
                                color = foreground.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onTogglePlayback, modifier = Modifier.size(48.dp)) {
                            Icon(
                                if (castState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (castState.isPlaying) "Pausa uppspelningen" else "Fortsätt uppspelningen",
                                tint = foreground,
                            )
                        }
                        IconButton(onClick = onStop, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Stop, "Stoppa uppspelningen", tint = foreground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramTiming(metadata: se.radioapp.app.domain.model.NowPlayingMetadata?, color: Color) {
    if (metadata == null) return
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()) }
    if (metadata.startsAt != null && metadata.endsAt != null) {
        Text(
            "${formatter.format(metadata.startsAt)}–${formatter.format(metadata.endsAt)}",
            color = color.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
    metadata.nextProgram?.takeIf { it.startsAt != null }?.let { next ->
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                Text("Nästa · ${formatter.format(next.startsAt)}", color = color.copy(alpha = 0.76f), style = MaterialTheme.typography.labelSmall)
                Text(next.name, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun LiveBadge(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text("LIVE", color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun FavoriteRow(
    state: RadioUiState,
    castState: CastUiState,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
) {
    if (state.favorites.isEmpty()) {
        EmptySection("Markera hjärtat på en kanal för att lägga till en favorit.")
        return
    }
    LazyRow(
        contentPadding = PaddingValues(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(state.favorites, key = { "favorite-${it.id}" }) { channel ->
            FavoriteCard(
                channel = channel,
                selected = isSelectedChannel(channel, castState),
                onPlay = { dispatchChannelPlayback(channel, onPlay) },
                onRemove = { onToggleFavorite(channel) },
            )
        }
    }
}

@Composable
private fun FavoriteCard(channel: Channel, selected: Boolean, onPlay: () -> Unit, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.width(154.dp).height(154.dp).semantics {
            role = Role.Button
            contentDescription = "Spela ${channel.name}${if (selected) ", spelas nu" else ""}"
        }.clickable(onClick = onPlay),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = ItemShape,
        tonalElevation = if (selected) 3.dp else 1.dp,
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            Column {
                Artwork(channel.imageUrl, channel.name, Modifier.size(72.dp), Color.White)
                Text(channel.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                Text(if (selected) "Spelas nu" else "Sveriges Radio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).size(48.dp),
            ) {
                Icon(Icons.Default.Favorite, "Ta bort ${channel.name} från favoriter", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ChannelGroup(
    channels: List<Channel>,
    state: RadioUiState,
    castState: CastUiState,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        channels.forEach { channel ->
            ChannelRow(
                channel = channel,
                subtitle = channelSubtitle(channel, state.defaultP4Channel?.id ?: state.defaultP4ChannelId),
                favorite = channel.id in state.favoriteIds,
                selected = isSelectedChannel(channel, castState),
                onPlay = { dispatchChannelPlayback(channel, onPlay) },
                onToggleFavorite = { onToggleFavorite(channel) },
            )
        }
    }
}

@Composable
private fun P4Section(
    state: RadioUiState,
    castState: CastUiState,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onOpenPicker: () -> Unit,
    onPlay: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
) {
    val defaultP4 = state.defaultP4Channel
    SectionTitle("P4 lokalt")
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RadioShape,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).animateContentSize(),
    ) {
        Column(Modifier.padding(14.dp)) {
            if (defaultP4 != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Artwork(defaultP4.imageUrl, defaultP4.name, Modifier.size(58.dp), Color.White)
                    Column(
                        Modifier.weight(1f).padding(horizontal = 12.dp).clickable {
                            dispatchChannelPlayback(defaultP4, onPlay)
                        },
                    ) {
                        Text(defaultP4.name, fontWeight = FontWeight.Bold)
                        Text("Din standardkanal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onOpenPicker) { Text("Byt") }
                }
            }
            TextButton(
                onClick = onExpandedChange,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(min = 48.dp),
            ) {
                Text(if (expanded) "Dölj lokala kanaler" else "Visa alla ${state.p4Channels.size} lokala kanaler")
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    state.p4Channels.forEach { channel ->
                        ChannelRow(
                            channel = channel,
                            subtitle = channelSubtitle(channel, defaultP4?.id ?: state.defaultP4ChannelId),
                            favorite = channel.id in state.favoriteIds,
                            selected = isSelectedChannel(channel, castState),
                            onPlay = { dispatchChannelPlayback(channel, onPlay) },
                            onToggleFavorite = { onToggleFavorite(channel) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    subtitle: String,
    favorite: Boolean,
    selected: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).semantics {
            role = Role.Button
            contentDescription = "Spela ${channel.name}${if (selected) ", spelas nu" else ""}"
        }.clickable(onClick = onPlay),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = ItemShape,
        tonalElevation = if (selected) 2.dp else 1.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(channel.imageUrl, channel.name, Modifier.size(52.dp), Color.White)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(channel.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (selected) "LIVE · Spelas nu" else subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconToggleButton(
                checked = favorite,
                onCheckedChange = { onToggleFavorite() },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    if (favorite) "Ta bort ${channel.name} från favoriter" else "Lägg till ${channel.name} som favorit",
                    tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
private fun DefaultP4Dialog(
    channels: List<Channel>,
    selectedId: String?,
    onSelect: (Channel) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj standardkanal", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 440.dp)) {
                items(channels, key = Channel::id) { channel ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { onSelect(channel) }.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            if (channel.id == selectedId) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(channel.name, fontWeight = FontWeight.SemiBold)
                            Text(channel.region?.name ?: "Lokal P4", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Klar") } },
    )
}

@Composable
private fun Artwork(imageUrl: String?, description: String, modifier: Modifier, background: Color) {
    AsyncImage(
        model = imageUrl,
        contentDescription = description,
        placeholder = painterResource(R.drawable.ic_launcher_foreground),
        error = painterResource(R.drawable.ic_launcher_foreground),
        fallback = painterResource(R.drawable.ic_launcher_foreground),
        contentScale = ContentScale.Fit,
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(background).padding(3.dp),
    )
}

@Composable
private fun CatalogLoadingState() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RadioShape, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Kanalerna laddas", fontWeight = FontWeight.Bold)
            Text("Du kan använda Cast-knappen under tiden.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            repeat(3) { index ->
                val placeholderAlpha = if (index % 2 == 0) 0.35f else 0.2f
                Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(44.dp).alpha(placeholderAlpha).clip(ItemShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
private fun CatalogErrorState() {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RadioShape, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Kanalerna kunde inte hämtas", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            Text("Kontrollera anslutningen och starta om appen.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun DeveloperInfo(catalogError: String?) {
    val configuration = CastOptionsProvider.configuration
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Utvecklarinfo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("Receiver: ${configuration.receiverLabel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (configuration.mode == CastReceiverMode.DEFAULT_MEDIA_RECEIVER) {
                Text(
                    "TEMPORARY TEST MODE · Inte RadioApps Custom Receiver",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            configuration.configurationError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
            catalogError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
}

@Composable
private fun CastButton() {
    AndroidView(
        modifier = Modifier.size(48.dp).padding(8.dp),
        factory = { context ->
            val buttonContext = mediaRouteButtonContext(context)
            MediaRouteButton(buttonContext).also {
                CastButtonFactory.setUpMediaRouteButton(buttonContext, it)
                it.contentDescription = "Välj Cast-enhet"
            }
        },
    )
}

internal fun mediaRouteButtonContext(context: Context): Context =
    ContextThemeWrapper(context, R.style.Theme_RadioApp_MediaRouteButton)
