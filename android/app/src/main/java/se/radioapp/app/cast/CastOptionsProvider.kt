package se.radioapp.app.cast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import se.radioapp.app.BuildConfig

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val receiverId = BuildConfig.CAST_RECEIVER_APP_ID.takeUnless { it == PLACEHOLDER || it.isBlank() }
            ?: CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
        return CastOptions.Builder()
            .setReceiverApplicationId(receiverId)
            .setEnableReconnectionService(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null

    companion object {
        const val PLACEHOLDER = "REPLACE_ME"
        val isCustomReceiverConfigured: Boolean
            get() = BuildConfig.CAST_RECEIVER_APP_ID.isNotBlank() &&
                BuildConfig.CAST_RECEIVER_APP_ID != PLACEHOLDER
    }
}
