package uk.co.sentinelweb.cuer.db

import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual val platform: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}