package se.radioapp.app.domain.model

import java.time.Instant

data class NextProgramMetadata(
    val programId: Int?,
    val name: String,
    val startsAt: Instant?,
    val endsAt: Instant?,
)

data class NowPlayingMetadata(
    val channelId: String,
    val programId: Int?,
    val programName: String,
    val programDescription: String?,
    val imageUrl: String?,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val nextProgram: NextProgramMetadata?,
    val updatedAt: Instant,
)
