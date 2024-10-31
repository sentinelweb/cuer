package uk.co.sentinelweb.cuer.app.ui.filebrowser

import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItem
import uk.co.sentinelweb.cuer.app.ui.filebrowser.FilesContract.ListItemType.*
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain
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
        list = state.currentListItems,
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
                val (title, tags) = cleanTitle(f.title)
                ListItem(
                    title = title,
                    dateModified = f.config.lastUpdate,
                    tags = tags,
                    isDirectory = true,
                    type = FOLDER,
                ) to f
            }
        }.plus(fileList.playlist.items.mapNotNull { f ->
            f.media.title?.let {
                val (title, tags) = cleanTitle(it)
                ListItem(
                    title = title,
                    dateModified = f.dateAdded,
                    tags = tags,
                    isDirectory = true,
                    type = when (f.media.mediaType) {
                        MediaDomain.MediaTypeDomain.VIDEO -> VIDEO
                        MediaDomain.MediaTypeDomain.AUDIO -> AUDIO
                        MediaDomain.MediaTypeDomain.WEB -> WEB
                        MediaDomain.MediaTypeDomain.FILE -> FILE
                    }
                ) to f
            }
        }).toMap()

    fun cleanTitle(title: String): Pair<String, List<String>> {
        // Regular expressions for tags, season/episode, and resolution
        val bracketPattern = "\\[(.+?)\\]".toRegex()
        val seasonEpisodePattern = "(?i)s\\d{2}e\\d{2}".toRegex()
        val resolutionPattern = "\\b\\d{3,4}p\\b".toRegex()

        // Extract items in square brackets as tags
        val tags = bracketPattern.findAll(title).map { it.groupValues[1] }.toMutableList()

        // Extract season/episode code and resolution
        val seasonEpisode = seasonEpisodePattern.find(title)?.value
        val resolution = resolutionPattern.find(title)?.value

        // Add season/episode and resolution to tags if they exist
        seasonEpisode?.let { tags.add(it) }
        resolution?.let { tags.add(it) }

        // Clean the title to be human-readable
        val humanReadableTitle = title
            .replace(bracketPattern, "") // Remove brackets and their content
            .replace(seasonEpisodePattern, "") // Remove season/episode code
            .replace(resolutionPattern, "") // Remove resolution
            .replace("[._-]".toRegex(), " ") // Replace special characters with space
            .replace("\\s+".toRegex(), " ") // Replace multiple spaces with a single space
            .trim()

        return Pair(humanReadableTitle, tags)
    }
}
