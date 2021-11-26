package uk.co.sentinelweb.cuer.app.ui.common.validator

import androidx.annotation.StringRes

data class ValidatorModel constructor(
    val valid: Boolean,
    val fieldValidations: List<ValidatorFieldModel>
) {
    data class ValidatorFieldModel constructor(
        val field: Field,
        @StringRes val error: Int
    )

    interface Field
}