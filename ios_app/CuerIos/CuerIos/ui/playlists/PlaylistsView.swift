//
//  PlaylistsView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct PlaylistsView: View {
    @StateObject private var viewModel: PlaylistsViewModel
    
    init(viewModel: PlaylistsViewModel) {
        self._viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        Text("Playlists")
    }
}

//struct PlaylistsView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistsView()
//    }
//}
