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
    private var view : BrowseViewProxy
    
    @StateObject
    private var holder: BrowseControllerHolder
    
    let layout = [
        GridItem(.flexible(minimum: 80, maximum: 250)),
        GridItem(.flexible(minimum: 80, maximum: 250))
    ]
    
    init(holder: BrowseControllerHolder) {
        self._holder = StateObject(wrappedValue: holder)
        self._view = StateObject(wrappedValue: holder.createMviView())
    }
    
    var body: some View {
        
        VStack (alignment: .leading, spacing:8) {
            HStack(alignment: .center, spacing: 8) {
                Image(systemName: "arrow.backward")
                    .onTapGesture {view.dispatch(event: BrowseContractViewEvent.OnUpClicked())}
                    .padding(8)
                Text(view.model.title)
                    .onTapGesture {
                    view.dispatch(event: BrowseContractViewEvent.OnCategoryClicked(model: view.model.categories.first!))
                }.font(headerTypeface)
                Spacer()
                if (view.model.order == BrowseContract.Order.categories) {
                    Image(systemName: "abc")
                        .onTapGesture {view.dispatch(event: BrowseContractViewEvent.OnSetOrder(order: BrowseContract.Order.aToZ))}
                        .padding(8)
                } else {
                    Image(systemName: "folder")
                        .onTapGesture {view.dispatch(event: BrowseContractViewEvent.OnSetOrder(order: BrowseContract.Order.categories))}
                        .padding(8)
                }
                
            }
            ScrollView {
                LazyVGrid(columns: layout, spacing: 0) {
                    ForEach(view.model.categories) { item in
                        let seq = view.model.categories.firstIndex(of: item) ?? 0
                        
                        BrowseItem(item: item, seq: seq)
                            .onTapGesture{ view.dispatch(event: BrowseContractViewEvent.OnCategoryClicked(model: item))}
                    }
                }
            }
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
