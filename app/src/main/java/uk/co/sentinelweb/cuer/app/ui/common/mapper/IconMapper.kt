package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class IconMapper {
    @DrawableRes
    fun map(platform: PlatformDomain?): Int = when (platform) {
        PlatformDomain.YOUTUBE -> R.drawable.ic_platform_youtube
        PlatformDomain.WEB -> R.drawable.ic_platfrom_web
        PlatformDomain.VIMEO -> R.drawable.ic_platform_vimeo_24_black
        PlatformDomain.PODCAST -> R.drawable.ic_platform_podcast
        else -> R.drawable.ic_platfrom_web
    }

    @DrawableRes
    fun map(mode: PlaylistDomain.PlaylistModeDomain): Int =
        when (mode) {
            PlaylistDomain.PlaylistModeDomain.SINGLE -> R.drawable.ic_playmode_straight
            PlaylistDomain.PlaylistModeDomain.LOOP -> R.drawable.ic_button_repeat_24
            PlaylistDomain.PlaylistModeDomain.SHUFFLE -> R.drawable.ic_playmode_shuffle
        }

    @DrawableRes
    fun map(type: PlaylistDomain.PlaylistTypeDomain, platform: PlatformDomain?): Int =
        when (type) {
            PlaylistDomain.PlaylistTypeDomain.USER -> R.drawable.ic_person
            PlaylistDomain.PlaylistTypeDomain.PLATFORM -> map(platform)
            PlaylistDomain.PlaylistTypeDomain.APP -> R.drawable.ic_menu_settings_black
        }

    @DrawableRes
    fun map(category: LinkDomain.Category): Int =
        when (category) {
            LinkDomain.Category.SUPPORT -> R.drawable.ic_support
            LinkDomain.Category.VIDEO -> R.drawable.ic_video
            LinkDomain.Category.SOCIAL -> R.drawable.ic_social
            LinkDomain.Category.PODCAST -> R.drawable.ic_podcast
            LinkDomain.Category.OTHER -> R.drawable.ic_platfrom_web
        }

    @DrawableRes
    fun map(domainHost: LinkDomain.DomainHost): Int =
        when (domainHost) {
            LinkDomain.DomainHost.GOOGLE -> R.drawable.ic_google
            LinkDomain.DomainHost.AMAZON -> R.drawable.ic_amazon
            LinkDomain.DomainHost.PATREON -> R.drawable.ic_patreon
            LinkDomain.DomainHost.PAYPAL -> R.drawable.ic_paypal
            LinkDomain.DomainHost.APPLE_PODCASTS -> R.drawable.ic_apple
            LinkDomain.DomainHost.SPOTIFY_PODCASTS -> R.drawable.ic_spotify
            LinkDomain.DomainHost.BUY_ME_A_COFFEE -> R.drawable.ic_buymeacoffee
            LinkDomain.DomainHost.KO_FI -> R.drawable.ic_ko_fi
            LinkDomain.DomainHost.FACEBOOK -> R.drawable.ic_facebook
            LinkDomain.DomainHost.INSTAGRAM -> R.drawable.ic_instagram
            LinkDomain.DomainHost.TWITTER -> R.drawable.ic_platform_twitter_24_black
            LinkDomain.DomainHost.DISCORD -> R.drawable.ic_discord
            LinkDomain.DomainHost.YOUTUBE -> R.drawable.ic_youtube
            LinkDomain.DomainHost.VIMEO -> R.drawable.ic_vimeo
            LinkDomain.DomainHost.SOUNDCLOUD -> R.drawable.ic_soundcloud
            LinkDomain.DomainHost.TEESPRING -> R.drawable.ic_teespring
            LinkDomain.DomainHost.TWITCH -> R.drawable.ic_twitch
            LinkDomain.DomainHost.WIKIPEDIA -> R.drawable.ic_wikipedia
            LinkDomain.DomainHost.ANCHOR_FM -> R.drawable.ic_platfrom_web
            LinkDomain.DomainHost.UNKNOWN -> R.drawable.ic_platfrom_web
        }

    @DrawableRes
    fun map(coin: LinkDomain.Crypto): Int =
        when (coin) {
            LinkDomain.Crypto.BITCOIN -> R.drawable.ic_bitcoin
            LinkDomain.Crypto.ETHERIUM -> R.drawable.ic_eth
            LinkDomain.Crypto.LITECOIN -> R.drawable.ic_litecoin
            LinkDomain.Crypto.DOGE -> R.drawable.ic_doge
            LinkDomain.Crypto.XMR -> R.drawable.ic_xmr
            LinkDomain.Crypto.XRP -> R.drawable.ic_ripple
            LinkDomain.Crypto.NEO -> R.drawable.ic_neo
        }

    @DrawableRes
    fun map(link: LinkDomain): Int = when (link) {
        is LinkDomain.UrlLinkDomain -> map(link.domain)
        is LinkDomain.CryptoLinkDomain -> map(link.type)
    }


}