package se.radioapp.app.cast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import se.radioapp.app.BuildConfig

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            .setReceiverApplicationId(
                configuration.receiverApplicationId(
                    CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID,
                ),
            )
            .setEnableReconnectionService(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null

    companion object {
        val configuration: CastReceiverConfiguration by lazy {
            CastReceiverConfiguration.from(
                rawMode = BuildConfig.CAST_RECEIVER_MODE,
                rawCustomReceiverApplicationId = BuildConfig.CAST_RECEIVER_APP_ID,
            )
        }
    }
}
