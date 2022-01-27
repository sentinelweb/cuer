
//import kotlinext.js.jsObject
import Content.ComponentStyles.drawer
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import dialog.addPlaylistDialog
import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.ms
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

external interface ContentProps : Props {
    var title: String
    var loading: Visibility
    var playlists: List<PlaylistDomain>
    var onSelectPlaylist: (PlaylistDomain) -> Unit
    var currentPlaylist: PlaylistDomain?
}

external interface ContentState : State {
    var checkBoxChecked: Boolean
    var currentItem: PlaylistItemDomain?
}

class Content : RComponent<ContentProps, ContentState>() {
    private val drawerWidth = 340
    private var drawerOpen: Boolean = false
    private var formDialogOpen: Boolean = false

    private object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
        val drawer by css {
            width = 100.pct
            height = 100.pct
            overflow = Overflow.hidden
            position = Position.relative
            display = Display.flex
        }
    }

    override fun RBuilder.render() {
        themeContext.Consumer { theme ->
            styledDiv {
                css(drawer)

                mAppBar(position = MAppBarPosition.absolute) {
                    css {
                        position = Position.absolute
                        transition += Transition("width", 195.ms, Timing.easeInOut, 0.ms)
                        if (drawerOpen) width = 100.pct - drawerWidth.px
                    }

                    mToolbar(disableGutters = !drawerOpen) {
                        if (!drawerOpen) {
                            mIconButton("menu", color = MColor.inherit, onClick = {
                                setState { drawerOpen = true }
                            })
                        }
                        mToolbarTitle(props.title)
                        mIconButton(
                            "add",
                            color = MColor.inherit,
                            onClick = { setState { formDialogOpen = true } })
                    }
                    styledDiv {
                        css {
                            visibility = props.loading
                        }
                        mLinearProgress(color = MLinearProgressColor.secondary)
                    }
                }
// todo find another way to do this for kotlin 1.6
//                val pp: MPaperProps = jsObject { }
//                pp.asDynamic().style = js { position = "relative" }
                mDrawer(
                    drawerOpen,
                    MDrawerAnchor.left,
                    MDrawerVariant.persistent/*, paperProps = pp*/
                ) {
                    css {
                        transition += Transition("left", 5000.ms, Timing.easeInOut, 0.ms)
                        width = drawerWidth.px
                    }
                    styledDiv {
                        css {
                            display = Display.flex
                            alignItems = Align.center
                            justifyContent = JustifyContent.flexEnd
                            toolbarJsCssToPartialCss(theme.mixins.toolbar)
                        }
                        mIconButton("chevron_left", onClick = { setState { drawerOpen = false } })
                    }
                    mDivider()
                    playlistList {
                        playlists = props.playlists
                        onSelectPlaylist = {
                            props.onSelectPlaylist(it)
                            setState { drawerOpen = false }
                        }
                    }
                }

                styledDiv {
                    css {
                        flexGrow = 1.0
                        marginLeft = -drawerWidth.px
                        display = Display.grid
                        put("grid-template-columns", "400px 1fr")
                        put("grid-template-rows", "1fr")
                        put("grid-gap", "1em")
                        put("grid-template-areas", "'playlist item'")
                        marginTop = 60.px
                    }
                    playlist {
                        title = props.currentPlaylist?.title ?: "No playlist"
                        playlist = props.currentPlaylist
                        selectedItem = state.currentItem
                        onSelectItem = { item ->
                            setState {
                                currentItem = item
                            }
                        }
                    }

                    state.currentItem?.let { item ->
                        playlistItem {
                            video = item.media
                            unwatchedVideo = item.media.watched
                            onWatchedButtonPressed = {
                                setState {
                                    currentItem = currentItem?.let { it.copy(media = it.media.copy(watched = !it.media.watched)) }
                                }
                            }
                        }
                    }
                }
                addPlaylistDialog {
                    isOpen = formDialogOpen
                    close = { setState { formDialogOpen = false } }
                    playlists = props.playlists
                }
            }
        }
    }

    override fun ContentState.init() {
        checkBoxChecked = false
        currentItem = null
    }

}

fun RBuilder.content(handler: ContentProps.() -> Unit) = child(Content::class) { attrs(handler) }

