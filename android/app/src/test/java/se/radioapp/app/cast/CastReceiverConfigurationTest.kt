package se.radioapp.app.cast

import com.google.android.gms.cast.CastMediaControlIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CastReceiverConfigurationTest {
    @Test fun defaultsToExplicitCustomModeSelection() {
        val configuration = CastReceiverConfiguration.from("CUSTOM", "A1B2C3D4")

        assertEquals(CastReceiverMode.CUSTOM, configuration.mode)
        assertEquals("A1B2C3D4", configuration.receiverApplicationId("default-id"))
        assertEquals("RadioApp Custom Receiver", configuration.receiverLabel)
        assertTrue(configuration.isPlaybackConfigured)
        assertNull(configuration.configurationError)
    }

    @Test fun defaultModeUsesGoogleSdkConstantWithoutCustomApplicationId() {
        val configuration = CastReceiverConfiguration.from("DEFAULT", null)

        assertEquals(CastReceiverMode.DEFAULT_MEDIA_RECEIVER, configuration.mode)
        assertEquals(
            CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID,
            configuration.receiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID),
        )
        assertEquals("Google Default Media Receiver", configuration.receiverLabel)
        assertTrue(configuration.isPlaybackConfigured)
        assertNull(configuration.configurationError)
    }

    @Test fun acceptsDescriptiveDefaultModeName() {
        assertEquals(
            CastReceiverMode.DEFAULT_MEDIA_RECEIVER,
            CastReceiverConfiguration.from("DEFAULT_MEDIA_RECEIVER", "").mode,
        )
    }

    @Test fun missingCustomApplicationIdIsSafeButPlaybackRemainsBlocked() {
        val configuration = CastReceiverConfiguration.from("CUSTOM", "REPLACE_ME")

        assertEquals(
            CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID,
            configuration.receiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID),
        )
        assertFalse(configuration.isPlaybackConfigured)
        assertEquals("Custom receiver not configured", configuration.configurationError)
    }

    @Test fun unknownModeDoesNotSilentlyEnableDefaultReceiverPlayback() {
        val configuration = CastReceiverConfiguration.from("TYPO", null)

        assertEquals(CastReceiverMode.CUSTOM, configuration.mode)
        assertFalse(configuration.isPlaybackConfigured)
        assertTrue(configuration.configurationError?.contains("Unknown CAST_RECEIVER_MODE") == true)
    }
}
