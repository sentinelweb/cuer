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
    let playlist: PlaylistDomain? = nil
    
    private var playlistIdSubscription: AnyCancellable? = nil
    
    @Published var plId:Int = -1
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        playlistIdSubscription = dependencies.mainCoordinator.$currentPlaylistId.sink(receiveValue:{plId in
            self.plId = plId
        })
    }
    
    deinit{
        playlistIdSubscription?.cancel()
    }
}
