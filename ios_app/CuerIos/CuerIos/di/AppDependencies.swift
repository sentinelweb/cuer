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

protocol IosBuildConfigDependency {
    var buildConfig:DomainBuildConfigDomain {get}
}

protocol SharedDependency {
    var shared:SharedAppDependencies {get}
}

protocol SharedObjectsDependency {
    var orchestratorFactory: OrchestratorFactory {get}
}

protocol PlaylistIdDependency {var plId: Int { get }}
protocol PlaylistIdOptionalDependency {var plId: Int? { get }}

class AppDependencies:
    IosBuildConfigDependency
& MainCoordinatorDependency
& BrowseViewModelDependency
& PlaylistViewModelDependency
& PlaylistsViewModelDependency
& SharedDependency
& SharedObjectsDependency
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
    
    lazy var shared: SharedAppDependencies = SharedAppDependencies(
        config: self.buildConfig,
        ytApiKey: CuerYoutubeApiKeyProvider(),
        pixabayApiKey: CuerPixabayApiKeyProvider()
    )
    
    var orchestratorFactory = OrchestratorFactory()
    
    lazy var mainCoordinator: MainCoordinator = {MainCoordinator(dependencies: self)}()
    
    func createBrowseViewModel() -> BrowseViewModel {
        BrowseViewModel(
            dependencies: BrowseViewModelProvider(
                mainCoordinator: mainCoordinator,
                orchestratorFactory: orchestratorFactory
            )
        )
    }
    
    func createPlaylistViewModel(plId: Int) -> PlaylistViewModel {
        PlaylistViewModel(
            dependencies: PlaylistViewModelProvider(
                mainCoordinator: mainCoordinator,
                plId: plId,
                orchestratorFactory: orchestratorFactory
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
