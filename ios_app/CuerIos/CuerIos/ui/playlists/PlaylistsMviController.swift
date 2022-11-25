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

class PlaylistsStrings: PlaylistsMviContract.Strings {
    
}

class PlaylistsMviViewProxy : UtilsUBaseView<PlaylistsMviContractViewModel, PlaylistsMviContractViewEvent>, PlaylistsMviContractView, ObservableObject {
    @Published
    var model: PlaylistsMviContractViewModel
    
    private let dependencies: PlaylistsMviControllerHolder.Dependencies
    
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
        let _ = debugPrint("playlists label received",label_)
        switch(label_) {
        case let messageLabel as PlaylistsMviContractMviStoreLabel.Message:
            debugPrint("Message: \(messageLabel.message))")
            
        case let navModel as PlaylistsMviContractMviStoreLabel.Navigate:
            dependencies.mainCoordinator.navigateModel(model: navModel.model)
            
        case let selectorConfig as PlaylistsMviContractMviStoreLabel.ShowPlaylistsSelector:
            dependencies.mainCoordinator.showPlaylistSelector(config: selectorConfig.config)
            
        default: debugPrint(label_)
        }
    }
    
    func actions() -> Actions {
        return Actions(view: self)
    }
    
    class Actions {
        let view:PlaylistsMviViewProxy
        
        init(view:PlaylistsMviViewProxy) {
            self.view = view
        }
        
        func tapAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnOpenPlaylist(item: item, view: nil))
        }
        
        func shareAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnShare(item: item))
        }
        
        func deleteAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnDelete(item: item))
        }
        
        func starAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnStar(item: item))
        }
        
        func mergeAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnMerge(item: item))
        }
        
        func moveAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnMoveSwipe(item: item))
        }
        
        func editAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnEdit(item: item, view: nil))
        }
        
        func playAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnPlay(item: item, external:true, view: nil))
        }
        
        func playInAppAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
            view.dispatch(event: PlaylistsMviContractViewEvent.OnPlay(item: item, external:false, view: nil))
        }
        
    }
}
