package uk.co.sentinelweb.cuer.net.youtube.videos

enum class YoutubePart(val part: String, val cost: Int, val requireAuth: Boolean) {
    ID("id", 0, false),
    PLAYER("player", 0, false),
    CONTENT_DETAILS("contentDetails", 2, false),
    FILEDETAILS("fileDetails", 1, true),
    LOCALIZATIONS("localizations", 2, false),
    RECORDINGDETAILS("recordingDetails", 2, true),
    SNIPPET("snippet", 2, false),
    STATISTICS("statistics", 2, false),
    STATUS("status", 2, false),
    SUGGESTIONS("suggestions", 1, true),
    TOPICDETAILS("topicDetails", 2, true),
    LIVE_BROADCAST_DETAILS("liveStreamingDetails", 1, false)
}