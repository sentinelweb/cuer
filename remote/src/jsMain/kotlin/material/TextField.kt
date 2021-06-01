@file: JsModule("@material-ui/core/TextField")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface TextFieldProps : RProps {
    var id: String?
    var helperText: String?
    var error: Boolean?
    var required: Boolean?
    var disabled: Boolean?
    var onChange: () -> Unit?
}

@JsName("default")
external val TextField: RClass<TextFieldProps>