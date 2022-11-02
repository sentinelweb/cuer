import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.summarise

fun PlaylistItemDomain.summarise(): String =
    "id: $id, order: $order, playlistId: $playlistId, media: ${media.summarise()}"
