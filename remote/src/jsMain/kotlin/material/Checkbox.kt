@file: JsModule("@material-ui/core/Checkbox")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface IconButtonProps : RProps {
    var id: String?
    var onChange: () -> Unit?
    var checked: Boolean?
}

@JsName("default")
external val Checkbox: RClass<IconButtonProps>