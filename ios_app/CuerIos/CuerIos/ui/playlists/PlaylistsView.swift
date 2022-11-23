//
//  PlaylistsView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI
import shared

struct PlaylistsView: View {
    @StateObject
    private var view : PlaylistsMviViewProxy
    
    @StateObject
    private var holder: PlaylistsMviControllerHolder
    
    init(holder: PlaylistsMviControllerHolder) {
        self._holder = StateObject(wrappedValue: holder)
        self._view = StateObject(wrappedValue: holder.createMviView())
    }
    
    var body: some View {
        VStack {
            Spacer()
            Text("Playlists")
            Spacer()
            //            Text("Playlist 1").onTapGesture {
            //                viewModel.navigateToPlaylist(id: 1)
            //            }
            //            Text("Playlist 2").onTapGesture {
            //                viewModel.navigateToPlaylist(id: 2)
            //            }
            Spacer()
        }
        .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
        .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
        .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
        
    }
}

//struct PlaylistsView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistsView()
//    }
//}
