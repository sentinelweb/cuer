package uk.co.sentinelweb.cuer.app.work.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class UpcomingVideosCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params), KoinComponent {

    private val upcomingPresenter: UpcomingContract.Presenter by inject()
    private val log: LogWrapper by inject()

    init {
        log.tag(this)
    }

    override fun doWork(): Result =
        try {
            upcomingPresenter.checkForUpcomingEpisodes(MINS_CHECK)
            Result.success()
        } catch (e: Exception) {
            log.e("Upcoming check failed", e)
            Result.failure()
        }


    companion object {
        val WORK_NAME = "UpcomingVideosCheck"
        val MINS_CHECK = 30
    }
}