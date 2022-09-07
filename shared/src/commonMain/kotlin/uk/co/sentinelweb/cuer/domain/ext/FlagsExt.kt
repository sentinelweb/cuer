package uk.co.sentinelweb.cuer.domain.ext

inline fun Long.hasFlag(flag: Long) = (this and flag) == flag

inline fun Long.setFlag(flag: Long, value: Boolean): Long =
    if (value) this or flag
    else this and flag.inv()

inline fun makeFlags(vararg flags:Pair<Long, Boolean>): Long =
    flags.fold(0L) { out, flag -> out.setFlag(flag.first, flag.second) }