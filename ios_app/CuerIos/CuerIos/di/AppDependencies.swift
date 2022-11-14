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

protocol PlaylistIdDependency {var plId: Int { get }}
protocol PlaylistIdOptionalDependency {var plId: Int? { get }}

class AppDependencies: IosBuildConfigDependency
& MainCoordinatorDependency
& BrowseViewModelDependency
& PlaylistViewModelDependency
& PlaylistsViewModelDependency
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
    
    func createPlaylistViewModel(plId: Int) -> PlaylistViewModel {
        PlaylistViewModel(
            dependencies: PlaylistViewModelProvider(
                mainCoordinator: mainCoordinator,
                plId: plId
            )
        )
    }
    
    func createPlaylistsViewModel() -> PlaylistsViewModel {
        PlaylistsViewModel(
            dependencies: PlaylistsViewModelProvider(
                mainCoordinator: mainCoordinator
            )
        )
    }
    
}