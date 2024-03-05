//
//  WorkManagerLauncher.swift
//  CuerIos
//
//  Created by rob munro on 02/03/2024.
//

import Foundation
import BackgroundTasks


// NOTE: need a real device to test this - simulator won't run bg tasks
// ideally should be analogus to android class of same name: WorkManagerLauncher
class WorkManagerInteractor {
    
    static let WM_UPCOMING_TASK_ID = "UpcomingVideosCheck"

    let dependencies: AppDependencies
    
    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
    }
    // from this video: https://www.youtube.com/watch?v=Lb7OShyNSdM
    // register background task handler
    // todo extract work id to a structure
    
    
    // resigters the task on the system - call from Appdelegate.application(...) - MUST run before start
    public func registerUpcomingTask(taskId: String) {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskId, using: nil, launchHandler: { task in
            debugPrint("upcoming task handler")
            // can also use BGProcessingTask() - aslo change below
            guard let task = task as? BGProcessingTask else {return}
            self.handleUpcomingTask(task: task)
        })
    }
    
    private func handleUpcomingTask(task: BGProcessingTask) {
        // this section actually handles the task so call UpcomingPresenter here
        debugPrint("handleUpcomingTask")
        let result = self.dependencies.sharedFactories
            .orchestratorFactory.upcomingPresenter.checkForUpcomingEpisodes(withinFutureMins: 30)
        task.setTaskCompleted(success: true)
        // fixme do I need this or otherwise schedule a periodic tsak somehow
        //self.scheduleUpcomingTaskDelayed(time: 30.0)?
    }
    
    func scheduleUpcomingTaskDelayed(taskId: String, timeSeconds: Double) {
        DispatchQueue.main.asyncAfter(deadline: .now() + timeSeconds) {
            self.scheduleUpcomingTask(taskId: taskId)
            debugPrint("scheduled: " + WorkManagerInteractor.WM_UPCOMING_TASK_ID)
            debugPrint("continued: " + WorkManagerInteractor.WM_UPCOMING_TASK_ID)
        }
    }
    
    // schedules the task
    func scheduleUpcomingTask(taskId: String) {
        do {
            let newTask = BGProcessingTaskRequest(identifier: taskId)
            newTask.requiresNetworkConnectivity = true
            try BGTaskScheduler.shared.submit(newTask)
        } catch {
            debugPrint("schedule failed")
        }
        debugPrint("upcoming scheduled")
    }
}
