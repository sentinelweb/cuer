package uk.co.sentinelweb.cuer.net.client

class RequestFailureException(
    val code: Int,
    val description: String
) : Exception("$code: $description")