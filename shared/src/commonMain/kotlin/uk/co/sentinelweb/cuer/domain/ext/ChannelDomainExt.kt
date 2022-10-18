import uk.co.sentinelweb.cuer.domain.ChannelDomain

fun ChannelDomain.summarise(): String = """
    id: $id, title: $title, platform: $platform - $platformId
""".trimIndent()