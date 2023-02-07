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
            KFImage(URL(string: view.model.header.imageUrl))
                .fade(duration: 0.3)
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: UIScreen.main.bounds.width, height: 150)
                .clipped()
                .onTapGesture {view.dispatch(event: PlaylistMviContractViewEvent.OnRefresh())}
                .listRowInsets(EdgeInsets())
            
            ForEach(view.model.items ?? []) { item in
                Text(item.title)
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
