package uk.co.sentinelweb.cuer.app.work

import android.content.Context
import androidx.work.*
import uk.co.sentinelweb.cuer.app.work.worker.UpcomingVideosCheckWorker
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.util.concurrent.TimeUnit

class WorkManagerInteractor(private val log: LogWrapper) {
    init {
        log.tag(this)
    }

    fun startUpcomingVideosChecker(c: Context) {
        log.d("startUpcomingVideosChecker")
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            UpcomingVideosCheckWorker::class.java,
            UpcomingVideosCheckWorker.MINS_CHECK.toLong(),
            TimeUnit.MINUTES
        )
            .build()

        WorkManager.getInstance(c).enqueueUniquePeriodicWork(
            UpcomingVideosCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // keeps existing scheduled already
            periodicWorkRequest
        )
    }

    fun checkStatus(c: Context, workName: String) {
        val instance = WorkManager.getInstance(c)
        val statuses = instance.getWorkInfos(WorkQuery.fromUniqueWorkNames(workName))
        val statusesGot = statuses.get()
        log.d("checking status($workName): ${statusesGot.size}")

        statusesGot.forEach {
            when (it.state) {
                WorkInfo.State.ENQUEUED -> {
                    log.d("Work ${it.id} is ENQUEUED")
                }

                WorkInfo.State.RUNNING -> {
                    log.d("Work ${it.id} is RUNNING")
                }

                WorkInfo.State.SUCCEEDED -> {
                    log.d("Work ${it.id} is SUCCEEDED")
                }

                WorkInfo.State.FAILED -> {
                    log.d("Work ${it.id} is FAILED")
                }

                WorkInfo.State.BLOCKED -> {
                    log.d("Work ${it.id} is BLOCKED")
                }

                WorkInfo.State.CANCELLED -> {
                    log.d("Work ${it.id} is CANCELLED")
                }
            }
        }
    }
}