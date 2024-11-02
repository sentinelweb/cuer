package uk.co.sentinelweb.cuer.app.ui.exoplayer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme

class ExoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CuerSharedTheme {
                ExoPlayerScreen()
            }
        }
    }
}

@Composable
fun ExoPlayerScreen() {
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val exoPlayer = ExoPlayer.Builder(context).build().apply {
            val mediaItem =
                MediaItem.fromUri(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
        player = exoPlayer
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (player != null) {
            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        this.player = player
                        this.useController = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )
            PlayerControls(player!!)
        }
//        val title = "Video Title" // Replace with actual title if available
//        Text(
//            text = title,
//            color = Color.White,
//            modifier = Modifier
//                .padding(16.dp)
//                .height(20.dp)
//                .background(Color.Transparent)
//        )
    }
}

@Composable
fun BoxScope.PlayerControls(player: ExoPlayer) {
    var playWhenReady by remember { mutableStateOf(player.playWhenReady) }
    var playbackPosition by remember { mutableStateOf(player.currentPosition) }
    var bufferedPosition by remember { mutableStateOf(player.bufferedPosition) }
    var duration by remember { mutableStateOf(player.duration) }

    LaunchedEffect(player) {
        while (true) {
            playbackPosition = player.currentPosition
            bufferedPosition = player.bufferedPosition
            duration = player.duration
            playWhenReady = player.isPlaying
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
//            .background(Color.DarkGray)
            .align(BottomCenter)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(16.dp)
        ) {
            Button(
                onClick = { player.seekBack() },
                modifier = Modifier.height(40.dp)
            ) { Text("<< 10s") }

            Button(
                onClick = {
                    playWhenReady = !playWhenReady
                    player.playWhenReady = playWhenReady
                },
                modifier = Modifier.height(40.dp)
            ) { Text(if (playWhenReady) "Pause" else "Play") }

            Button(onClick = { player.seekForward() }) {
                Text("10s >>")
            }

            Button(
                onClick = {
                    player.stop()
                    player.seekTo(0)
                    playWhenReady = false
                },
                modifier = Modifier.height(40.dp)
            ) { Text("Stop") }
        }

        Slider(
            value = playbackPosition.toFloat(),
            onValueChange = { value ->
                player.seekTo(value.toLong())
                playbackPosition = value.toLong()
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                inactiveTrackColor = Color.Gray,
                activeTrackColor = Color.White
            )
        )

        Text(
            text = "${formatTime(playbackPosition)} / ${formatTime(duration)}",
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
