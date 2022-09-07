package uk.co.sentinelweb.cuer.db

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}