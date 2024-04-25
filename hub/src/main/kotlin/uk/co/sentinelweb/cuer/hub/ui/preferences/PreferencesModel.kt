package uk.co.sentinelweb.cuer.hub.ui.preferences

data class PreferencesModel(
    val folderRoots: Set<String>,
    val isDatabaseInitialised:Boolean
) {
    companion object{
        fun blankModel() = PreferencesModel(
            folderRoots = setOf(),
            false
        )
    }
}