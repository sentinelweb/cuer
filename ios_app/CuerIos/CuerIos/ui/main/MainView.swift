//
//  MainView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct MainView: View {
    
    @StateObject private var coordinator: MainCoordinator
    // updated from onReceive at bottom of view
    //      @State private var screen: Screen = Screen.launch
    
    
    init(mainCoordinator: MainCoordinator) {
        self._coordinator = StateObject(wrappedValue: mainCoordinator)
    }
    
    var body: some View {
        switch (coordinator.screen) {
        case .launch:
            Text("Launching")
                .onAppear{coordinator.navigate(route: Route.main)}
        case .main:
            TabView(selection: $coordinator.currentTab) {
                BrowseView(viewModel: coordinator.createBrowseViewModel())
                    .tabItem { Label("Browse", systemImage: "folder.fill.badge.person.crop") }
                    .tag(MainTab.browse)
                
                PlaylistsView(viewModel: coordinator.createPlaylistsViewModel())
                    .tabItem { Label("Playlists", systemImage: "list.bullet.indent") }
                    .tag(MainTab.playlists)
                
                PlaylistView(viewModel: coordinator.createPlaylistViewModel())
                    .tabItem { Label("Playlist", systemImage: "music.note.list") }
                    .tag(MainTab.playlist)
                
                NavigationView {
                    SettingsView(coordinator: coordinator)
                }
                .navigationViewStyle(StackNavigationViewStyle())
                .tabItem { Label("Settings", systemImage: "gear") }
                .tag(MainTab.settings)
                
            }
            .sheet(item: $coordinator.openedURL) {
                SafariView(url: $0)
                    .edgesIgnoringSafeArea(.all)
            }
        default:
            Text("Unsupported")
        }
    }
}


//struct MainView_Previews: PreviewProvider {
//    static var previews: some View {
//        MainView()
//    }
//}
