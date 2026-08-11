package se.radioapp.app.cast

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.NowPlayingMetadata

data class CastUiState(
    val connected: Boolean = false,
    val receiverName: String? = null,
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val message: String? = null,
)

class CastController(context: Context) {
    private val castContext = CastContext.getSharedInstance(context.applicationContext)
    private val sessionManager = castContext.sessionManager
    private val mutableState = MutableStateFlow(CastUiState())
    val state: StateFlow<CastUiState> = mutableState.asStateFlow()
    private var observedMediaClient: RemoteMediaClient? = null

    private val mediaCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val client = observedMediaClient ?: return
            val status = client.mediaStatus
            Log.d(TAG, "media status state=${status?.playerState} idleReason=${status?.idleReason}")
            mutableState.value = mutableState.value.copy(isPlaying = client.isPlaying)
            if (status?.idleReason == MediaStatus.IDLE_REASON_ERROR) {
                Log.e(TAG, "playback error: receiver reported IDLE_REASON_ERROR")
            }
        }

        override fun onMediaError(mediaError: com.google.android.gms.cast.MediaError) {
            Log.e(TAG, "playback error: $mediaError")
        }
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            Log.i(TAG, "Cast session started")
            connected(session)
        }
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            Log.i(TAG, "Cast session resumed wasSuspended=$wasSuspended")
            connected(session)
        }
        override fun onSessionStarting(session: CastSession) { Log.i(TAG, "Cast session starting") }
        override fun onSessionStartFailed(session: CastSession, error: Int) {
            Log.e(TAG, "Cast session failed error=$error")
            failed("Receiver unavailable ($error)")
        }
        override fun onSessionEnding(session: CastSession) { Log.i(TAG, "Cast session disconnecting") }
        override fun onSessionEnded(session: CastSession, error: Int) {
            Log.i(TAG, "Cast session disconnected error=$error")
            stopObservingMedia()
            mutableState.value = CastUiState(message = if (error == 0) null else "Cast-sessionen avslutades ($error)")
        }
        override fun onSessionResuming(session: CastSession, sessionId: String) { Log.i(TAG, "Cast session resuming") }
        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            Log.e(TAG, "Cast session resume failed error=$error")
            failed("Cast-sessionen kunde inte återanslutas ($error)")
        }
        override fun onSessionSuspended(session: CastSession, reason: Int) {
            Log.w(TAG, "Cast session suspended reason=$reason")
            failed("Cast-sessionen tappades ($reason)")
        }
    }

    init {
        sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
        sessionManager.currentCastSession?.takeIf { it.isConnected }?.let(::connected)
    }

    fun playChannel(channel: Channel, program: NowPlayingMetadata? = null) {
        val configuration = CastOptionsProvider.configuration
        val session = sessionManager.currentCastSession
        val client = session?.remoteMediaClient
        Log.i(
            TAG,
            "playChannel entered id=${channel.id} streamUrl=${channel.streamUrl} " +
                "receiverMode=${configuration.mode} configured=${configuration.isPlaybackConfigured} " +
                "sessionPresent=${session != null} sessionConnected=${session?.isConnected == true} " +
                "mediaClientPresent=${client != null}",
        )
        if (!configuration.isPlaybackConfigured) {
            val error = configuration.configurationError ?: "Cast receiver not configured"
            Log.e(TAG, "playChannel blocked before LOAD: $error")
            failed(error)
            return
        }
        if (session?.isConnected != true || client == null) {
            Log.w(TAG, "playChannel blocked before LOAD: no connected Cast session/media client")
            failed("Ingen Cast-enhet är ansluten – använd Cast-knappen")
            return
        }

        val mediaInfo = runCatching { CastMediaInfoMapper.map(channel, program) }
            .onFailure { Log.e(TAG, "MediaInfo creation failed id=${channel.id}", it) }
            .getOrElse {
                failed("Kanalen kunde inte förberedas för uppspelning")
                return
            }
        Log.i(
            TAG,
            "MediaInfo created id=${channel.id} contentId=${mediaInfo.contentId} " +
                "contentType=${mediaInfo.contentType} streamType=${mediaInfo.streamType}",
        )
        val request = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build()
        Log.i(TAG, "LOAD requested id=${channel.id} receiverMode=${configuration.mode}")
        runCatching {
            client.load(request).setResultCallback { result ->
                if (result.status.isSuccess) {
                    Log.i(TAG, "LOAD result success id=${channel.id} status=${result.status.statusCode}")
                    mutableState.value = mutableState.value.copy(currentChannel = channel, isPlaying = true, message = null)
                } else {
                    Log.e(
                        TAG,
                        "LOAD result failure id=${channel.id} status=${result.status.statusCode} " +
                            "message=${result.status.statusMessage}",
                    )
                    failed("Streamen kunde inte startas: ${result.status.statusMessage ?: result.status.statusCode}")
                }
            }
        }.onFailure {
            Log.e(TAG, "LOAD request exception id=${channel.id}", it)
            failed("Streamen kunde inte startas")
        }
    }

    fun togglePlayback() {
        val client = sessionManager.currentCastSession?.remoteMediaClient
        if (client == null) {
            failed("Ingen Cast-enhet är ansluten")
            return
        }
        val shouldPlay = !client.isPlaying
        Log.i(TAG, "playback command=${if (shouldPlay) "play" else "pause"}")
        val result = if (shouldPlay) client.play() else client.pause()
        result.setResultCallback { status ->
            if (status.status.isSuccess) mutableState.value = mutableState.value.copy(isPlaying = shouldPlay, message = null)
            else {
                Log.e(TAG, "playback command failed status=${status.status.statusCode}")
                failed("Uppspelningskommandot misslyckades")
            }
        }
    }

    fun stop() {
        val client = sessionManager.currentCastSession?.remoteMediaClient
        if (client == null) {
            failed("Ingen Cast-enhet är ansluten")
            return
        }
        Log.i(TAG, "playback command=stop")
        client.stop().setResultCallback { status ->
            if (status.status.isSuccess) {
                mutableState.value = mutableState.value.copy(currentChannel = null, isPlaying = false, message = null)
            } else {
                Log.e(TAG, "stop command failed status=${status.status.statusCode}")
                failed("Stop-kommandot misslyckades")
            }
        }
    }

    fun clearMessage() { mutableState.value = mutableState.value.copy(message = null) }

    private fun connected(session: CastSession) {
        observeMedia(session.remoteMediaClient)
        mutableState.value = mutableState.value.copy(
            connected = true,
            receiverName = session.castDevice?.friendlyName,
            message = null,
        )
    }

    private fun failed(message: String) {
        mutableState.value = mutableState.value.copy(message = message)
    }

    private fun observeMedia(client: RemoteMediaClient?) {
        if (observedMediaClient === client) return
        stopObservingMedia()
        observedMediaClient = client
        client?.registerCallback(mediaCallback)
    }

    private fun stopObservingMedia() {
        observedMediaClient?.unregisterCallback(mediaCallback)
        observedMediaClient = null
    }

    companion object { private const val TAG = "RadioAppCast" }
}
