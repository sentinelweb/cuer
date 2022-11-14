//
//  MainCoordinator.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

// outgoing route request (pass view dependencies)
enum Screen {
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

enum MainTabScreen {
    case browse(viewModel: BrowseViewModel)
    case playlists(viewModel: PlaylistsViewModel)
    case playlist(viewModel: PlaylistViewModel)
}

// incoming route request (pass data in enum)
enum Route {
    case browse
    case playlists
    case playlist
    case itemEdit
    case playlistEdit
}

protocol MainCoordinatorDependency {
    var mainCoordinator: MainCoordinator { get }
}

class EmptyObservable: ObservableObject {}// testing only

class MainCoordinator: ObservableObject {
    
    let dependencies: AppDependencies
    
    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
//        self.tabScreen = MainTabScreen.browse(viewModel: dependencies.createBrowseViewModel())
    }
    
    @Published var screen: Screen = Screen.launch // navigate called in view onAppear
    @Published var tabScreen: MainTabScreen? = nil
    @Published var currentRoute: Route = Route.browse
    @Published var currentTab: MainTab = MainTab.browse
    @Published var openedURL: URL?
    
    func navigate(route: Route) {
        self.currentRoute = route
        debugPrint("navigate: \(route)")
        switch(route){
        case .browse:
            tabScreen = MainTabScreen.browse(viewModel: dependencies.createBrowseViewModel())
        
        default: debugPrint("navigate default: \(route)")
        }
    }
    
//    func createViewModel(route: Route)-> any ObservableObject {
//        switch(route){
//        case .browse:
//            return dependencies.createBrowseViewModel()
//        default:EmptyObservable()
//        }
//
//    }
    
    func open(_ url: URL) {
        self.openedURL = url
    }
    
}

