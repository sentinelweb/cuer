//
//  BrowseViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import shared
import Combine

protocol BrowseViewModelDependency {
    func createBrowseViewModel() -> BrowseViewModel
}

class BrowseViewModelProvider: BrowseViewModel.Dependencies {
    let mainCoordinator: MainCoordinator
    let sharedFactories: SharedFactories
    
    init(
        mainCoordinator: MainCoordinator,
        sharedFactories: SharedFactories
    ) {
        self.mainCoordinator = mainCoordinator
        self.sharedFactories = sharedFactories
    }
}

final class BrowseViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedFactoriesDependency
    private let dependencies: Dependencies
    
    private let orchestrator: OrchestratorFactory
    private let filter: ProxyFilter
//    private let browseController: BrowseController
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        self.orchestrator = dependencies.sharedFactories.orchestratorFactory
        self.filter = dependencies.sharedFactories.orchestratorFactory.proxyFilter
    }
    
    func execPlatformRequest() {
        do {
            let platformIdListFilter = filter.platformIdListFilter(ids: ["PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR"], platform: DomainPlatformDomain.youtube)
            
            orchestrator.playlistOrchestrator.loadByPlatformId(
                platformId: "PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR",//"PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR",
                options:DomainOrchestratorContractOptions(source: DomainOrchestratorContractSource.platform, flat: false, emit: false)
            ) { playlist, e in
                print("loadPlaylistPlatform count: \(playlist)")
            }
            
       } catch {
            print("loadPlaylist error: \(error)")
        }
    }
}
