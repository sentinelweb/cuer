package uk.co.sentinelweb.cuer.app.ui.support

import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Model


class SupportModelMapper {
    fun map(state: SupportContract.MviStore.State): Model =
        state.run {
            Model(
                title = media?.channelData?.title,
                links = links?.mapIndexed { i, link ->
                    Model.Link(
                        title = link.run {
                            if (!title.isNullOrEmpty()) {
                                "$title\n $address"
                            } else address
                        },
                        link = link.address,
                        index = i,
                        category = link.category,
                        domain = link
                    )
                }?.groupBy { it.category },
                isInitialised = links != null
            )
        }
}
