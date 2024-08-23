package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel

import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

interface SkipContract {

    interface Presenter {

    }

    interface External {
        val skipForwardText: String
        val skipBackText: String
        val skipForwardInterval: Int
        val skipBackInterval: Int
        var duration: Long
        var listener: Listener
        fun skipFwd()
        fun skipBack()
        fun updatePosition(ms: Long)
        fun stateChange(playState: PlayerStateDomain)
        fun onSelectSkipTime(fwd: Boolean)
        fun updateSkipTimes()
        fun updateTexts()
    }

    interface View {
        fun showDialog(model: SelectDialogModel)
    }

    interface Listener {
        fun skipSeekTo(target: Long)
        fun skipSetBackText(text: String)
        fun skipSetFwdText(text: String)
    }

    interface Mapper {
        fun mapForwardTime(time: Long): String
        fun mapBackTime(time: Long): String
        fun mapAccumulationTime(time: Long): String
        fun mapTimeSelectionDialogModel(
            currentTimeMs: Int,
            forward: Boolean,
            itemClick: (Int) -> Unit
        ): SelectDialogModel
    }

    data class State(
        var forwardJumpInterval: Int = 30000,
        var backJumpInterval: Int = 30000,
        var duration: Long = 0,
        var accumulator: Long = 0,
        var targetPosition: Long? = null,
        var position: Long = 0,
        var currentPlayState: PlayerStateDomain? = null,
        var videoReady:Boolean = false
    )
}
