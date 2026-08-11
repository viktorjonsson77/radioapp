package se.radioapp.app.ui

import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.R as AppCompatR
import androidx.mediarouter.app.MediaRouteButton
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.radioapp.app.R

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaRouteButtonThemeTest {
    @Test
    fun `activity theme provides opaque AppCompat primary color`() {
        assertOpaquePrimaryColor(
            android.view.ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(),
                R.style.Theme_RadioApp,
            ),
        )
    }

    @Test
    fun `media route button theme provides opaque primary color`() {
        val context = mediaRouteButtonContext(ApplicationProvider.getApplicationContext())

        assertOpaquePrimaryColor(context)
    }

    private fun assertOpaquePrimaryColor(context: android.content.Context) {
        val value = TypedValue()

        assertTrue(context.theme.resolveAttribute(AppCompatR.attr.colorPrimary, value, true))
        val color = if (value.resourceId != 0) {
            context.getColor(value.resourceId)
        } else {
            value.data
        }

        assertEquals(255, Color.alpha(color))
    }

    @Test
    fun `media route button can be constructed with dedicated theme`() {
        val context = mediaRouteButtonContext(ApplicationProvider.getApplicationContext())

        MediaRouteButton(context)
    }
}
