package uk.co.sentinelweb.cuer.app.ui.common.itemlist

import org.koin.dsl.module
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemFactory

object ItemListModule {
    val listModule = module {
        factory { ItemFactory() } // todo remove ItemFactory
    }
}