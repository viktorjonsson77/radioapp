package se.radioapp.app

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityHostTest {
    @Test
    fun `main activity remains a FragmentActivity compatible Compose host`() {
        assertTrue(AppCompatActivity::class.java.isAssignableFrom(MainActivity::class.java))
        assertTrue(FragmentActivity::class.java.isAssignableFrom(MainActivity::class.java))
        assertTrue(ComponentActivity::class.java.isAssignableFrom(MainActivity::class.java))
    }
}
