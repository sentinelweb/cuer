package uk.co.sentinelweb.cuer.net.ext

fun String.replaceUrlPlaceholder(P_COMMAND: String, value: String) =
    replace("{$P_COMMAND}", value)