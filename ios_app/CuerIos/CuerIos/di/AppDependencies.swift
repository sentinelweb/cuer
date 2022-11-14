//
//  AppDependencies.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

//import shared
//
//protocol xxxExecutorDependency {
//  var xxxExecutor: UseCaseExecutor { get }
//}

protocol PlaylistIdDependency {var batchId: String { get }}
protocol PlaylistIdOptionalDependency {var batchId: String? { get }}

class AppDependencies: IosBuildConfigDependency
& MainCoordinatorDependency
& BrowseViewModelDependency
{
    lazy var buildConfig: IosBuildConfig = {IosBuildConfig()}()
    
    lazy var mainCoordinator: MainCoordinator = {MainCoordinator(dependencies: self)}()
    
    func createBrowseViewModel() -> BrowseViewModel {
        BrowseViewModel(
            dependencies: BrowseViewModelProvider(
                mainCoordinator: mainCoordinator
            )
        )
    }
    
}
