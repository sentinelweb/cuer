
import kotlinx.browser.document
import kotlinx.css.Color
import kotlinx.css.background
import kotlinx.css.color
import kotlinx.css.fontFamily
import react.dom.render
import styled.css
import styled.styledBody

fun main() {
    document.bgColor = "grey"
    render(document.getElementById("root")) {
        styledBody {
            css {
                fontFamily = "sans-serif"
                color = Color.white
            }
            child(App::class) {}
        }
    }

}