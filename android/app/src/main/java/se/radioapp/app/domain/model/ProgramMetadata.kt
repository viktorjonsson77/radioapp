package se.radioapp.app.domain.model

import java.time.Instant

data class ProgramMetadata(
    val channelId: String,
    val title: String,
    val imageUrl: String? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val nextTitle: String? = null,
)
