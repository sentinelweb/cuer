package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.fragment.app.FragmentActivity
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect.AppSelectorBottomSheet
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain

class AndroidCryptoLauncher(
    private val activity: Activity,
    private val toast: ToastWrapper,
    private val alertDialogCreator: AlertDialogCreator,
    log: LogWrapper,
) : CryptoLauncher {

    init {
        log.tag(this)
    }

    override fun launch(link: LinkDomain.CryptoLinkDomain) {
        val clipboardManager: ClipboardManager =
            activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(link.type.toString(), link.address)
        clipboardManager.setPrimaryClip(clipData)
        toast.show("${link.type.name} address copied")
        showWarningDialog()
    }

    private fun showWarningDialog() {
        alertDialogCreator.create(
            AlertDialogModel(
                title = R.string.support_crypto_warning_title,
                message = R.string.support_crypto_warning_message,
                confirm = AlertDialogModel.Button(
                    R.string.support_crypto_warning_ok,
                    { showCryptoAppLauncher() }
                ),
                cancel = AlertDialogModel.Button(R.string.cancel)
            )
        ).show()
    }

    private fun showCryptoAppLauncher() {
        AppSelectorBottomSheet.show(activity as FragmentActivity, cryptoAppWhiteList)
    }

    // https://www.androidauthority.com/best-crypto-wallets-1230685/
    // note ensure packages are added to Manifest query for this check to work
    override val cryptoAppWhiteList = listOf(
        "com.squareup.cash",
        "de.schildbach.wallet", // bitcoin.org wallet
        "co.mona.android", // crypto.com
        "com.bitcoin.mwallet", //bitcoin.com
        "com.coinomi.wallet",
        "com.coinbase.android",
        "fr.acinq.eclair.wallet.mainnet2", //eclair wallet
        "org.electrum.electrum",
        "exodusmovement.exodus",
        "com.gemini.android.app",
        "com.mycelium.wallet",
        "com.wallet.crypto.trustapp",
        "com.sofi.mobile",
        "com.binance.dev",
        "net.bitstamp.app",
        "com.revolut.revolut"
    )
}
