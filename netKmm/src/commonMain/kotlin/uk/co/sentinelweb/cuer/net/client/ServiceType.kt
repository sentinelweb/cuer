package uk.co.sentinelweb.cuer.net.client

enum class ServiceType(val baseUrl: String) {
    YOUTUBE("https://www.googleapis.com/youtube/v3"),
    PIXABAY("https://pixabay.com/api"),
    REMOTE("http://"),
}