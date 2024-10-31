package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItem
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItemType.*
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.ext.name

class FilesModelMapper {

    fun map(
        state: FilesContract.State,
        loading: Boolean,
    ) = FilesContract.Model(
        loading = loading,
        nodeName = state.sourceNode?.name(),
        filePath = state.path?.let { "/$it" },
        list = state.currentListItems
            ?.entries
            ?.sortedWith(compareBy({ it.key.title.lowercase() }, { it.key.season?.lowercase() }))
            ?.associate { it.toPair() },
    )

    fun mapToIntermediate(fileList: PlaylistAndChildrenDomain): Map<ListItem, Domain> =
        fileList.children.map { f ->
            if ("..".equals(f.title)) {
                ListItem(
                    title = f.title,
                    dateModified = f.config.lastUpdate,
                    tags = listOf(),
                    isDirectory = true,
                    type = UP,
                ) to f
            } else {
                cleanTitle(f.title).copy(
                    dateModified = f.config.lastUpdate,
                    isDirectory = true,
                    type = FOLDER,
                ) to f
            }
        }.plus(fileList.playlist.items.mapNotNull { f ->
            f.media.title?.let {
                cleanTitle(it).copy(
                    dateModified = f.dateAdded,
                    isDirectory = true,
                    type = when (f.media.mediaType) {
                        MediaTypeDomain.VIDEO -> VIDEO
                        MediaTypeDomain.AUDIO -> AUDIO
                        MediaTypeDomain.WEB -> WEB
                        MediaTypeDomain.FILE -> FILE
                    }
                ) to f
            }
        }).toMap()

    val unwantedSubstrings =
        listOf("WEB", "WEBRip", "H.264", "AAC", "h264", "BluRay", "Xvid", "DVDRip", "Rip", "x264",
            "AMZN", "x265", "HEVC", "HDTV", "HMAX", "DDP5")
            .map { it to it.toRegex(RegexOption.IGNORE_CASE) }
    val bracketPattern = "\\[(.+?)\\]".toRegex()
    val seasonEpisodePattern = "(?i)s\\d{2}e\\d{2}".toRegex()
    val resolutionPattern = "\\b\\d{3,4}p\\b".toRegex()
    val sizePattern = "\\b\\d{3,4}[mM][bB]\\b".toRegex()
    val fileExtensionPattern = "\\.([a-zA-Z0-9]+)$".toRegex()

    fun cleanTitle(title: String): ListItem {
        // Extract items in square brackets as tags
        val tags = bracketPattern.findAll(title).map { it.groupValues[1] }.toMutableList()

        // Extract season/episode code and resolution
        resolutionPattern.find(title)?.value?.let { tags.add(it) }
        sizePattern.find(title)?.value?.let { tags.add(it) }

        val season = seasonEpisodePattern.find(title)?.value
        val ext = fileExtensionPattern.find(title)?.groupValues?.get(1)

        var cleanedTitle = title

        for (substring in unwantedSubstrings) {
            substring.second.find(title)?.value?.let { tags.add(it) }
            cleanedTitle = cleanedTitle.replace(substring.first, "")
        }
        // Clean the title to be human-readable
        cleanedTitle = cleanedTitle
            .replace(bracketPattern, "")
            .replace(seasonEpisodePattern, "")
            .replace(resolutionPattern, "")
            .replace(sizePattern, "")
            .replace(fileExtensionPattern, "")
            .replace("[._-]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()

        return ListItem(
            title = cleanedTitle,
            dateModified = null,
            isDirectory = false,
            tags = tags,
            type = FILE,
            ext = ext,
            season = season,
        )
    }
}
