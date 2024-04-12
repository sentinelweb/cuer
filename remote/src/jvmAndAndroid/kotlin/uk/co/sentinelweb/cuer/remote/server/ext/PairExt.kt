package uk.co.sentinelweb.cuer.remote.server.ext

fun <A : Any, B : Any> Pair<A?, B?>.checkNull(): Pair<A, B>? {
    return if (first != null && second != null) {
        Pair(first!!, second!!)
    } else null
}