import kotlinx.browser.document
import kotlinx.css.*
import react.dom.*
import styled.css
import styled.styledBody

fun main() {
    document.bgColor = "navy"
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