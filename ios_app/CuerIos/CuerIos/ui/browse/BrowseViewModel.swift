//
//  BrowseViewModel.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

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
    
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
    }
}
