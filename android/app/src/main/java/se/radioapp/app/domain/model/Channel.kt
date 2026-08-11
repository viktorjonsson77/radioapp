package se.radioapp.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelCatalog(
    val schemaVersion: Int,
    val channels: List<Channel>,
)

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
    val streamUrl: String,
    val streamQuality: StreamQuality,
    val streamFormat: StreamFormat,
    val imageUrl: String? = null,
    val category: ChannelCategory,
    val region: String? = null,
    val isLocal: Boolean,
    val isFavoriteCapable: Boolean,
)

@Serializable
enum class StreamQuality { AAC_32, AAC_128, AAC_320, MP3_96 }

@Serializable
enum class StreamFormat { AAC, MP3 }

@Serializable
enum class ChannelCategory { NATIONAL, LOCAL_P4 }
