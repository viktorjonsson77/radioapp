package se.radioapp.app.data.metadata

import se.radioapp.app.domain.metadata.SrMetadataProvider
import se.radioapp.app.domain.model.ProgramMetadata

/** Metadata failure or absence is intentionally independent of stream playback. */
class FixtureSrMetadataProvider : SrMetadataProvider {
    override suspend fun nowPlaying(channelId: String): Result<ProgramMetadata?> = Result.success(null)
}
