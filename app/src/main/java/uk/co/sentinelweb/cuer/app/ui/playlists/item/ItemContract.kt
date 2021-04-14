package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.text.SpannableString
import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface ItemContract {

    interface View {
        fun setTopText(text: SpannableString)
        fun setBottomText(text: SpannableString)
        fun setIconResource(@DrawableRes iconRes: Int)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setIconUrl(url: String)
        fun showOverflow(showOverflow: Boolean)
    }

    interface Presenter {
        fun doClick()
        fun doPlay(external: Boolean)
        fun doStar()
        fun doShare()
        fun canEdit(): Boolean
        fun canShare(): Boolean
        fun canPlay(): Boolean
        fun canLaunch(): Boolean
    }

    interface External : ItemBaseContract.ItemPresenterBase {
        fun update(item: Model, current: Boolean)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: Model)
        fun onRightSwipe(item: Model)
        fun onLeftSwipe(item: Model)
        fun onPlay(item: Model, external: Boolean)
        fun onStar(item: Model)
        fun onShare(item: Model)
    }

    data class State constructor(var item: Model? = null)

    data class Model(
        override val id: Long,// todo OrchestratorContract.Identifier
        val index: Int,
        val title: String,
        val checkIcon: Boolean,
        val thumbNailUrl: String?,
        val starred: Boolean,
        val count: Int,
        val newItems: Int,
        val loopMode: PlaylistDomain.PlaylistModeDomain,
        val type: PlaylistDomain.PlaylistTypeDomain,
        val platform: PlatformDomain?,
        val showOverflow: Boolean,
        val source: OrchestratorContract.Source,
        val canPlay: Boolean,
        val canEdit: Boolean,
        val canLaunch: Boolean,
        val canShare: Boolean,
        val watched: Boolean
    ) : ItemBaseModel(id)

}
