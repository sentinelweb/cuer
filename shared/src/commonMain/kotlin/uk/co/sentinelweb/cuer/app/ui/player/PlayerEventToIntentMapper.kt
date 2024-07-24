package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event

object PlayerEventToIntentMapper {
    val eventToIntent: suspend Event.() -> Intent =
        {
            when (this) {
                is Event.PlayerStateChanged -> Intent.PlayState(state)
                is Event.TrackFwdClicked -> Intent.TrackFwd
                is Event.TrackBackClicked -> Intent.TrackBack
                is Event.SkipFwdClicked -> Intent.SkipFwd
                is Event.SkipBackClicked -> Intent.SkipBack
                is Event.PositionReceived -> Intent.Position(ms)
                is Event.SkipFwdSelectClicked -> Intent.SkipFwdSelect
                is Event.SkipBackSelectClicked -> Intent.SkipBackSelect
                is Event.PlayPauseClicked -> Intent.PlayPause(isPlaying)
                is Event.SeekBarChanged -> Intent.SeekToFraction(fraction)
                is Event.PlaylistClicked -> Intent.PlaylistView
                is Event.ItemClicked -> Intent.PlaylistItemView
                is Event.LinkClick -> Intent.LinkOpen(link)
                is Event.ChannelClick -> Intent.ChannelOpen
                is Event.TrackClick -> Intent.TrackSelected(
                    item,
                    resetPosition
                )

                is Event.DurationReceived -> Intent.Duration(ms)
                is Event.IdReceived -> Intent.Id(videoId)
                is Event.FullScreenClick -> Intent.FullScreenPlayerOpen
                is Event.PortraitClick -> Intent.PortraitPlayerOpen
                is Event.PipClick -> Intent.PipPlayerOpen
                //is OnDestroy -> {log.d("map destroy");Destroy}
                is Event.OnInitFromService -> Intent.InitFromService(
                    playlistAndItem
                )

                is Event.OnPlayItemFromService -> Intent.PlayItemFromService(
                    playlistAndItem
                )

                is Event.OnSeekToPosition -> Intent.SeekToPosition(ms)
                is Event.Support -> Intent.Support
                is Event.StarClick -> Intent.StarClick
                is Event.OpenClick -> Intent.OpenInApp
                is Event.ShareClick -> Intent.Share
                is Event.VolumeChanged -> Intent.VolumeChanged(vol)
            }
        }
}