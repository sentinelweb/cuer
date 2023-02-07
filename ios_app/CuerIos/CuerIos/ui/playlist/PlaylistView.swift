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
        ZStack(alignment: .bottom) {
            List {
                PlaylistHeaderView(header: view.model.header, action: { e in view.dispatch(event: e)})
                    .listRowInsets(EdgeInsets())
                
                if let items = view.model.items {
                    ForEach(items) { item in
                        PlaylistItemRowViewActions(item: item, actions: view.actions())
                    }.listRowInsets(EdgeInsets())
                }
                
                
            }.listStyle(PlainListStyle())
                .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
                .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
                .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
            
            if let undo = view.showSnackbar {
                SnackbarView(message: undo.message, undoAction: {view.dispatch(event: PlaylistMviContractViewEvent.OnUndo(undoType: undo.undoType))})
            }
        }
        .edgesIgnoringSafeArea(.bottom)
    }
}


//struct PlaylistView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistView()
//    }
//}
