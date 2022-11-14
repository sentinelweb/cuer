//
//  CuerIosApp.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

@main
struct CuerIosApp: App {
    let dependencies = AppDependencies()
    
    var body: some Scene {
        WindowGroup {
            MainView(mainCoordinator: self.dependencies.mainCoordinator)
        }
    }
}
