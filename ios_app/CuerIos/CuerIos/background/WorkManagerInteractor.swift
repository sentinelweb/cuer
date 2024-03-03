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
    // from this video: https://www.youtube.com/watch?v=Lb7OShyNSdM
    // register background task handler
    // todo extract work id to a structure
    
    let upcomingTaskId = "UpcomingVideosCheck"
    
    // resigters the task on the system - call from Appdelegate.application(...) - MUST run before start
    public func registerUpcomingTask(taskId: String) {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: taskId, using: nil, launchHandler: { task in
            debugPrint("upcoming task handler")
            // can also use BGProcessingTask() - aslo change below
            guard let task = task as? BGAppRefreshTask else {return}
            self.handleUpcomingTask(task: task)
        })
    }
    
    private func handleUpcomingTask(task: BGAppRefreshTask) {
        // this section actually handles the task so call UpcomingPresenter here
        debugPrint("handleUpcomingTask")
        task.setTaskCompleted(success: true)
    }
    
    // schedules the task
    private func scheduleUpcomingTask(taskId: String) {
        do {
            let newTask = BGAppRefreshTaskRequest(identifier: taskId)
            try BGTaskScheduler.shared.submit(newTask)
        } catch {
            
        }
        debugPrint("upcoming scheduled")
    }
}
