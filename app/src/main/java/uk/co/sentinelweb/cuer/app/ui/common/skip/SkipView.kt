package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel

class SkipView constructor(
    private val selectDialogCreator: SelectDialogCreator
) : SkipContract.View {

    override fun showDialog(model: SelectDialogModel) {
        selectDialogCreator.createSingle(model).show()
    }
}