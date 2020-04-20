package uk.co.sentinelweb.cuer.app.ui.common.itemlist.item

class ItemPresenter (
        val view: ItemContract.View,
        val interactions: ItemContract.Interactions,
        val state: ItemState
) : ItemContract.Presenter {

    override fun update(item: ItemModel) {
        view.setTopText(item.topText)
        view.setBottomText(item.bottomText)
        view.setCheckedVisible(item.checkIcon)
        view.setIconResource(item.iconRes)
        state.item = item
    }

    override fun doClick() {
        interactions.onClick(state.item!!)
    }

    override fun doLeft() {
        interactions.onLeftSwipe(state.item!!)
    }

    override fun doRight() {
        interactions.onRightSwipe(state.item!!)
    }

}
