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
    @State private var playlistViewModel: PlaylistViewModel?
    
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
                BrowseView(holder: coordinator.browseController!)
                    .tabItem { TabLabelView(text: "Browse", systemImage: "folder.fill.badge.person.crop") }
                    .tag(MainTab.browse)
                
                PlaylistsView(viewModel: coordinator.playlistsViewModel!)
                    .tabItem { TabLabelView(text: "Playlists", systemImage: "list.bullet.indent") }
                    .tag(MainTab.playlists)
                
                if (playlistViewModel != nil) {
                    PlaylistView(viewModel: playlistViewModel!)
                        .tabItem { TabLabelView(text: "Playlist", systemImage: "music.note.list") }
                        .tag(MainTab.playlist)
                }
                
                NavigationView { SettingsView(coordinator: coordinator) }
                    .navigationViewStyle(StackNavigationViewStyle())
                    .tabItem { TabLabelView(text: "Settings", systemImage: "gear") }
                    .tag(MainTab.settings)
                
            }.onAppear() {
                UITabBar.appearance().backgroundColor = .systemBackground.withAlphaComponent(0.8)
                 UITabBarItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: tabItemTypeface], for: .normal)
                 UITabBarItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: tabItemSelectedTypeface], for: .selected)
            }
            .sheet(item: $coordinator.openedURL) {
                SafariView(url: $0)
                    .edgesIgnoringSafeArea(.all)
            }.onReceive(coordinator.$playlistViewModel, perform: {vm in playlistViewModel=vm})
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
