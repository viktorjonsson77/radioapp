package se.radioapp.app.data.channel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AssetChannelRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun parsesCanonicalCatalogAndLooksUpChannel() = runTest {
        val repository = AssetChannelRepository(context)
        val channels = repository.getChannels()

        assertEquals(36, channels.size)
        assertEquals(25, channels.count { it.category.name == "LOCAL_P4" })
        assertEquals("P4 Malmöhus", repository.getChannel("p4-malmo")?.name)
        assertEquals(207, repository.getChannel("p4-malmo")?.srChannelId)
        assertEquals("malmohus", repository.getChannel("p4-malmo")?.region?.slug)
        assertTrue(channels.all { it.streamUrl.startsWith("https://live1.sr.se/") })
        assertEquals(channels.size, channels.map { it.srChannelId }.distinct().size)
    }
}
