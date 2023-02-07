//
//  PlaylistMviController.swift
//  CuerIos
//
//  Created by Robert Munro on 06/02/2023.
//

import Foundation
import shared
import Combine

protocol PlaylistControllerDependency {
    func createPlaylistHolder() -> PlaylistMviControllerHolder
}

class PlaylistControllerProvider: PlaylistMviControllerHolder.Dependencies {
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

class PlaylistMviControllerHolder : ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedFactoriesDependency
    
    private let dependencies: Dependencies
    let lifecycle: LifecycleLifecycleRegistry
    let controller: PlaylistMviController
    
    private var playlistIdSubscription: AnyCancellable? = nil
    /*@Published*/ var playlistId: DomainOrchestratorContractIdentifier<DomainGUID>? = nil
    
    init(dependencies: Dependencies) {
        debugPrint("---- init:PlaylistMviControllerHolder")
        
        self.dependencies = dependencies
        lifecycle = dependencies.sharedFactories.orchestratorFactory.utils.lifecycleRegistry()
        controller = dependencies.sharedFactories.presentationFactory.playlistController(lifecycle: lifecycle)
        lifecycle.onCreate()
        playlistIdSubscription = dependencies.mainCoordinator.$currentPlaylistId.sink(receiveValue:{playlistIdentifier in
            debugPrint("---- playlistIdentifier:", playlistIdentifier)
            if (self.playlistId != playlistIdentifier) {
                self.playlistId = playlistIdentifier
                if (playlistIdentifier != nil) {
                    self.controller.onSetPlayListData(intent: PlaylistMviContractMviStoreIntent.SetPlaylistData(
                        plId: playlistIdentifier?.id,
                        plItemId: nil,
                        playNow: false,
                        source: playlistIdentifier?.source ?? .local,
                        addPlaylistParent: nil
                    ))
                }
            }
        })
    }
    
    deinit {
        lifecycle.onDestroy()
    }
    
    func createMviView() -> PlaylistMviViewProxy { PlaylistMviViewProxy(dependencies: self.dependencies)}
}

class PlaylistMviViewProxy : UtilsUBaseView<PlaylistMviContractViewModel, PlaylistMviContractViewEvent>, PlaylistMviContractView, ObservableObject {
    
    @Published
    var model: PlaylistMviContractViewModel
    
    private let dependencies: PlaylistMviControllerHolder.Dependencies
    
    init(dependencies: PlaylistsMviControllerHolder.Dependencies)  {
        model = PlaylistMviModelMapper.Companion().DEFAULT_PLAYLIST_VIEW_MODEL
        self.dependencies = dependencies
        super.init()
    }
    
    override func render(model: PlaylistMviContractViewModel) {
        self.model = model
    }
    
    func processLabel(label_: PlaylistMviContractMviStoreLabel) {
        let _ = debugPrint("playlist label received", label_)
        switch(label_) {
//        case let messageLabel as PlaylistsMviContractMviStoreLabel.Message:
//            debugPrint("Message: \(messageLabel.message))")
//
//        case let navModel as PlaylistsMviContractMviStoreLabel.Navigate:
//            dependencies.mainCoordinator.navigateModel(model: navModel.model)
//
//        case let selectorConfig as PlaylistsMviContractMviStoreLabel.ShowPlaylistsSelector:
//            dependencies.mainCoordinator.showPlaylistSelector(config: selectorConfig.config)
        
        default: debugPrint(label_)
        }
    }
    
    func actions() -> Actions {
        return Actions(view: self)
    }
    
    class Actions {
        let view:PlaylistMviViewProxy
        
        init(view:PlaylistMviViewProxy) {
            self.view = view
        }
        
//        func tapAction(item: PlaylistItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistMviContractViewEvent.OnOpenPlaylist(item: item, view: nil))
//        }
//
//        func shareAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnShare(item: item))
//        }
//
//        func deleteAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnDelete(item: item))
//        }
//
//        func starAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnStar(item: item))
//        }
//
//        func mergeAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnMerge(item: item))
//        }
//
//        func moveAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnMoveSwipe(item: item))
//        }
//
//        func editAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnEdit(item: item, view: nil))
//        }
//
//        func playAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnPlay(item: item, external:true, view: nil))
//        }
//
//        func playInAppAction(item: PlaylistsItemMviContract.ModelItem) -> Void {
//            view.dispatch(event: PlaylistsMviContractViewEvent.OnPlay(item: item, external:false, view: nil))
//        }
        
    }
}
