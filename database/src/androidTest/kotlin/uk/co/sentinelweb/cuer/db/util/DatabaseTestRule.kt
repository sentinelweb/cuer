package uk.co.sentinelweb.cuer.db.util

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.db.di.DatabaseCommonModule
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class DatabaseTestRule : TestWatcher(), KoinComponent {

    private val driverModule = module {
        single<SqlDriver> { JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY) }
    }

    private val dbTestModule = module {
        factory<LogWrapper> { SystemLogWrapper() }
    }

    private val utilTestModule = module {
        factory { TimeProvider() }
        factory { GuidCreator() }
    }

    val modules = listOf(driverModule, dbTestModule, utilTestModule)
        .plus(DatabaseCommonModule.modules)

    override fun starting(description: Description) {
        super.starting(description)
    }

    override fun finished(description: Description) {
        super.finished(description)
    }
}
