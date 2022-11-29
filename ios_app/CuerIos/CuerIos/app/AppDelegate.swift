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
            dependencies: dependencies.shared
        )
        let dbInit = OrchestratorFactory().databaseInitializer
        if !dbInit.isInitialized() {
            dbInit.doInitDatabase(path: "default-dbinit")
        }
        return true
    }
}