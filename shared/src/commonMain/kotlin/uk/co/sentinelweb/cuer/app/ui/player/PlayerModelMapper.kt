package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlayerModelMapper constructor(

) {
    fun map(state: PlayerContract.MviStore.State): PlayerContract.View.Model =
        state.run {
            PlayerContract.View.Model(
                texts = PlayerContract.View.Model.Texts(
                    title = item?.media?.title ?: "No title",
                    playlistTitle = playlist?.title ?: "No playlist",
                    playlistData = playlist?.let { "${it.currentIndex + 1}/${it.items.size}" } ?: "",
                    nextTrackText = trackTitle(playlist, 1),
                    lastTrackText = trackTitle(playlist, -1),
                    skipBackText = "-1m",
                    skipFwdText = "1m"
                ),
                nextTrackEnabled = playlist?.run { currentIndex < playlist.items.size - 1 } ?: false,
                prevTrackEnabled = playlist?.run { currentIndex > 0 } ?: false,
                platformId = item?.media?.platformId,
                playState = playerState
            )
        }

    private fun trackTitle(playlist: PlaylistDomain?, offset: Int): String =
        playlist?.currentIndex
            ?.let { it + offset }
            ?.takeIf { it >= 0 && it < playlist.items.size }
            ?.let { playlist.items.get(it).media.title }
            ?: ""

}