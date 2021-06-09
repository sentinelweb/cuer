package uk.co.sentinelweb.cuer.remote.util

class WebLink {

    fun replaceLinks(it: String): String {
        return it.replace(REGEX) { m ->
            """<a href="${m.value}" target="_blank">${m.value}</a>"""
        }

    }

    companion object {
        private const val LINK_PATTERN =
            """(?:(?:https?|ftp):\/\/|\b(?:[a-z\d]+\.))(?:(?:[^\s()<>]+|\((?:[^\s()<>]+|(?:\([^\s()<>]+\)))?\))+(?:\((?:[^\s()<>]+|(?:\(?:[^\s()<>]+\)))?\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))?"""
        val REGEX = LINK_PATTERN.toRegex()
    }
}