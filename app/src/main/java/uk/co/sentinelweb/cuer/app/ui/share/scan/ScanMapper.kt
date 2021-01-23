package uk.co.sentinelweb.cuer.app.ui.share.scan

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class ScanMapper constructor() {

    fun map(domain: MediaDomain): ScanContract.Model =
        ScanContract.Model(
            domain.url,
            ObjectTypeDomain.MEDIA,
            domain.title ?: "-",
            PlatformDomain.YOUTUBE,
            domain.platformId,
            R.drawable.ic_item_tick_white,
            true
        )

    fun map(domain: PlaylistDomain): ScanContract.Model =
        ScanContract.Model(
            domain.config.updateUrl!!,
            ObjectTypeDomain.PLAYLIST,
            domain.title ?: "-",
            PlatformDomain.YOUTUBE,
            domain.config.platformId!!,
            R.drawable.ic_item_tick_white,
            true
        )

    fun mapError(uri: String): ScanContract.Model =
        ScanContract.Model(
            uri,
            ObjectTypeDomain.UNKNOWN,
            uri,
            PlatformDomain.YOUTUBE,
            "none",
            R.drawable.ic_notif_close_white,
            false
        )

    fun mapMediaResult(isNew: Boolean, isOnPlaylist: Boolean, media: MediaDomain) = ScanContract.Result(
        isNew,
        isOnPlaylist,
        ObjectTypeDomain.MEDIA,
        media
    )

    fun mapPlaylistResult(isNew: Boolean, playlist: PlaylistDomain) = ScanContract.Result(
        isNew,
        isNew,
        ObjectTypeDomain.PLAYLIST,
        playlist
    )
}
