package uk.co.sentinelweb.klink.ui.common.itemlist.item

import androidx.annotation.DrawableRes

// todo make generic
open class ItemModel(
    val id: String?,
    val topText: String,
    val bottomText: String,
    val checkIcon: Boolean,
    @DrawableRes val iconRes: Int
//    val count: String,
//    val age: String,
//    val ageSinceLastSeen: String,
//    val statusCode: String,
//    val checkStatus: String,
//    val title: String?,
//    val type: Type,
//    val checkIcon: Boolean,
//    @DrawableRes val iconRes:Int,
//    val id:String?
    )
