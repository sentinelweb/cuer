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
            VStack(alignment: .leading, spacing: 8) {
                Text("go up").onTapGesture {
                    view.dispatch(event: BrowseContractViewEvent.OnUpClicked())
                }
                Text(view.model.title).onTapGesture {
                    view.dispatch(event: BrowseContractViewEvent.OnCategoryClicked(model: view.model.categories.first!))
                }.font(.title)
            }
            ScrollView {
                LazyVGrid(columns: layout, spacing: 0) {
                    ForEach(view.model.categories) { item in
                        VStack {
                            let url = (item.thumbNailUrl ??? {$0?.starts(with: "https") ?? false})
                            ?? "https://cuer-275020.firebaseapp.com/images/categories/greek.jpg"
                            AsyncImage(url: URL(string: url)) { image in
                                image.resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                ProgressView()
                            }.frame(width: UIScreen.main.bounds.width / 2, height: 150)
                                .clipped()
//                                .shadow(color: .gray, radius: 2, x: 4, y: 4)
                                
                            .overlay(titleOverlay(item: item), alignment: .bottom)
                            .frame(maxWidth: UIScreen.main.bounds.width / 2, maxHeight:150)
                        }.onTapGesture{ view.dispatch(event: BrowseContractViewEvent.OnCategoryClicked(model: item))}
                    }
                }
            }
        }
        .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
        .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
        .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
    }
    
    
    @ViewBuilder
    private func titleOverlay(item: BrowseContractViewCategoryModel)-> some View {
        return HStack {
            Text(item.title)
                Spacer()
            }
            .font(.headline)
            .padding(.horizontal, 20)
            .padding(.vertical, 8)
            .background(Color(.systemBackground).opacity(0.75))
//            .onTapGesture { viewModel.open(source) }
        
    }
}

//struct BrowseView_Previews: PreviewProvider {
//    static var previews: some View {
//        BrowseView()
//    }
//}
