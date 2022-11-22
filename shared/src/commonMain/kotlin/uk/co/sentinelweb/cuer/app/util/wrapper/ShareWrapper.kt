package uk.co.sentinelweb.cuer.app.util.wrapper

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface ShareWrapper {
    fun share(media: MediaDomain)
    fun share(playlist: PlaylistDomain)
}