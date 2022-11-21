//
//  PlayListViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import Combine
import shared

protocol PlaylistViewModelDependency {
    func createPlaylistViewModel(plId: Int) -> PlaylistViewModel
}

class PlaylistViewModelProvider: PlaylistViewModel.Dependencies {
    let mainCoordinator: MainCoordinator
    let sharedFactories: SharedFactories
    let plId: Int
    
    init(
        mainCoordinator: MainCoordinator,
        plId: Int,
        sharedFactories: SharedFactories
    ) {
        self.mainCoordinator = mainCoordinator
        self.plId = plId
        self.sharedFactories = sharedFactories
    }
}

final class PlaylistViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & PlaylistIdDependency & SharedFactoriesDependency
    let dependencies: Dependencies
    
    let playlist: DomainPlaylistDomain? = nil
    
    private let orchestrator: OrchestratorFactory
    private let filter: ProxyFilter
    
    private var playlistIdSubscription: AnyCancellable? = nil
    
    @Published var plId:Int = -1
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        self.orchestrator = dependencies.sharedFactories.orchestratorFactory
        self.filter = dependencies.sharedFactories.orchestratorFactory.proxyFilter
        playlistIdSubscription = dependencies.mainCoordinator.$currentPlaylistId.sink(receiveValue:{plId in
            if (self.plId != plId) {
                self.plId = plId
                if (plId > 0) {
                    self.loadPlaylist(plId: plId)
                }
            }
        })
    }
    
    func loadPlaylist(plId: Int) {
//        let task = Task{
            do {
                orchestrator.playlistOrchestrator.count(
                    filter: filter.allFilter(),
                    options: DomainOrchestratorContractOptions(source: DomainOrchestratorContractSource.local, flat: true, emit: false)
                ) { count, e in
                    print("loadPlaylist count: \(count)")
                }
            } catch {
                print("loadPlaylist error: \(error)")
            }
//        }
    }
    
    deinit{
        playlistIdSubscription?.cancel()
    }
}
