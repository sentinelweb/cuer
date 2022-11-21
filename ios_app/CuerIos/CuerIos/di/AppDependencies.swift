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

// todo make build info plist: https://stackoverflow.com/questions/6851660/version-vs-build-in-xcode
protocol IosBuildConfigDependency {
    var buildConfig:DomainBuildConfigDomain {get}
}

protocol SharedDependency {
    var shared:SharedAppDependencies {get}
}

protocol SharedFactoriesDependency {
    var sharedFactories: SharedFactories {get}
}

class SharedFactories {
    let orchestratorFactory = OrchestratorFactory()
    let presentationFactory = PresentationFactory()
}

protocol PlaylistIdDependency {var plId: Int { get }}
protocol PlaylistIdOptionalDependency {var plId: Int? { get }}

class AppDependencies:
    IosBuildConfigDependency
& MainCoordinatorDependency
& BrowseControllerDependency
& PlaylistViewModelDependency
& PlaylistsViewModelDependency
& SharedDependency
& SharedFactoriesDependency
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
    
    var sharedFactories = SharedFactories()
    
    lazy var mainCoordinator: MainCoordinator = {MainCoordinator(dependencies: self)}()

    func createBrowseHolder() -> BrowseControllerHolder {
        BrowseControllerHolder(dependencies: BrowseControllerProvider(
            mainCoordinator: mainCoordinator,
            sharedFactories: sharedFactories
        ))
    }
    
    func createPlaylistViewModel(plId: Int) -> PlaylistViewModel {
        PlaylistViewModel(
            dependencies: PlaylistViewModelProvider(
                mainCoordinator: mainCoordinator,
                plId: plId,
                sharedFactories: sharedFactories
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
