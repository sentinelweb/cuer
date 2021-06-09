package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory

object AppSharedModule {
    val objectModule = module {
        factory { ParserFactory() }
    }
}