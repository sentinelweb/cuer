package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class AndroidIconMapper(
    val iconMapper: IconMapper,
    val res: ResourceWrapper
) {
    @DrawableRes
    fun map(platform: PlatformDomain?): Int =
        res.getDrawableResourceId(iconMapper.map(platform))

    @DrawableRes
    fun map(mode: PlaylistDomain.PlaylistModeDomain): Int =
        res.getDrawableResourceId(iconMapper.map(mode))

    @DrawableRes
    fun map(type: PlaylistDomain.PlaylistTypeDomain, platform: PlatformDomain?): Int =
        res.getDrawableResourceId(iconMapper.map(type, platform))

    @DrawableRes
    fun map(category: LinkDomain.Category): Int =
        res.getDrawableResourceId(iconMapper.map(category))

    @DrawableRes
    @Suppress("ComplexMethod")
    fun map(domainHost: LinkDomain.DomainHost): Int =
        res.getDrawableResourceId(iconMapper.map(domainHost))

    @DrawableRes
    fun map(coin: LinkDomain.Crypto): Int =
        res.getDrawableResourceId(iconMapper.map(coin))

    @DrawableRes
    fun map(link: LinkDomain): Int = res.getDrawableResourceId(iconMapper.map(link))
}