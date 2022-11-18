//
//  BrowseView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI
import Combine
import shared

struct BrowseView: View {
    @StateObject
    private var view = BrowseViewProxy()
    
    @StateObject
    private var holder: BrowseControllerHolder
    
    init(holder: BrowseControllerHolder) {
        self._holder = StateObject(wrappedValue: holder)
    }
    
    var body: some View {
        VStack {
            Spacer()
            Text(view.model.title).onTapGesture {
                view.dispatch(event: BrowseContractViewEvent.OnCategoryClicked(model: view.model.categories.first!))
            }
            Spacer()
            Text("goto up").onTapGesture {
                view.dispatch(event: BrowseContractViewEvent.OnUpClicked())
            }
            Spacer()
        }
        .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
        .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
        .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
    }
}

//struct BrowseView_Previews: PreviewProvider {
//    static var previews: some View {
//        BrowseView()
//    }
//}
