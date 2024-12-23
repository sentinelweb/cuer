//
//  MainView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct MainView: View {
    
    @StateObject private var coordinator: MainCoordinator

    init(mainCoordinator: MainCoordinator) {
        self._coordinator = StateObject(wrappedValue: mainCoordinator)
    }
    
    var body: some View {
        switch (coordinator.screen) {
        case .launch:
            Text("Launching")
                .onAppear{coordinator.navigate(route: Route.main)}
        case .main:
            ZStack {
                TabView(selection: $coordinator.currentTab) {
                    BrowseView(holder: coordinator.browseController!)
                        .tabItem { TabLabelView(text: "Browse", systemImage: "folder.fill.badge.person.crop") }
                        .tag(MainTab.browse)
                    
                    PlaylistsView(holder: coordinator.playlistsController!)
                        .tabItem { TabLabelView(text: "Playlists", systemImage: "list.bullet.indent") }
                        .tag(MainTab.playlists)
                    
                    if (coordinator.playlistController != nil) {
                        PlaylistView(holder: coordinator.playlistController!)
                            .tabItem { TabLabelView(text: "Playlist", systemImage: "music.note.list") }
                            .tag(MainTab.playlist)
                    }
                    
                    NavigationView { SettingsView(coordinator: coordinator) }
                        .navigationViewStyle(StackNavigationViewStyle())
                        .tabItem { TabLabelView(text: "Settings", systemImage: "gearshape") }
                        .tag(MainTab.settings)
                }
                if (coordinator.playlistSelectDialog != nil) {
                    let _ = debugPrint("showPlaylistsDialog")
                    PlaylistsDialogView(holder: coordinator.playlistSelectDialog!)
                        .transition(.opacity.animation(.easeIn(duration: 0.5)))
                }
            }
            .onAppear() {
                UITabBar.appearance().backgroundColor = .systemBackground.withAlphaComponent(0.8)
                UITabBarItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: tabItemTypeface], for: .normal)
                UITabBarItem.appearance().setTitleTextAttributes([NSAttributedString.Key.font: tabItemSelectedTypeface], for: .selected)
            }
            .sheet(item: $coordinator.openedURL) {
                SafariView(url: $0)
                    .edgesIgnoringSafeArea(.all)
            }
            .sheet(item: $coordinator.shareData) {
                ShareSheet(activityItems: [$0])
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
