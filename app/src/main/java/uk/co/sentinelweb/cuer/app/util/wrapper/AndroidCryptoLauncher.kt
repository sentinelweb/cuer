package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain

class AndroidCryptoLauncher(
    private val activity: Activity,
    private val toast: ToastWrapper,
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
        // todo show warning dialog
        toast.show("${link.type.name} address copied")
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
