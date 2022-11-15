//
//  CuerIosApp.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

@main
struct CuerIosApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            MainView(mainCoordinator: self.appDelegate.dependencies.mainCoordinator)
        }
    }
}
