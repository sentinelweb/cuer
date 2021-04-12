package uk.co.sentinelweb.cuer.app.db.util

fun setFlag(flags: Long, flag: Long, value: Boolean): Long = if (value) flags or flag else flags and flags.inv()