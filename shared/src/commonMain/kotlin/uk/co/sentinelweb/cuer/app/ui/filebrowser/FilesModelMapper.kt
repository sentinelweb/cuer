package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItem
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItem.ListItemType.*
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Alpha
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.Sort.Time
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase.Companion.PARENT_FOLDER_TEXT
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.name

class FilesModelMapper(
    private val timeSinceFormatter: TimeSinceFormatter
) {

    fun map(
        state: FilesContract.State,
        loading: Boolean,
    ) = FilesContract.Model(
        loading = loading,
        nodeName = state.sourceNode?.name(),
        filePath = state.path?.let { "/$it" },
        upListItem = state.upListItem,
        list = state.currentListItems
            ?.entries
            ?.sortedWith(
                if (state.sortAcending) {
                    when (state.sort) {
                        Time -> compareBy({ it.key.dateModified })
                        Alpha -> compareBy({ it.key.title.lowercase() })
                    }
                } else {
                    when (state.sort) {
                        Time -> compareByDescending({ it.key.dateModified })
                        Alpha -> compareByDescending({ it.key.title.lowercase() })
                    }
                }
            )
            ?.associate { it.toPair() }
            ?.toList(),
    )

    fun mapToIntermediate(fileList: PlaylistAndChildrenDomain): Map<ListItem, Domain> =
        fileList.children.map { f ->
            if (PARENT_FOLDER_TEXT.equals(f.title)) {
                mapParentItem(f)
            } else {
                mapFolderItem(f)
            }
        }.plus(fileList.playlist.items.mapNotNull { f ->
            f.media.title
                ?.takeIf { it != ".DS_Store" }
                ?.let {
                mapFileItem(it, f)
            }
        }).toMap()

    fun mapFileItem(
        it: String,
        domain: PlaylistItemDomain
    ) = cleanTitle(it).copy(
        dateModified = domain.dateAdded,
        isDirectory = true,
        type = when (domain.media.mediaType) {
            MediaTypeDomain.VIDEO -> VIDEO
            MediaTypeDomain.AUDIO -> AUDIO
            MediaTypeDomain.WEB -> WEB
            MediaTypeDomain.FILE -> FILE
        },
        timeSince = timeSinceFormatter.formatTimeSince(domain.dateAdded.toEpochMilliseconds())
    ) to domain

    fun mapFolderItem(domain: PlaylistDomain) = cleanTitle(domain.title).copy(
        dateModified = domain.config.lastUpdate,
        isDirectory = true,
        type = FOLDER,
        timeSince = domain.config.lastUpdate?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) }
    ) to domain

    fun mapParentItem(domain: PlaylistDomain) = ListItem(
        title = domain.title,
        dateModified = domain.config.lastUpdate,
        tags = listOf(),
        isDirectory = true,
        type = UP,
    ) to domain

    val unwantedSubstrings =
        listOf(
            "WEB", "WEBRip", "H.264", "AAC", "h264", "BluRay", "Xvid", "DVDRip", "Rip", "x264",
            "AMZN", "x265", "HEVC", "HDTV", "HMAX", "DDP5"
        ).map { it to it.toRegex(RegexOption.IGNORE_CASE) }
    val bracketPattern = "\\[(.+?)]".toRegex()
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

        for (unwanted in unwantedSubstrings) {
            unwanted.second.find(title)?.value?.let { tags.add(it) }
            cleanedTitle = cleanedTitle.replace(unwanted.second, "")
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
