import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.summarise

fun PlaylistItemDomain.summarise(): String =
    "ITEM: id: $id, order: $order, playlistId: $playlistId, media: ${media.summarise()}"
