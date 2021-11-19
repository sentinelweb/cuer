package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class IconMapper {
    @DrawableRes
    fun map(platform: PlatformDomain?): Int = when (platform) {
        PlatformDomain.YOUTUBE -> R.drawable.ic_platform_youtube_24_black
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
}