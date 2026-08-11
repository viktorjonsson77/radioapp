package se.radioapp.app.domain.metadata

import se.radioapp.app.domain.model.ProgramMetadata

interface SrMetadataProvider {
    suspend fun nowPlaying(channelId: String): Result<ProgramMetadata?>
}
