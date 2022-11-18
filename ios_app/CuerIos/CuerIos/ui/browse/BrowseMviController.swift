//
//  BrowseMviController.swift
//  CuerIos
//
//  Created by Robert Munro on 18/11/2022.
//

import Foundation
import shared

protocol BrowseControllerDependency {
    func createBrowseHolder() -> BrowseControllerHolder
}

class BrowseControllerProvider: BrowseControllerHolder.Dependencies {
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

class BrowseControllerHolder : ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedFactoriesDependency
    
    private let dependencies: Dependencies
    let lifecycle: LifecycleLifecycleRegistry
    let controller: BrowseController

    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        lifecycle = dependencies.sharedFactories.orchestratorFactory.utils.lifecycleRegistry()
        controller = dependencies.sharedFactories.presentationFactory.browseControllerCreate(lifecycle: lifecycle)
        lifecycle.onCreate()
    }

    deinit {
        lifecycle.onDestroy()
    }
}

class BCStrings:BrowseContractStrings {
    func errorNoCatWithID(id: Int64) -> String {
        "errorNoCatWithID"
    }
    
    var allCatsTitle: String = "allCatsTitle"
    
    var errorNoPlaylistConfigured: String = "errorNoPlaylistConfigured"
    
    var recent: String = "errorNoPlaylistConfigured"
}

class BrowseViewProxy : UtilsUBaseView<BrowseContractViewModel, BrowseContractViewEvent>, BrowseContractView, ObservableObject {
    @Published
    var model: BrowseContractViewModel
    
    override init() {
        model = BrowseContractViewModel(
            title: "browsetitle",
            categories: [],
            recent: nil,
            isRoot: true,
            order: BrowseContract.Order.categories)
        super.init()
    }

    override func render(model: BrowseContractViewModel) {
        self.model = model
    }
    
    func processLabel(label: BrowseContractMviStoreLabel) {
        
    }
}
