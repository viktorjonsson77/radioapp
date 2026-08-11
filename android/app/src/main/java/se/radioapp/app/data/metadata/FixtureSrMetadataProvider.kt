package se.radioapp.app.data.metadata

import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.NowPlayingMetadata

/** Metadata failure or absence is intentionally independent of stream playback. */
class FixtureSrMetadataProvider : SrMetadataProvider {
    override suspend fun nowPlaying(channel: Channel): Result<NowPlayingMetadata?> = Result.success(null)
}
