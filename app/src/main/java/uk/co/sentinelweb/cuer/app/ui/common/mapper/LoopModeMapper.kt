package uk.co.sentinelweb.cuer.app.ui.common.mapper

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class LoopModeMapper {
    @DrawableRes
    fun mapIcon(mode: PlaylistDomain.PlaylistModeDomain): Int =
        when (mode) {
            PlaylistDomain.PlaylistModeDomain.SINGLE -> R.drawable.ic_button_shuffle_disabled_24
            PlaylistDomain.PlaylistModeDomain.LOOP -> R.drawable.ic_button_repeat_24
            PlaylistDomain.PlaylistModeDomain.SHUFFLE -> R.drawable.ic_button_shuffle_24
        }

}