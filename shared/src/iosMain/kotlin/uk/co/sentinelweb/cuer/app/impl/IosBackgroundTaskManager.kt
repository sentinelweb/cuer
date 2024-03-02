package uk.co.sentinelweb.cuer.app.impl

import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGTaskScheduler

// test for launching / scheduling bg tasks on iOS
class IosBackgroundTaskManager {
    fun registerBackgroundTask(workName: String) {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = workName,
            usingQueue = null
        ) { task ->
            task as BGAppRefreshTask
        }
    }

    fun submitTask(workName: String) {
        val newTask = BGAppRefreshTask() // doesnt have identifier param? library is old?
        //newTask.
    }
}