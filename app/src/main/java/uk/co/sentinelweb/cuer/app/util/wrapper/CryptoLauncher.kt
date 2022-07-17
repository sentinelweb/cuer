package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain


class CryptoLauncher(
    private val activity: Activity,
    private val toast: ToastWrapper,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun launch(link: LinkDomain.CryptoLinkDomain) {
        val clipboardManager: ClipboardManager =
            activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(link.type.toString(), link.address)
        clipboardManager.setPrimaryClip(clipData)
        // todo show warning dialog
        // todo show list of crypto payment apps
        toast.show("${link.type} address copied")

        val appsInstalled = cryptoAppWhiteList
            .mapNotNull { isPackageInstalled(it, activity.packageManager) }
            .map { packageInfo ->
                AppDetails(
                    title = activity.packageManager.getApplicationLabel(packageInfo.applicationInfo),
                    packageName = packageInfo.packageName,
                    icon = packageInfo.applicationInfo.loadIcon(activity.packageManager)
                )
            }
            .forEach { appDetails -> log.d("${appDetails.title} - ${appDetails.packageName}") }
    }

    data class AppDetails(
        val title: CharSequence,
        val packageName: String,
        val icon: Drawable
    )

    // todo check if we need to declare queries in manifest
    //https://stackoverflow.com/questions/18752202/check-if-application-is-installed-android
    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): PackageInfo? {
        return try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    // https://www.androidauthority.com/best-crypto-wallets-1230685/
    // note ensure packages are added to Manifest query for this check to work
    val cryptoAppWhiteList = listOf(
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
    )
}