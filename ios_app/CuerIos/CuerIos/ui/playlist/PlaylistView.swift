//
//  PlaylistView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct PlaylistView: View {
    @StateObject private var viewModel: PlaylistViewModel
    
    init(viewModel: PlaylistViewModel) {
        self._viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        Text("Playlist: \(viewModel.plId)")
    }
}

//struct PlaylistView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistView()
//    }
//}
