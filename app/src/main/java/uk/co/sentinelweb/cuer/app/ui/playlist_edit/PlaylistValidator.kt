package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistValidator constructor(
    private val res: ResourceWrapper
) {
    fun validate(domain: PlaylistDomain): ValidatorModel {
        val errors = mutableListOf<ValidatorModel.ValidatorFieldModel>()
        if (domain.title.isBlank()) {
            errors.add(
                ValidatorModel.ValidatorFieldModel(
                    PlaylistField.TITLE,
                    R.string.pe_error_title_blank
                )
            )
        }
        return ValidatorModel(
            errors.size == 0,
            errors.toList()
        )
    }

    enum class PlaylistField : ValidatorModel.Field {
        TITLE
    }
}