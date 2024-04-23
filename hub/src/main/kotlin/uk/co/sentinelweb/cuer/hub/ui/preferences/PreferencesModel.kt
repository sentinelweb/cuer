package uk.co.sentinelweb.cuer.hub.ui.preferences

data class PreferencesModel(
    val folderRoots: Set<String>
) {
    companion object{
        fun blankModel() = PreferencesModel(
            folderRoots = setOf()
        )
    }
}