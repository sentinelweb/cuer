package uk.co.sentinelweb.cuer.app.util.wrapper

import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

abstract class ShareWrapper {
    abstract fun share(media: MediaDomain)
    abstract fun share(playlist: PlaylistDomain)
    abstract fun open(url: String)

    fun fullMessage(media: MediaDomain): String {
        return """Hey! Check out this video:
                    |
                    |${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${YoutubeUrl.channelUrl(media)})
                    | 
                    |🌎 https://cuer.app
                    """.trimIndent()
    }

    fun shortMessage(media: MediaDomain): String {
        return """${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${YoutubeUrl.channelUrl(media)})
                    """.trimIndent()
    }

    fun playlistMessage(playlist: PlaylistDomain): String {
        val header = """
            Playlist: ${playlist.title}
            
            
        """.trimIndent()
        val url = playlist.platformId?.let {
            """
            URL: ${YoutubeUrl.playlistUrl(playlist)}
            
            
            """.trimIndent()
        } ?: ""
        val desc = playlist.config.description?.let { "Description:\n $it\n\n" } ?: ""

        val items = if (playlist.items.size > 0) {
            "🎥 Videos:\n\n" +
                    playlist.items.joinToString("\n") {
                        it.media.let {
                            (if (it.starred) "⭐️ " else "") + """
                        ${it.title}
                        ${YoutubeUrl.videoShortUrl(it)}
                           by: ${it.channelData.title} = ${it.channelData.customUrl ?: YoutubeUrl.channelUrl(it.channelData)}
                        
                    """.trimIndent()
                        }
                    }
        } else ""
        val footer = "🌎 https://cuer.app"

        return header + url + desc + items + footer
    }
}