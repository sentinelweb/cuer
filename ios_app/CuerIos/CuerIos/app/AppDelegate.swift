//
//  AppDelegate.swift
//  CuerIos
//
//  Created by Robert Munro on 15/11/2022.
//

import Foundation
import UIKit
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    let dependencies = AppDependencies()
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        KoinKt.doInitKoin(
            config: dependencies.buildConfig
        )
        let dbInit = OrchestratorFactory().databaseInitializer
        debugPrint("init: \(dbInit.isInitialized())")
        if !dbInit.isInitialized() {
            debugPrint("init db:start")
            dbInit.doInitDatabase(path: "default-dbinit")
            debugPrint("init db:done")
        }
        return true
    }
}
