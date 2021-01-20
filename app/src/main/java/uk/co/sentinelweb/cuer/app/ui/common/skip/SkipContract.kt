package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

interface SkipContract {

    interface Presenter {

    }

    interface External {
        val skipBackText: String
        val skipForwardText: String
        var duration: Long
        var listener: Listener
        fun skipFwd()
        fun skipBack()
        fun updatePosition(ms: Long)
        fun stateChange(playState: PlayerStateDomain)
        fun onSelectSkipTime(fwd: Boolean)
    }

    interface View {
        fun showDialog(model: SelectDialogModel)
    }

    interface Listener {
        fun skipSeekTo(target: Long)
        fun skipSetBackText(text: String)
        fun skipSetFwdText(text: String)
    }

    data class State(
        var forwardJumpInterval: Int = 30000,
        var backJumpInterval: Int = 30000,
        var duration: Long = 0,
        var accumulator: Long = 0,
        var targetPosition: Long? = null,
        var position: Long = 0,
        var currentStateState: PlayerStateDomain? = null
    )

    class Mapper constructor(
        private val timeSinceFormatter: TimeSinceFormatter,
        private val res: ResourceWrapper
    ) {
        fun mapForwardTime(time: Long): String = timeSinceFormatter.formatTimeShort(time)
        fun mapBackTime(time: Long): String = "-" + timeSinceFormatter.formatTimeShort(time)
        fun mapAccumulationTime(time: Long): String = timeSinceFormatter.formatTimeShort(time)

        fun mapTimeSelectionDialogModel(
            currentTimeMs: Int,
            forward: Boolean,
            itemClick: (Int) -> Unit
        ) = SelectDialogModel(
            type = DialogModel.Type.SKIP_TIME,
            multi = false,
            title = if (forward) R.string.dialog_title_select_fwd_skip_time else R.string.dialog_title_select_back_skip_time,
            items = res.getIntArray(R.array.skip_time_values).map { time ->
                SelectDialogModel.Item(
                    if (forward) mapForwardTime(time.toLong()) else mapBackTime(time.toLong()),
                    selected = (currentTimeMs == time),
                    selectable = true
                )
            },
            itemClick = { index, selected -> itemClick(res.getIntArray(R.array.skip_time_values)[index]) },
            confirm = null,
            dismiss = {}
        )
    }
}