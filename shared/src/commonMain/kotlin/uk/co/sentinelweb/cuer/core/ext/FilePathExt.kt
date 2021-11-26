package uk.co.sentinelweb.cuer.core.ext


fun String.getFileName() = this.substring(this.lastIndexOf("/") + 1)

fun String.fileExt(): String? = this
    .takeIf { this.lastIndexOf(".") > -1 }
    ?.substring(this.lastIndexOf(".") + 1)
