package uk.co.sentinelweb.cuer.app.ui.common.interfaces

interface ActionBarModifier {
    fun setMenuItemColor(colorStateListId: Int)
}

class EmptyActionBarModifier : ActionBarModifier {
    override fun setMenuItemColor(colorStateListId: Int) = Unit
}