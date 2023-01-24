package uk.co.sentinelweb.cuer.app.ui.common.views.description

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.TimecodeDomain

interface DescriptionContract {

    data class DescriptionModel constructor(
        val title: CharSequence?,
        val description: String?,
        val playlistChips: List<ChipModel> = listOf(ChipModel.PLAYLIST_SELECT_MODEL),
        val channelTitle: String?,
        val channelThumbUrl: String?,
        val channelDescription: String?,
        val pubDate: String?,
        val ribbonActions: List<RibbonModel>,
        val info: Info
    ) {
        data class Info(
            val platform: PlatformDomain,
            val platformId: String,
            val dbId: Long? = null
        )
    }

    interface Interactions {
        fun onLinkClick(link: LinkDomain.UrlLinkDomain)
        fun onChannelClick()
        fun onCryptoClick(cryptoAddress: LinkDomain.CryptoLinkDomain)
        fun onTimecodeClick(timecode: TimecodeDomain)
        fun onSelectPlaylistChipClick(model: ChipModel)
        fun onRemovePlaylist(chipModel: ChipModel)
        fun onRibbonItemClick(ribbonItem: RibbonModel)
        fun onPlaylistChipClick(chipModel: ChipModel)
    }

    companion object {
        val viewModule = module {
            factory { DescriptionMapper(get()) }
        }
    }
}
