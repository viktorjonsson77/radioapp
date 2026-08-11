package se.radioapp.app.cast

import android.content.Context
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import se.radioapp.app.domain.model.Channel
import se.radioapp.app.domain.model.ProgramMetadata

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

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) = connected(session)
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) = connected(session)
        override fun onSessionStarting(session: CastSession) = Unit
        override fun onSessionStartFailed(session: CastSession, error: Int) = failed("Receiver unavailable ($error)")
        override fun onSessionEnding(session: CastSession) = Unit
        override fun onSessionEnded(session: CastSession, error: Int) {
            mutableState.value = CastUiState(message = if (error == 0) null else "Cast-sessionen avslutades ($error)")
        }
        override fun onSessionResuming(session: CastSession, sessionId: String) = Unit
        override fun onSessionResumeFailed(session: CastSession, error: Int) = failed("Cast-sessionen kunde inte återanslutas ($error)")
        override fun onSessionSuspended(session: CastSession, reason: Int) = failed("Cast-sessionen tappades ($reason)")
    }

    init {
        sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
        sessionManager.currentCastSession?.takeIf { it.isConnected }?.let(::connected)
    }

    fun playChannel(channel: Channel, program: ProgramMetadata? = null) {
        if (!CastOptionsProvider.isCustomReceiverConfigured) {
            failed("Custom receiver not configured")
            return
        }
        val session = sessionManager.currentCastSession
        val client = session?.remoteMediaClient
        if (session?.isConnected != true || client == null) {
            failed("Ingen Cast-enhet är ansluten – använd Cast-knappen")
            return
        }

        val request = MediaLoadRequestData.Builder()
            .setMediaInfo(CastMediaInfoMapper.map(channel, program))
            .setAutoplay(true)
            .build()
        client.load(request).setResultCallback { result ->
            if (result.status.isSuccess) {
                mutableState.value = mutableState.value.copy(currentChannel = channel, isPlaying = true, message = null)
            } else {
                failed("Streamen kunde inte startas: ${result.status.statusMessage ?: result.status.statusCode}")
            }
        }
    }

    fun togglePlayback() {
        val client = sessionManager.currentCastSession?.remoteMediaClient
        if (client == null) {
            failed("Ingen Cast-enhet är ansluten")
            return
        }
        val shouldPlay = !client.isPlaying
        val result = if (shouldPlay) client.play() else client.pause()
        result.setResultCallback { status ->
            if (status.status.isSuccess) mutableState.value = mutableState.value.copy(isPlaying = shouldPlay, message = null)
            else failed("Uppspelningskommandot misslyckades")
        }
    }

    fun stop() {
        val client = sessionManager.currentCastSession?.remoteMediaClient
        if (client == null) {
            failed("Ingen Cast-enhet är ansluten")
            return
        }
        client.stop().setResultCallback { status ->
            if (status.status.isSuccess) {
                mutableState.value = mutableState.value.copy(currentChannel = null, isPlaying = false, message = null)
            } else failed("Stop-kommandot misslyckades")
        }
    }

    fun clearMessage() { mutableState.value = mutableState.value.copy(message = null) }

    private fun connected(session: CastSession) {
        mutableState.value = mutableState.value.copy(
            connected = true,
            receiverName = session.castDevice?.friendlyName,
            message = null,
        )
    }

    private fun failed(message: String) {
        mutableState.value = mutableState.value.copy(message = message)
    }
}
