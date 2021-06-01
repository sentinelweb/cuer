@file: JsModule("@material-ui/core/SwipeableDrawer")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface SwipeableDrawerProps : RProps {
    var id: String?
    var checked: Boolean?
    var anchor: String?
    var open: Boolean?
}

@JsName("default")
external val SwipeableDrawer: RClass<SwipeableDrawerProps>
