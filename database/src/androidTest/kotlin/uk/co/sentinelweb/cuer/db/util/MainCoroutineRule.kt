package uk.co.sentinelweb.cuer.db.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider

class MainCoroutineRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    private val dispatcherModule = module {
        single<CoroutineDispatcher> { testDispatcher }
        single<CoroutineContextProvider> { TestCoroutineContextProvider(testDispatcher) }
    }
    val modules = listOf(dispatcherModule)

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
