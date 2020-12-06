package uk.co.sentinelweb.cuer.app.db.backup.version.v1

private typealias MediaDomainV1 = MediaDomain
private typealias MediaTypeDomainV1 = MediaDomain.MediaTypeDomain
private typealias ChannelDomainV1 = ChannelDomain
private typealias ImageDomainV1 = ImageDomain
private typealias PlatformDomainV1 = PlatformDomain

private typealias MediaDomainMain = uk.co.sentinelweb.cuer.domain.MediaDomain
private typealias MediaTypeDomainMain = uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
private typealias ChannelDomainMain = uk.co.sentinelweb.cuer.domain.ChannelDomain
private typealias ImageDomainMain = uk.co.sentinelweb.cuer.domain.ImageDomain
private typealias PlatformDomainMain = uk.co.sentinelweb.cuer.domain.PlatformDomain

class V1Mapper {

    fun map(mediaV1: MediaDomainV1) = MediaDomainMain(
        id = mediaV1.id?.toLong(),
        platform = mapPlatform(mediaV1.platform),
        channelData = mapChannel(mediaV1.channelData, mapPlatform(mediaV1.platform)),
        published = mediaV1.published,
        thumbNail = mediaV1.thumbNail?.let { mapImage(it) },
        image = mediaV1.image?.let { mapImage(it) },
        platformId = mediaV1.mediaId,
        url = mediaV1.url,
        starred = mediaV1.starred,
        description = mediaV1.description,
        title = mediaV1.title,
        watched = mediaV1.dateLastPlayed != null,
        dateLastPlayed = mediaV1.dateLastPlayed,
        duration = mediaV1.duration,
        mediaType = mapMediaType(mediaV1.mediaType),
        positon = mediaV1.positon
    )

    private fun mapMediaType(mediaTypeV1: MediaTypeDomainV1) =
        MediaTypeDomainMain.valueOf(mediaTypeV1.toString())

    private fun mapPlatform(platformV1: PlatformDomainV1) =
        PlatformDomainMain.valueOf(platformV1.toString())

    private fun mapChannel(
        channelV1: ChannelDomainV1,
        platform: PlatformDomainMain
    ) = ChannelDomainMain(
        id = null,
        platform = platform,
        title = channelV1.title,
        description = channelV1.description,
        starred = channelV1.starred,
        image = channelV1.image?.let { mapImage(it) },
        thumbNail = channelV1.thumbNail?.let { mapImage(it) },
        country = null,
        customUrl = null,
        platformId = channelV1.id,
        published = channelV1.published
    )

    private fun mapImage(image: ImageDomainV1) = ImageDomainMain(
        url = image.url,
        height = image.height,
        width = image.width
    )

}