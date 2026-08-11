package se.radioapp.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import se.radioapp.app.cast.CastController
import se.radioapp.app.data.channel.AssetChannelRepository
import se.radioapp.app.data.favorites.DataStoreFavoriteRepository
import se.radioapp.app.data.metadata.CachingSrMetadataProvider
import se.radioapp.app.data.metadata.HttpSrMetadataProvider
import se.radioapp.app.ui.RadioScreen
import se.radioapp.app.ui.RadioViewModel
import se.radioapp.app.ui.theme.RadioTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContext = applicationContext
        val preferences = DataStoreFavoriteRepository(appContext)
        val factory = viewModelFactory {
            initializer {
                RadioViewModel(
                    channels = AssetChannelRepository(appContext),
                    favorites = preferences,
                    settings = preferences,
                    metadata = CachingSrMetadataProvider(HttpSrMetadataProvider()),
                    castController = CastController(appContext),
                )
            }
        }
        setContent {
            val model: RadioViewModel = viewModel(factory = factory)
            val state by model.uiState.collectAsStateWithLifecycle()
            val castState by model.castState.collectAsStateWithLifecycle()
            RadioTheme {
                RadioScreen(
                    state = state,
                    castState = castState,
                    onPlay = model::play,
                    onToggleFavorite = model::toggleFavorite,
                    onSetDefaultP4 = model::setDefaultP4,
                    onTogglePlayback = model::togglePlayback,
                    onStop = model::stop,
                )
            }
        }
    }
}
