import uk.co.sentinelweb.cuer.domain.ChannelDomain

fun ChannelDomain.summarise(): String = """
    CHANNEL: id: $id, platform: $platform - $platformId, title: $title
""".trimIndent()
