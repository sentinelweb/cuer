//
//  BrowseViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import shared

protocol BrowseViewModelDependency {
    func createBrowseViewModel() -> BrowseViewModel
}

class BrowseViewModelProvider: BrowseViewModel.Dependencies {
    let mainCoordinator: MainCoordinator
    let orchestratorFactory: OrchestratorFactory
    
    init(
        mainCoordinator: MainCoordinator,
        orchestratorFactory: OrchestratorFactory
    ) {
        self.mainCoordinator = mainCoordinator
        self.orchestratorFactory = orchestratorFactory
    }
}

final class BrowseViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedObjectsDependency
    private let dependencies: Dependencies
    
    private let orchestrator: OrchestratorFactory
    private let filter: ProxyFilter
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        self.orchestrator = dependencies.orchestratorFactory
        self.filter = dependencies.orchestratorFactory.proxyFilter
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
