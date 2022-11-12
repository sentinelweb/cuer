package uk.co.sentinelweb.cuer.app.ui.common.ktx

import android.view.Menu
import androidx.core.view.forEach
import org.koin.core.context.GlobalContext.get
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

fun Menu.setMenuItemsColor(cslRes: Int) {
    val colorStateList = get().get<ResourceWrapper>().getColorStateList(cslRes)
    this.forEach { item -> item.iconTintList = colorStateList }
}
