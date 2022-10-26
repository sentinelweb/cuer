package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.LinkDomain.Crypto.*
import uk.co.sentinelweb.cuer.domain.LinkDomain.DomainHost.*
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
            LinkDomain.Category.SUPPORT -> R.drawable.ic_donate
            LinkDomain.Category.CRYPTO -> R.drawable.ic_coin
            LinkDomain.Category.VIDEO -> R.drawable.ic_video
            LinkDomain.Category.SOCIAL -> R.drawable.ic_social
            LinkDomain.Category.PODCAST -> R.drawable.ic_podcast
            LinkDomain.Category.OTHER -> R.drawable.ic_platfrom_web
        }

    @DrawableRes
    @Suppress("ComplexMethod")
    fun map(domainHost: LinkDomain.DomainHost): Int =
        when (domainHost) {
            GOOGLE -> R.drawable.ic_google
            AMAZON -> R.drawable.ic_amazon
            PATREON -> R.drawable.ic_patreon
            PAYPAL -> R.drawable.ic_paypal
            APPLE_PODCASTS -> R.drawable.ic_apple
            SPOTIFY_PODCASTS -> R.drawable.ic_spotify
            BUY_ME_A_COFFEE -> R.drawable.ic_buymeacoffee
            KO_FI -> R.drawable.ic_ko_fi
            FACEBOOK -> R.drawable.ic_facebook
            INSTAGRAM -> R.drawable.ic_instagram
            TWITTER -> R.drawable.ic_platform_twitter_24_black
            DISCORD -> R.drawable.ic_discord
            YOUTUBE -> R.drawable.ic_youtube
            VIMEO -> R.drawable.ic_vimeo
            SOUNDCLOUD -> R.drawable.ic_soundcloud
            TEESPRING -> R.drawable.ic_teespring
            TWITCH -> R.drawable.ic_twitch
            WIKIPEDIA -> R.drawable.ic_wikipedia
            ANCHOR_FM -> R.drawable.ic_platfrom_web
            UNKNOWN -> R.drawable.ic_platfrom_web
        }

    @DrawableRes
    fun map(coin: LinkDomain.Crypto): Int =
        when (coin) {
            BITCOIN -> R.drawable.ic_bitcoin
            ETHERIUM -> R.drawable.ic_eth
            LITECOIN -> R.drawable.ic_litecoin
            DOGE -> R.drawable.ic_doge
            MONERO -> R.drawable.ic_xmr
            RIPPLE -> R.drawable.ic_ripple
            NEO -> R.drawable.ic_neo
            DASH -> R.drawable.ic_dash
            BITCOIN_CASH -> R.drawable.ic_bitcoincash
            ALGO -> R.drawable.ic_algo
            ADA_SHELLEY, ADA_DAEDALUS, ADA_ICARUS -> R.drawable.ic_cardano
        }

    @DrawableRes
    fun map(link: LinkDomain): Int = when (link) {
        is LinkDomain.UrlLinkDomain -> map(link.domain)
        is LinkDomain.CryptoLinkDomain -> map(link.type)
    }
}