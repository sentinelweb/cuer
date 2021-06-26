package uk.co.sentinelweb.cuer.app.ui.player


class PlayerModelMapper constructor(

) {
    fun map(state: PlayerContract.MviStore.State): PlayerContract.View.Model =
        state.run {
            PlayerContract.View.Model(
                texts = PlayerContract.View.Model.Texts(
                    title = item?.media?.title ?: "No title",
                    playlistTitle = playlist?.title ?: "No playlist",
                    playlistData = playlist?.let { "${it.currentIndex + 1}/${it.items.size}" } ?: "",
                    nextTrackText = "Next track: ${playlist?.run { currentIndex + 1 } ?: "-"}",
                    lastTrackText = "Prev track: ${playlist?.run { currentIndex - 1 } ?: "-"}",
                    skipBackText = "-1m",
                    skipFwdText = "1m"
                ),
                platformId = item?.media?.platformId,
                playState = playerState,
                playCommand = command
            )
        }

}