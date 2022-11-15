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
    let plId: Int
    
    init(mainCoordinator: MainCoordinator, plId: Int) {
        self.mainCoordinator = mainCoordinator
        self.plId = plId
    }
}

final class PlaylistViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & PlaylistIdDependency
    let dependencies: Dependencies
    let playlist: DomainPlaylistDomain? = nil
    
//    private let log = SystemLogWrapper() // fixme: not available?
    private let orch = OrchestratorFactory()
    
    private var playlistIdSubscription: AnyCancellable? = nil
    
    @Published var plId:Int = -1
    
    init(dependencies: Dependencies) {
//        log.tag="PlaylistViewModel"
//        log.d(msg: "init start")
        self.dependencies = dependencies
        playlistIdSubscription = dependencies.mainCoordinator.$currentPlaylistId.sink(receiveValue:{plId in
            if (self.plId != plId) {
                self.plId = plId
                if (plId > 0) {
                    self.loadPlaylist(plId: plId)
                }
            }
//            self.log.d(msg: "got playlistid: \(plId)")
        })
//        log.d(msg: "init complete")
    }
    
    func loadPlaylist(plId: Int) {
//        let task = Task{
//            do {
                let allFilter=ProxyFilter().allFilter()
            orch.playlistOrchestrator.count(
                filter: allFilter,// wont match anything
                options:DomainOrchestratorContractOptions(source: DomainOrchestratorContractSource.local, flat: true, emit: false)
            ) { count, e in
                print("loadPlaylist count: \(count)")
            }
        
//    } catch {
//                print("loadPlaylist error: \(error)")
//            }
//        }
    }
    
    deinit{
        playlistIdSubscription?.cancel()
    }
}
