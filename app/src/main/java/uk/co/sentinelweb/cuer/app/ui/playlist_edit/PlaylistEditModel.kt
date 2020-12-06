package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel

data class PlaylistEditModel constructor(
    val titleDisplay: CharSequence,
    val titleEdit: CharSequence,
    val imageUrl: String?,
    val thumbUrl: String?,
    val starred: Boolean,
    val validation: ValidatorModel?
)