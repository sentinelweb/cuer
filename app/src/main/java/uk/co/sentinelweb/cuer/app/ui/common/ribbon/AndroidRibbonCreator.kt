package uk.co.sentinelweb.cuer.app.ui.common.ribbon

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class AndroidRibbonCreator(
    private val res: ResourceWrapper,
) : RibbonCreator {

    override fun createRibbon(media: MediaDomain): List<RibbonModel> = listOf(
        RibbonModel(RibbonModel.Type.UNSTAR, res.getString(R.string.menu_unstar), R.drawable.ic_starred),
        RibbonModel(RibbonModel.Type.STAR, res.getString(R.string.menu_star), R.drawable.ic_starred_off),
        RibbonModel(RibbonModel.Type.SUPPORT, res.getString(R.string.menu_support), R.drawable.ic_support),
        RibbonModel(RibbonModel.Type.EDIT, res.getString(R.string.menu_edit), R.drawable.ic_edit_24),
        RibbonModel(RibbonModel.Type.SHARE, res.getString(R.string.menu_share), R.drawable.ic_share_black),
        RibbonModel(RibbonModel.Type.LIKE, res.getString(R.string.menu_like), R.drawable.ic_like_24),
        RibbonModel(RibbonModel.Type.COMMENT, res.getString(R.string.menu_comment), R.drawable.ic_comment_24),
        RibbonModel(RibbonModel.Type.LAUNCH, res.getString(R.string.menu_launch), R.drawable.ic_launch_black),
    )

    override fun createPlayerRibbon(media: MediaDomain): List<RibbonModel> = listOf(
        RibbonModel(RibbonModel.Type.UNSTAR, res.getString(R.string.menu_unstar), R.drawable.ic_starred),
        RibbonModel(RibbonModel.Type.STAR, res.getString(R.string.menu_star), R.drawable.ic_starred_off),
        RibbonModel(RibbonModel.Type.SUPPORT, res.getString(R.string.menu_support), R.drawable.ic_support),
        RibbonModel(RibbonModel.Type.SHARE, res.getString(R.string.menu_share), R.drawable.ic_share_black),
        RibbonModel(RibbonModel.Type.PIP, res.getString(R.string.menu_pip), R.drawable.ic_picture_in_picture),
        RibbonModel(RibbonModel.Type.FULL, res.getString(R.string.menu_full), R.drawable.ic_fullscreen_24),
        RibbonModel(RibbonModel.Type.LIKE, res.getString(R.string.menu_like), R.drawable.ic_like_24),
        RibbonModel(RibbonModel.Type.COMMENT, res.getString(R.string.menu_comment), R.drawable.ic_comment_24),
        RibbonModel(RibbonModel.Type.LAUNCH, res.getString(R.string.menu_launch), R.drawable.ic_launch_black),
    )
}