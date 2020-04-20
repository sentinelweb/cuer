package uk.co.sentinelweb.cuer.app.ui.common.itemlist.item

// todo make reusable with generics
interface ItemDomainToModelMapper<From, To> {
    fun mapItemFiltered(link: From, filter: (i: From) -> Boolean): To?
    fun mapItem(link: From, id:String?): To
}
//class ItemDomainToModelMapper {
//
//    fun mapItemFiltered(link: ItemDomain, filter: (i: ItemDomain) -> Boolean): ItemModel? {
//        if (filter.invoke(link)) {
//            return mapItem(link, null)
//        }
//        return null
//    }
//
//    fun mapItem(link: ItemDomain, id:String?): ItemModel {
//        val age = ((SystemClock.uptimeMillis() - link.firstDetected) / 1000).toInt()
//        val iconRes = when (link.type) {
//            Type.LINK -> R.drawable.ic_link_black_48dp
//            Type.EMAIL -> R.drawable.ic_mail_black_24dp
//            Type.TWITTER_ADDRESS -> R.drawable.ic_cloud_black_24dp
//            Type.TWITTER_TOPIC -> R.drawable.ic_cloud_black_24dp // TODO make new icon
//        }
//        return ItemModel(
//                link.urlString,
//                "${link.count}",
//                "${age}s",
//                "${link.ageLastSeen(SystemClock.uptimeMillis())}s",
//                "${link.statusCode}",
//                link.checkStatus.toString(),
//                link.title,
//                link.type,
//                link.checkStatus == CheckStatus.CHECKED,
//                iconRes,
//                id
//        )
//    }
//}
