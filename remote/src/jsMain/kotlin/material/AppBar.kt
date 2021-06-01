@file: JsModule("@material-ui/core/AppBar")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface AppBarProps : RProps {
    var id: String?
    var color: String?
}

@JsName("default")
external val AppBar: RClass<AppBarProps>