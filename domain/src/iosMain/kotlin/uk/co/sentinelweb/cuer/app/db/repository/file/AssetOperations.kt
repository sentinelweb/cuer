package uk.co.sentinelweb.cuer.app.db.repository.file

import platform.Foundation.*

actual class AssetOperations() {
    actual fun getAsString(path: String): String? {
        NSBundle.mainBundle().pathForResource(path, ofType = "json")
            ?.let { pathApp ->
                val fm = NSFileManager()
                val exists = fm.fileExistsAtPath(pathApp)
                if (exists) {
                    return fm.contentsAtPath(pathApp)
                        ?.let { NSString.create(it, NSUTF8StringEncoding).toString() }
                }
            }
        return null
    }
}