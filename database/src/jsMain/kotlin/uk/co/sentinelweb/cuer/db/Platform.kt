package uk.co.sentinelweb.cuer.db

import kotlinx.browser.document

actual class Platform actual constructor() {
    actual val platform: String = "Web: ${document.domain}"
}