package uk.co.sentinelweb.cuer.app.ui.common.dialog

interface AlertDialogContract {
    interface Creator {
        fun create(model: AlertDialogModel): Any /* dialog ref */

        fun createAndShowDialog(model: AlertDialogModel)

        fun dismissDialog(dialogRef: Any)
    }
}
