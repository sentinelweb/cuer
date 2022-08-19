package uk.co.sentinelweb.cuer.domain.ext

inline fun Int.hasFlag(flag: Int) = this and flag == flag
inline fun Long.hasFlag(flag: Long) = this and flag == flag
inline fun Long.hasFlag(flag: Int) = this and flag.toLong() == flag.toLong()