package se.radioapp.app.cast

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ProgramMetadata
import se.radioapp.app.domain.model.StreamFormat

object CastMediaInfoMapper {
    fun map(channel: Channel, program: ProgramMetadata? = null): MediaInfo {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, channel.name)
            putString(MediaMetadata.KEY_ARTIST, "Sveriges Radio")
            putString(MediaMetadata.KEY_SUBTITLE, program?.title ?: "Direktsänd radio")
            (program?.imageUrl ?: channel.imageUrl)?.let { addImage(WebImage(Uri.parse(it))) }
        }

        return MediaInfo.Builder(channel.streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType(channel.contentType())
            .setMetadata(metadata)
            .build()
    }

    private fun Channel.contentType(): String = when (streamFormat) {
        StreamFormat.AAC -> "audio/aac"
        StreamFormat.MP3 -> "audio/mpeg"
    }
}
