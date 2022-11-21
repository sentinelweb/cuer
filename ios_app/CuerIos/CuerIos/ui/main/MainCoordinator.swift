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
    case playlist(plId: Int)
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
    @Published var currentPlaylistId: Int = -1
    @Published var playlistViewModel: PlaylistViewModel! = nil
    @Published var playlistsViewModel: PlaylistsViewModel! = nil
    @Published var browseController: BrowseControllerHolder! = nil
    @Published var openedURL: URL?
    
    func navigate(route: Route) {
        self.currentRoute = route
        debugPrint("navigate: \(route)")
        switch(route){
        case .main:
            self.playlistViewModel = createPlaylistViewModel()
            self.playlistsViewModel = createPlaylistsViewModel()
            self.browseController = dependencies.createBrowseHolder()
            self.screen = Parent.main
        case .browse:
            self.currentTab = MainTab.browse
        case .playlists:
            self.currentTab = MainTab.playlists
        case let .playlist(plId):
            self.currentTab = MainTab.playlist
            self.currentPlaylistId = plId
        
        default: debugPrint("navigate default: \(route)")
        }
    }
    
    private func createBrowseController() -> BrowseControllerHolder {
        if (self.browseController == nil) {
            self.browseController = dependencies.createBrowseHolder()
        }
        return self.browseController!
    }
    
    private func createPlaylistsViewModel() -> PlaylistsViewModel {
        if (self.playlistsViewModel == nil) {
            self.playlistsViewModel = dependencies.createPlaylistsViewModel()
        }
        return self.playlistsViewModel!
    }
    
    private func createPlaylistViewModel() -> PlaylistViewModel{
        if (self.playlistViewModel == nil) {
            self.playlistViewModel = dependencies.createPlaylistViewModel(plId: -1)
        }
        return self.playlistViewModel!
    }
    
    func open(_ url: URL) {
        self.openedURL = url
    }
    
}

