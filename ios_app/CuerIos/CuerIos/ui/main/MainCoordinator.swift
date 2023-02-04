//
//  MainCoordinator.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import shared
import UIKit

// outgoing route request (pass view dependencies)
enum Parent {
    case launch
    case main
    case none // no screen
}

enum MainTab {
    case browse
    case playlists
    case playlist
    case settings
}

// incoming route request (pass data in enum)
enum Route {
    case main
    case browse
    case playlists
    case playlist(plId: DomainOrchestratorContractIdentifier<DomainGUID>)
    case itemEdit
    case playlistEdit
    case none
}

protocol MainCoordinatorDependency {
    var mainCoordinator: MainCoordinator { get }
}

class EmptyObservable: ObservableObject {}// testing only

class MainCoordinator: ObservableObject {
    
    let dependencies: AppDependencies
    
    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
    }
    
    @Published var screen: Parent = Parent.launch // navigate called in view onAppear
    @Published var currentRoute: Route = Route.none
    @Published var currentTab: MainTab = MainTab.browse
    @Published var currentPlaylistId: DomainOrchestratorContractIdentifier<DomainGUID>? = nil
    @Published var playlistViewModel: PlaylistViewModel! = nil
    @Published var playlistsController: PlaylistsMviControllerHolder! = nil
    @Published var browseController: BrowseControllerHolder! = nil
    @Published var openedURL: URL?
    @Published var shareData: String?
    @Published var playlistSelectDialog: PlaylistsDialogViewModelHolder? = nil
    
    func navigate(route: Route) {
        self.currentRoute = route
        debugPrint("navigate: \(route)")
        hideDialogs()
        switch(route){
        case .main:
            self.playlistViewModel = createPlaylistViewModel()
            self.playlistsController = createPlaylistsController()
            self.browseController = dependencies.createBrowseHolder()
            self.screen = Parent.main
        case .browse:
            self.currentTab = MainTab.browse
        case .playlists:
            self.currentTab = MainTab.playlists
        case let .playlist(plId):
            debugPrint("playlisId:", plId)
            self.currentTab = MainTab.playlist
            self.currentPlaylistId = plId
            
        default: debugPrint("navigate default: \(route)")
        }
    }
    
    func navigateModel(model: NavigationModel) {
        hideDialogs()
        switch(model.target){
        case .playlist:
            self.currentTab = MainTab.playlist
            let id = DomainOrchestratorContractIdentifier<DomainGUID>(id: DomainGUID(value: model.params[.playlistId] as! String), source: model.params[.source] as! DomainOrchestratorContractSource)
            debugPrint("navigateModel:", id)
            self.currentPlaylistId = id
        default: debugPrint("Not supported: \(model)")
        }
    }
    
    private func createBrowseController() -> BrowseControllerHolder {
        if (self.browseController == nil) {
            self.browseController = dependencies.createBrowseHolder()
        }
        return self.browseController!
    }
    
    private func createPlaylistsController() -> PlaylistsMviControllerHolder {
        if (self.playlistsController == nil) {
            self.playlistsController = dependencies.createPlaylistsHolder()
        }
        return self.playlistsController!
    }
    
    private func createPlaylistViewModel() -> PlaylistViewModel{
        if (self.playlistViewModel == nil) {
            self.playlistViewModel = dependencies.createPlaylistViewModel(plId: -1)
        }
        return self.playlistViewModel!
    }
    
    private func hideDialogs() {
        hidePlaylistSelector()
    }
    
    func open(_ url: URL) {
        self.openedURL = url
    }
    
    func share(_ text: String) {
        self.shareData = text
    }
    
    func showPlaylistSelector(config: PlaylistsMviDialogContractConfig) {
        self.playlistSelectDialog = dependencies.createPlaylistsDialogHolder(config: config)
    }
    
    func hidePlaylistSelector(){
        self.playlistSelectDialog = nil
    }
}

