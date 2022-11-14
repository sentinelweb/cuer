//
//  PlaylistsViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import shared

protocol PlaylistsViewModelDependency {
    func createPlaylistsViewModel() -> PlaylistsViewModel
}

class PlaylistsViewModelProvider: PlaylistsViewModel.Dependencies {
    let mainCoordinator: MainCoordinator
    
    init(mainCoordinator: MainCoordinator) {
        self.mainCoordinator = mainCoordinator
    }
}

final class PlaylistsViewModel: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency
    let dependencies: Dependencies

    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
    }
    
    func navigateToPlaylist(id: Int) {
        self.dependencies.mainCoordinator.navigate(route: Route.playlist(plId: id))
    }
}
