
import kotlinx.browser.document
import kotlinx.css.*
import react.dom.render
import styled.css
import styled.styledBody

fun main() {
    document.bgColor = "white"
    render(document.getElementById("root")) {
        styledBody {
            css {
                fontFamily = "sans-serif"
                color = Color.black
                display = Display.grid
                //gridTemplateColumns = GridTemplateColumns.auto GridTemplateColumns.auto,GridTemplateColumns.auto
                put("grid-template-columns", "220px 300px 1fr")
                put("grid-template-rows", "30px 1fr")
                put("grid-gap", "1em")
                put("grid-template-areas", "'banner banner banner' 'playlists playlist item'")
            }
            child(App::class) {}
        }
    }

}