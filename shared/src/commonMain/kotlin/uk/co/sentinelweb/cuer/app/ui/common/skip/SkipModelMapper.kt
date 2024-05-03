package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter

class SkipModelMapper constructor(
    private val timeSinceFormatter: TimeSinceFormatter
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
        title =
        if (forward) "Select forward skip time"
        else "Select backward skip time",
        items = skipChoices.map { time ->
            SelectDialogModel.Item(
                if (forward) mapForwardTime(time.toLong()) else mapBackTime(time.toLong()),
                selected = (currentTimeMs == time),
                selectable = true
            )
        },
        itemClick = { index, selected -> itemClick(skipChoices[index]) },
        confirm = null,
        dismiss = {}
    )

    companion object {
        //skipChoices = res.getIntArray(R.array.skip_time_values)
        private val skipChoices = intArrayOf(5000, 10000, 30000, 60000, 300000, 600000, 1800000)
    }
}