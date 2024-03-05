//
//  AppDependencies.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import shared
import SwiftUI
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
& PlaylistsControllerDependency
& PlaylistControllerDependency
& SharedDependency
& SharedFactoriesDependency
& PlaylistsDialogDependency
{
    
    
#if DEBUG
    private let isDebug = true
#else
    private let isDebug = false
#endif
    lazy var buildConfig: DomainBuildConfigDomain = DomainBuildConfigDomain(
        isDebug: isDebug,
        cuerRemoteEnabled: false,
        versionCode: 1,
        version: "0.77",
        device: "Ios",
        deviceType:DomainNodeDomain.DeviceType.ios
    )
    
    lazy var shared: SharedAppDependencies = SharedAppDependencies(
        config: self.buildConfig,
        ytApiKey: CuerYoutubeApiKeyProvider(),
        pixabayApiKey: CuerPixabayApiKeyProvider(),
        shareWrapper: {IosShareWrapper(mainCoordinator: mainCoordinator)}(),
        platformLaunchWrapper: {IosPlatformLauncher(mainCoordinator: mainCoordinator)}(),
        upcomingView: {UCV()}()
    )
    
    class UCV:UpcomingContractView {
        func showNotification(item: DomainPlaylistItemDomain) {
            debugPrint("showNotification: \(item.media.title ?? "No title")")
        }
        
        
    }
    
    // todo lazy create
    var sharedFactories = SharedFactories()
    lazy var workManager = WorkManagerInteractor(dependencies: self)
    
    lazy var mainCoordinator: MainCoordinator = {MainCoordinator(dependencies: self)}()

    func createBrowseHolder() -> BrowseControllerHolder {
        BrowseControllerHolder(dependencies: BrowseControllerProvider(
            mainCoordinator: mainCoordinator,
            sharedFactories: sharedFactories
        ))
    }
    
    func createPlaylistsHolder() -> PlaylistsMviControllerHolder {
        PlaylistsMviControllerHolder(dependencies: PlaylistsControllerProvider(
            mainCoordinator: mainCoordinator,
            sharedFactories: sharedFactories
        ))
    }

    
    func createPlaylistHolder() -> PlaylistMviControllerHolder {
        PlaylistMviControllerHolder(dependencies: PlaylistControllerProvider(
            mainCoordinator: mainCoordinator,
            sharedFactories: sharedFactories
        ))
    }
    
    func createPlaylistsDialogHolder(config: PlaylistsMviDialogContractConfig) -> PlaylistsDialogViewModelHolder {
        PlaylistsDialogViewModelHolder(dependencies: PlaylistsDialogProvider(
            mainCoordinator: mainCoordinator,
            viewModel: sharedFactories.presentationFactory.playlistsDialogViewModel(),
            config: config
        ))
    }
    
}
