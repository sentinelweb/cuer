package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import uk.co.sentinelweb.cuer.domain.LinkDomain


class CryptoLauncher(
    private val activity: Activity,
    private val toast: ToastWrapper
) {

    fun launch(link: LinkDomain.CryptoLinkDomain) {
        val clipboardManager: ClipboardManager =
            activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(link.type.toString(), link.address)
        clipboardManager.setPrimaryClip(clipData)
        // todo show warning dialog
        // todo show list of crypto payment apps
        toast.show("${link.type} address copied")
    }
}