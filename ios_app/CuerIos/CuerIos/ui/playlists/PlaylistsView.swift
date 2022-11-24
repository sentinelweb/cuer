//
//  PlaylistsView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct PlaylistsView: View {
    
    @StateObject
    private var view : PlaylistsMviViewProxy
    
    @StateObject
    private var holder: PlaylistsMviControllerHolder
    
    let layout = [
        GridItem()
    ]
    
    init(holder: PlaylistsMviControllerHolder) {
        self._holder = StateObject(wrappedValue: holder)
        self._view = StateObject(wrappedValue: holder.createMviView())
    }
    
    var body: some View {
        List {
            KFImage(URL(string: view.model.imageUrl))
                .fade(duration: 0.3)
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: UIScreen.main.bounds.width, height: 150)
                .clipped()
                .onTapGesture {view.dispatch(event: PlaylistsMviContractViewEvent.OnRefresh())}
                .listRowInsets(EdgeInsets())
            
            Text(String(view.model.items.count))
                .font(headerInfoTypeface)
                .listRowInsets(EdgeInsets(top: 0, leading: 8, bottom: 0, trailing: 0))
            
            Text(view.model.title)
                .font(headerTypeface)
                .listRowInsets(EdgeInsets(top: 0, leading: 8, bottom: 0, trailing: 0))
            
            
            ForEach(view.model.items) { item in
                let _ = debugPrint(item.id)
                switch(item) {
                    
                case let header as PlaylistsItemMviContract.ModelHeader:
                    PlaylistsHeaderItemView(item: header)
                    
                case let itemRow as PlaylistsItemMviContract.ModelItem:
                    PlaylistsItemRowView(item: itemRow, actions: view.actions())
                        .onTapGesture {view.dispatch(event: PlaylistsMviContractViewEvent.OnOpenPlaylist(item: item, view: nil))}
                case let list as PlaylistsItemMviContract.ModelList:
                    PlaylistsListItemView(list: list, actions: view.actions())
                    
                default:
                    Text("Unknown Type!")
                }
            }.listRowInsets(EdgeInsets())
        }.listStyle(PlainListStyle())
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
