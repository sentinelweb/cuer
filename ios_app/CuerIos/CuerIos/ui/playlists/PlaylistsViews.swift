//
//  PlaylistsViews.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct HeaderItemView: View {
    let item: PlaylistsItemMviContract.ModelHeader
    
    var body: some View {
        Text(item.title)
            .font(headerListRowTypeface)
            .padding(.vertical, 8)
            .padding(.leading, 8)
        
    }
}

struct ItemRowView: View {
    let item: PlaylistsItemMviContract.ModelItem
    
    var body: some View {
        HStack(alignment: .center) {
            KFImage(URL(string: item.thumbNailUrl ?? DEFAULT_IMAGE))
                .fade(duration: 0.3)
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 100, height: 60)
                .clipped()
            Text(item.title)
        }
    }
}

struct ListItemView: View {
    
    let list: PlaylistsItemMviContract.ModelList
    let onTap: (PlaylistsItemMviContract.ModelItem) -> Void
    
    let layout = [GridItem(.fixed(100))]
    
    var body: some View {
        ScrollView{
            LazyHGrid(rows: layout, spacing: 1) {
                ForEach(list.items) { item in
                    KFImage(URL(string: item.thumbNailUrl ?? DEFAULT_IMAGE))
                        .fade(duration: 0.3)
                        .forceTransition()
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 100, height: 100)
                        .clipped()
                        .overlay(titleOverlay(item: item), alignment: .bottom)
                        .onTapGesture {onTap(item)}
                }
            }
        }
    }
    
    @ViewBuilder
    private func titleOverlay(item: PlaylistsItemMviContract.ModelItem) -> some View {
        HStack {
            Text(item.title)
            Spacer()
        }
        .foregroundColor(Color(.label))
        .padding(.horizontal, 4)
        .padding(.vertical, 4)
        .background(Color(.systemBackground).opacity(0.75))
        .font(itemTileTypeface)
    }
}

struct TitleView_Previews: PreviewProvider {
    static var previews: some View {
        let item = PlaylistsItemMviContract.ModelHeader(id:1, title: "Header title")
        HeaderItemView(item: item)
    }
}
