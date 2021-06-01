@file: JsModule("@material-ui/core/Toolbar")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface ToolbarProps : RProps {
    var id: String?
}

@JsName("default")
external val Toolbar: RClass<ToolbarProps>