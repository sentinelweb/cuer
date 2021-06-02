import kotlinx.css.Visibility
import kotlinx.css.visibility
import material.AppBar
import material.LinearProgress
import material.Toolbar
import material.Typography
import react.*
import styled.css
import styled.styledDiv

class Banner : RComponent<BannerProps, BannerState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "banner")
            }
            AppBar {
                attrs {
                    color = "secondary"
                }
                Toolbar {
                    Typography {
                        attrs {
                            variant = "h4"
                        }
                        +props.title
                    }

                }
                styledDiv {
                    css {
                        visibility = props.loading
                    }
                    LinearProgress
                }
            }

//
//                    TextField {
//                        attrs {
//                            id = "urlLink"
//                            required = true
//                            helperText = "Help me..."
//                            error = state.checkBoxChecked
//                        }
//                    }
//                    Checkbox {
//                        attrs {
//                            id = "rememberMe"
//                            checked = state.checkBoxChecked
//                            onChange = {
//                                setState {
//                                    checkBoxChecked = !checkBoxChecked
//                                }
//                            }
//                        }
//                    }
//                    label {
//                        attrs["htmlFor"] = "rememberMe"
//                        +"Checkbox"
//                    }

//                SwipeableDrawer{
//                    attrs {
//                        anchor = "left"
//                        open = state.checkBoxChecked
//                    }
//                    +"Some content"
//                }
//            }

        }
    }

    override fun BannerState.init() {
        checkBoxChecked = false
    }
}

external interface BannerProps : RProps {
    var title: String
    var loading: Visibility
}

external interface BannerState : RState {
    var checkBoxChecked: Boolean
}

fun RBuilder.banner(handler: BannerProps.() -> Unit): ReactElement {
    return child(Banner::class) {
        this.attrs(handler)
    }
}