package uk.co.sentinelweb.cuer.app.ui.common.mapper

import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.LinkDomain.Crypto.*
import uk.co.sentinelweb.cuer.domain.LinkDomain.DomainHost.*
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class IconMapper {
    //@DrawableRes
    fun map(platform: PlatformDomain?): Icon = when (platform) {
        PlatformDomain.YOUTUBE -> Icon.ic_platform_youtube
        PlatformDomain.WEB -> Icon.ic_platfrom_web
        PlatformDomain.VIMEO -> Icon.ic_platform_vimeo_24_black
        PlatformDomain.PODCAST -> Icon.ic_platform_podcast
        else -> Icon.ic_platfrom_web
    }

    //@DrawableRes
    fun map(mode: PlaylistDomain.PlaylistModeDomain): Icon =
        when (mode) {
            PlaylistDomain.PlaylistModeDomain.SINGLE -> Icon.ic_playmode_straight
            PlaylistDomain.PlaylistModeDomain.LOOP -> Icon.ic_button_repeat_24
            PlaylistDomain.PlaylistModeDomain.SHUFFLE -> Icon.ic_playmode_shuffle
        }

    //@DrawableRes
    fun map(type: PlaylistDomain.PlaylistTypeDomain, platform: PlatformDomain?): Icon =
        when (type) {
            PlaylistDomain.PlaylistTypeDomain.USER -> Icon.ic_person
            PlaylistDomain.PlaylistTypeDomain.PLATFORM -> map(platform)
            PlaylistDomain.PlaylistTypeDomain.APP -> Icon.ic_menu_settings_black
        }

    //@DrawableRes
    fun map(category: LinkDomain.Category): Icon =
        when (category) {
            LinkDomain.Category.SUPPORT -> Icon.ic_donate
            LinkDomain.Category.CRYPTO -> Icon.ic_coin
            LinkDomain.Category.VIDEO -> Icon.ic_video
            LinkDomain.Category.SOCIAL -> Icon.ic_social
            LinkDomain.Category.PODCAST -> Icon.ic_podcast
            LinkDomain.Category.OTHER -> Icon.ic_platfrom_web
        }

    //@DrawableRes
    @Suppress("ComplexMethod")
    fun map(domainHost: LinkDomain.DomainHost): Icon =
        when (domainHost) {
            GOOGLE -> Icon.ic_google
            AMAZON -> Icon.ic_amazon
            PATREON -> Icon.ic_patreon
            PAYPAL -> Icon.ic_paypal
            APPLE_PODCASTS -> Icon.ic_apple
            SPOTIFY_PODCASTS -> Icon.ic_spotify
            BUY_ME_A_COFFEE -> Icon.ic_buymeacoffee
            KO_FI -> Icon.ic_ko_fi
            FACEBOOK -> Icon.ic_facebook
            INSTAGRAM -> Icon.ic_instagram
            TWITTER -> Icon.ic_platform_twitter_24_black
            DISCORD -> Icon.ic_discord
            YOUTUBE -> Icon.ic_platform_youtube
            VIMEO -> Icon.ic_vimeo
            SOUNDCLOUD -> Icon.ic_soundcloud
            TEESPRING -> Icon.ic_teespring
            TWITCH -> Icon.ic_twitch
            WIKIPEDIA -> Icon.ic_wikipedia
            ANCHOR_FM -> Icon.ic_platfrom_web
            TIKTOK -> Icon.ic_tiktok
            UNKNOWN -> Icon.ic_platfrom_web
        }

    //@DrawableRes
    fun map(coin: LinkDomain.Crypto): Icon =
        when (coin) {
            BITCOIN -> Icon.ic_bitcoin
            ETHERIUM -> Icon.ic_eth
            LITECOIN -> Icon.ic_litecoin
            DOGE -> Icon.ic_doge
            MONERO -> Icon.ic_xmr
            RIPPLE -> Icon.ic_ripple
            NEO -> Icon.ic_neo
            DASH -> Icon.ic_dash
            BITCOIN_CASH -> Icon.ic_bitcoincash
            ALGO -> Icon.ic_algo
            ADA_SHELLEY, ADA_DAEDALUS, ADA_ICARUS -> Icon.ic_cardano
        }

    //@DrawableRes
    fun map(link: LinkDomain): Icon = when (link) {
        is LinkDomain.UrlLinkDomain -> map(link.domain)
        is LinkDomain.CryptoLinkDomain -> map(link.type)
    }
}