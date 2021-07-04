package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter

class SkipModelMapper constructor(
    private val timeSinceFormatter: TimeSinceFormatter,
    private val res: ResourceWrapper
) : SkipContract.Mapper {
    override fun mapForwardTime(time: Long): String = timeSinceFormatter.formatTimeShort(time)
    override fun mapBackTime(time: Long): String = "-" + timeSinceFormatter.formatTimeShort(time)
    override fun mapAccumulationTime(time: Long): String = timeSinceFormatter.formatTimeShort(time)

    override fun mapTimeSelectionDialogModel(
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