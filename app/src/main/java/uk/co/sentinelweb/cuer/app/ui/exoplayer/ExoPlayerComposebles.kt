package uk.co.sentinelweb.cuer.app.ui.exoplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import httpLocalNetworkUrl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.remote.server.LocalRepository

object ExoPlayerComposebles: KoinComponent {
    private val localRepository: LocalRepository by inject()

    @Composable
    fun ExoPlayerUi(view: ExoActivity.MviViewImpl) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                //setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
            }
        }

        val state = view.model.collectAsState()

        LaunchedEffect(state.value.playlistItem) {
            state.value.playlistItem?.httpLocalNetworkUrl(localRepository)
                ?.also { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }
        }

        DisposableEffect(lifecycleOwner, exoPlayer) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                    Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = true
                    Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                exoPlayer.release()
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlayerSurface(
                player = exoPlayer,
                surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
