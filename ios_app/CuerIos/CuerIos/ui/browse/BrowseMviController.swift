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
    
    func createMviView() -> BrowseViewProxy { BrowseViewProxy(dependencies:self.dependencies)}
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
    
    private let dependencies: BrowseControllerHolder.Dependencies
//    init(dependencies: BrowseControllerHolder.Dependencies) {
//        self.dependencies = dependencies
//    }
    
    init(dependencies: BrowseControllerHolder.Dependencies)  {
        model = BrowseContractViewModel(
            title: "browsetitle",
            categories: [],
            recent: nil,
            isRoot: true,
            order: BrowseContract.Order.categories)
        self.dependencies = dependencies
        super.init()
    }

    override func render(model: BrowseContractViewModel) {
        self.model = model
    }
    
    func processLabel(label: BrowseContractMviStoreLabel) {
        switch(label) {
        case is BrowseContractMviStoreLabel.AddPlaylist:
            let addLabel = label as! BrowseContractMviStoreLabel.AddPlaylist
//            let str:String = playlistUrl(platformId: addLabel.cat.platformId)
            let str = "https://www.youtube.com/playlist?list=" + addLabel.cat.platformId!
            self.dependencies.mainCoordinator.open(URL.init(string: str)!)
        default: debugPrint(label)
        }
    }
}
