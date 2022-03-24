package uk.co.sentinelweb.cuer.app.ui.support


class SupportModelMapper {
    fun map(state: SupportContract.MviStore.State): SupportContract.View.Model =
        state.run {
            SupportContract.View.Model(
                links = listOf(
                    SupportContract.View.Model.Link(
                        title = "title",
                        link = "link"
                    )
                )
            )
        }
}