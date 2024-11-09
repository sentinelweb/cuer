package uk.co.sentinelweb.cuer.app.ui.player

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

object PlayerEventToIntentMapper {

    fun eventToIntent(event: Event): Intent {
        return when (event) {
            is Event.PlayerStateChanged -> Intent.PlayState(event.state)
            is Event.TrackFwdClicked -> Intent.TrackFwd
            is Event.TrackBackClicked -> Intent.TrackBack
            is Event.SkipFwdClicked -> Intent.SkipFwd
            is Event.SkipBackClicked -> Intent.SkipBack
            is Event.PositionReceived -> Intent.Position(event.ms)
            is Event.SkipFwdSelectClicked -> Intent.SkipFwdSelect
            is Event.SkipBackSelectClicked -> Intent.SkipBackSelect
            is Event.PlayPauseClicked -> Intent.PlayPause(event.isPlaying)
            is Event.SeekBarChanged -> Intent.SeekToFraction(event.fraction)
            is Event.PlaylistClicked -> Intent.PlaylistView
            is Event.ItemClicked -> Intent.PlaylistItemView
            is Event.LinkClick -> Intent.LinkOpen(event.link)
            is Event.ChannelClick -> Intent.ChannelOpen
            is Event.TrackClick -> Intent.TrackSelected(
                event.item,
                event.resetPosition
            )

            is Event.DurationReceived -> Intent.Duration(event.ms)
            is Event.IdReceived -> Intent.Id(event.videoId)
            is Event.FullScreenClick -> Intent.FullScreenPlayerOpen
            is Event.PortraitClick -> Intent.PortraitPlayerOpen
            is Event.PipClick -> Intent.PipPlayerOpen
            //is OnDestroy -> {log.d("map destroy");Destroy}
            is Event.OnInitFromService -> Intent.InitFromService(
                event.playlistAndItem
            )

            is Event.OnPlayItemFromService -> Intent.PlayItemFromService(
                event.playlistAndItem
            )

            is Event.OnSeekToPosition -> Intent.SeekToPosition(event.ms)
            is Event.Support -> Intent.Support
            is Event.StarClick -> Intent.StarClick
            is Event.OpenClick -> Intent.OpenInApp
            is Event.ShareClick -> Intent.Share
            is Event.VolumeChanged -> Intent.VolumeChanged(event.vol)
            is Event.OnScreenAcquired -> Intent.ScreenAcquired(event.screen)
            Event.OnResume -> Intent.Resume
        }

    }

}
