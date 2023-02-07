//
//  PlaylistView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct PlaylistView: View {
    @StateObject
    private var view : PlaylistMviViewProxy
    
    @StateObject
    private var holder: PlaylistMviControllerHolder
    
    let layout = [
        GridItem()
    ]
    
    init(holder: PlaylistMviControllerHolder) {
        self._holder = StateObject(wrappedValue: holder)
        self._view = StateObject(wrappedValue: holder.createMviView())
    }
    
    var body: some View {
        List {
            PlaylistHeaderView(header: view.model.header, action: { e in view.dispatch(event: e)})
                .listRowInsets(EdgeInsets())
            
            ForEach(view.model.items ?? []) { item in
                PlaylistItemRowView(item: item)
            }.listRowInsets(EdgeInsets())
            
        }.listStyle(PlainListStyle())
            .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
            .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
            //.onAppear { holder.controller.onRefresh()}
            .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
    }
}


//struct PlaylistView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistView()
//    }
//}
