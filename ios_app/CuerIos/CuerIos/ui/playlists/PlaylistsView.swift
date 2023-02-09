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
        ZStack(alignment: .bottom) {
            List {
                PlaylistsHeaderView(model: view.model)
                    .listRowInsets(EdgeInsets())

                ForEach(view.model.items) { item in
                    switch(item) {
                        
                    case let header as PlaylistsItemMviContract.ModelHeader:
                        PlaylistsHeaderItemView(item: header)
                        
                    case let itemRow as PlaylistsItemMviContract.ModelItem:
                        PlaylistsItemRowViewActions(item: itemRow, actions: view.actions())
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
                .onAppear { holder.controller.onRefresh()}
                .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
            
            if let undo = view.showSnackbar {
                SnackbarView(message: undo.message, undoAction: {view.dispatch(event: PlaylistsMviContractViewEvent.OnUndo(undoType: undo.undoType))})
            }
        }
    }
}

//struct PlaylistsView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistsView()
//    }
//}
