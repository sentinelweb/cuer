package uk.co.sentinelweb.cuer.net.ext

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.toLocalNetworkIdentifier
import uk.co.sentinelweb.cuer.domain.*

internal fun PlaylistDomain.convertIdsLocalNetowrk(locator: Locator) = copy(
    id = id?.id?.toLocalNetworkIdentifier(locator),
    image = image?.convertIdsLocalNetowrk(locator),
    thumb = thumb?.convertIdsLocalNetowrk(locator),
    items = items.map { it.convertIdsLocalNetowrk(locator) }
)

internal fun PlaylistItemDomain.convertIdsLocalNetowrk(locator: Locator) = copy(
    id = id?.id?.toLocalNetworkIdentifier(locator),
    media = media.convertIdsLocalNetowrk(locator)
)

internal fun MediaDomain.convertIdsLocalNetowrk(locator: Locator) = copy(
    id = id?.id?.toLocalNetworkIdentifier(locator),
    image = image?.convertIdsLocalNetowrk(locator),
    thumbNail = thumbNail?.convertIdsLocalNetowrk(locator),
    channelData = channelData.convertIdsLocalNetowrk(locator)
)

internal fun ChannelDomain.convertIdsLocalNetowrk(locator: Locator) = copy(
    id = id?.id?.toLocalNetworkIdentifier(locator),
    image = image?.convertIdsLocalNetowrk(locator),
    thumbNail = thumbNail?.convertIdsLocalNetowrk(locator)
)

internal fun ImageDomain.convertIdsLocalNetowrk(locator: Locator) = copy(
    id = id?.id?.toLocalNetworkIdentifier(locator),
)