//
//  PlayListViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

protocol PlaylistViewModelDependency {
    func createPlaylistViewModel() -> PlaylistViewModel
}

class PlaylistViewModelProvider: PlaylistViewModel.Dependencies {
    let mainCoordinator: MainCoordinator
    init(mainCoordinator: MainCoordinator) {
        self.mainCoordinator = mainCoordinator
    }
}

final class PlaylistViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency
    let dependencies: Dependencies
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
    }
}
