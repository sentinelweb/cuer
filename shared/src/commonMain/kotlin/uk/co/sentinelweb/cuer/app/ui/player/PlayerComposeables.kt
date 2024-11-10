package uk.co.sentinelweb.cuer.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.common.compose.colorTransparentBlack
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.shared.generated.resources.*

private val buttonSize = 48.dp
private val buttonPadding = 8.dp

object PlayerComposeables : KoinComponent {
    private val log: LogWrapper by inject<LogWrapper>()

    @Composable
    fun PlayerTransport(
        model: Model,
        view: BaseMviView<Model, Event>,
        contentColor: Color = Color.White,
        modifier: Modifier,
    ) {
        log.tag("PlayerComposeables")
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(colorTransparentBlack)
        ) {

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { view.dispatch(TrackBackClicked) }) {
                    Icon(
                        painterResource(Res.drawable.ic_player_track_b),
                        tint = contentColor,
                        contentDescription = model.texts.lastTrackText,
                        modifier = Modifier.size(buttonSize)
                            .padding(buttonPadding)
                    )
                }
                Box {
                    IconButton(onClick = { view.dispatch(SkipBackClicked) }) {
                        Icon(
                            painterResource(Res.drawable.ic_player_fast_rewind),
                            tint = contentColor,
                            contentDescription = model.texts.skipBackText,
                            modifier = Modifier.size(buttonSize)
                                .padding(buttonPadding)
                        )
                    }
                    Text(
                        model.texts.skipBackText ?: "-??s",
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }

                if (model.playState == PlayerStateDomain.PLAYING) {
                    IconButton(onClick = {
                        log.d("Pause clicked")
                        view.dispatch(PlayPauseClicked(true))
                    }) {
                        Icon(
                            painterResource(Res.drawable.ic_player_pause),
                            tint = contentColor,
                            contentDescription = "Pause",
                            modifier = Modifier.size(buttonSize)
                                .padding(buttonPadding)
                        )
                    }
                } else {
                    IconButton(onClick = {
                        log.d("Play clicked")
                        view.dispatch(PlayPauseClicked(false))
                    }) {
                        Icon(
                            painterResource(Res.drawable.ic_player_play),
                            tint = contentColor,
                            contentDescription = "Play",
                            modifier = Modifier.size(buttonSize)
                                .padding(buttonPadding)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { view.dispatch(SkipFwdClicked) }) {
                        Icon(
                            painterResource(Res.drawable.ic_player_fast_forward),
                            tint = contentColor,
                            contentDescription = model.texts.skipFwdText,
                            modifier = Modifier.size(buttonSize)
                                .padding(buttonPadding)
                        )
                    }
                    Text(
                        model.texts.skipFwdText ?: "??s",
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
                IconButton(onClick = { view.dispatch(TrackFwdClicked) }) {
                    Icon(
                        painterResource(Res.drawable.ic_player_track_f),
                        tint = contentColor,
                        contentDescription = model.texts.nextTrackText,
                        modifier = Modifier.size(buttonSize)
                            .padding(buttonPadding)
                    )
                }
            }

            var sliderPosition by remember { mutableStateOf(0f) }
            LaunchedEffect(model.times.seekBarFraction) {
                sliderPosition = model.times.seekBarFraction
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text(
                    model.times.positionText,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    model.texts.title ?: "Unknown title",
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    model.times.durationText,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }


            Slider(
                value = sliderPosition,
                onValueChange = { fraction -> sliderPosition = fraction },
                onValueChangeFinished = { view.dispatch(SeekBarChanged(sliderPosition)) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = contentColor,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    fun VolumeDisplay(
        volume: Float, // 0..1
        modifier: Modifier,
    ) {
        Row(modifier = modifier) {
            Text(
                stringResource(Res.string.player_volume, (volume * 100).toInt()),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
