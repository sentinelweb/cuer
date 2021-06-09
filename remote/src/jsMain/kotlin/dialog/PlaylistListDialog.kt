package dialog

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.mRadioGroup
import com.ccfraser.muirwik.components.mRadioWithLabel
import react.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

external interface PlaylistListDialogProps : RProps {
    var isOpen: Boolean
    var playlists: List<PlaylistDomain>
    var close: () -> Unit
    var onPlayListSelected: (PlaylistDomain?) -> Unit
}

external interface PlaylistListDialogState : RState {
    var selectedValue: String
}

@JsExport
class PlaylistListDialog : RComponent<PlaylistListDialogProps, PlaylistListDialogState>() {

    override fun RBuilder.render() {
        mDialog(props.isOpen, scroll = DialogScroll.paper) {
            attrs.disableEscapeKeyDown = true
            mDialogTitle("Select Playlist")
            // We will show the dividers on one of the dialogs (i.e. the one with paper scroll)
            mDialogContent(dividers = true) {
                mRadioGroup(onChange = { _, value -> setSelection(value) }/*value = confirmationDialogValue,*/) {
                    props.playlists.forEach {
                        mRadioWithLabel(it.title, value = it.id?.toString())
                    }
                }
            }
            mDialogActions {
                mButton("Cancel", color = MColor.primary, onClick = { props.close() })
                mButton("Ok", color = MColor.primary, onClick = { fireSelected() })
            }
        }
    }

    private fun setSelection(value: String) {
        setState { selectedValue = value }
    }

    private fun fireSelected() {
        props.onPlayListSelected(props.playlists.find { it.id == state.selectedValue.toLong() })
    }
}

fun RBuilder.playlistListDialog(handler: PlaylistListDialogProps.() -> Unit) = child(PlaylistListDialog::class) { attrs(handler) }