//
//  PlaylistsMviController.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import Foundation
import shared

protocol PlaylistsControllerDependency {
    func createPlaylistsHolder() -> PlaylistsMviControllerHolder
}

class PlaylistsControllerProvider: PlaylistsMviControllerHolder.Dependencies {
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

class PlaylistsMviControllerHolder : ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedFactoriesDependency
    
    private let dependencies: Dependencies
    let lifecycle: LifecycleLifecycleRegistry
    let controller: PlaylistsMviController

    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        lifecycle = dependencies.sharedFactories.orchestratorFactory.utils.lifecycleRegistry()
        controller = dependencies.sharedFactories.presentationFactory.playlistsController(lifecycle: lifecycle)
        lifecycle.onCreate()
    }

    deinit {
        lifecycle.onDestroy()
    }
    
    func createMviView() -> PlaylistsMviViewProxy { PlaylistsMviViewProxy(dependencies: self.dependencies)}
}

class PlaylistsStrings:PlaylistsMviContract.Strings {
    
}

class PlaylistsMviViewProxy : UtilsUBaseView<PlaylistsMviContractViewModel, PlaylistsMviContractViewEvent>, PlaylistsMviContractView, ObservableObject {
    @Published
    var model: PlaylistsMviContractViewModel
    
    private let dependencies: PlaylistsMviControllerHolder.Dependencies
//    init(dependencies: PlaylistsControllerHolder.Dependencies) {
//        self.dependencies = dependencies
//    }
    
    init(dependencies: PlaylistsMviControllerHolder.Dependencies)  {
        model = PlaylistsMviContractViewModel(
            title:"initial",
            imageUrl:"https://cuer-275020.firebaseapp.com/images/headers/headphones-2588235_640.jpg",
            currentPlaylistId: DomainOrchestratorContractIdentifier.init(id: 1 as AnyObject, source: .local),
            items:[]
        )
        self.dependencies = dependencies
        super.init()
    }

    override func render(model: PlaylistsMviContractViewModel) {
        self.model = model
    }
    
    func processLabel(label_: PlaylistsMviContractMviStoreLabel) {
        switch(label_) {
        case is PlaylistsMviContractMviStoreLabel.Message:
            let messageLabel = label_ as! PlaylistsMviContractMviStoreLabel.Message
            debugPrint("Message: \(messageLabel.message))")
            // todo show message
        default: debugPrint(label_)
        }
    }
}
