//
//  AppDependencies.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

import shared
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
#if DEBUG
    private let isDebug = true
#else
    private let isDebug = false
#endif
    lazy var buildConfig: DomainBuildConfigDomain = DomainBuildConfigDomain(
        isDebug: isDebug,
        versionCode: 1,
        version: "0.77")
    
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
