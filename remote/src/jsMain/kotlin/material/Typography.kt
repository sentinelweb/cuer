@file: JsModule("@material-ui/core/Typography")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface TypographyProps : RProps {
    var id: String?
    var color: String?// inherit, primary, secondary
    var variant: String?//"h1","h2"
}

@JsName("default")
external val Typography: RClass<TypographyProps>