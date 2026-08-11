package se.radioapp.app.domain.metadata

import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.NowPlayingMetadata

fun interface SrMetadataProvider {
    suspend fun nowPlaying(channel: Channel): Result<NowPlayingMetadata?>
}
