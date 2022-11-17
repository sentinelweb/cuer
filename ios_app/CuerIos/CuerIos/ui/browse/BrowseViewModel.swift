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
    init(mainCoordinator: MainCoordinator) {
        self.mainCoordinator = mainCoordinator
    }
}

final class BrowseViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency
    let dependencies: Dependencies
    
    private let orch = OrchestratorFactory() // todo inject
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
    }
    
    func execPlatformRequest() {
        do {
            let platformIdListFilter=ProxyFilter().platformIdListFilter(ids: ["PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR"], platform: DomainPlatformDomain.youtube)
            orch.playlistOrchestrator.loadPlatform(
                platformId: "PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR",//"PLf-zrdqNE8p9qjU-kzB8ROMpgbXYahCQR",
                options:DomainOrchestratorContractOptions(source: DomainOrchestratorContractSource.platform, flat: false, emit: false)
            ) { playlist, e in
                print("loadPlaylistPlatform count: \(playlist)")
            }
            let fullNoEmitOptions = DomainOrchestratorContractOptions(source: DomainOrchestratorContractSource.local, flat: false, emit: false)
            //orch.playlistOrchestrator.load(id: Int64(plId), options: fullNoEmitOptions)
        } catch {
            print("loadPlaylist error: \(error)")
        }
    }
}
