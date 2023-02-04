//
//  PlaylistsViews.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct PlaylistsHeaderItemView: View {
    let item: PlaylistsItemMviContract.ModelHeader
    var body: some View {
        Text(item.title)
            .font(headerListRowTypeface)
            .padding(.vertical, 8)
            .padding(.leading, 8)
        
    }
}

struct PlaylistsItemRowView: View {
    
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
                .font(itemRowTitleTypeface)
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(.leading, CGFloat(item.depth) * 10.0)
        .padding(.bottom, 2)
    }
}

struct PlaylistsItemRowViewActions: View {
    
    let item: PlaylistsItemMviContract.ModelItem
    let actions: PlaylistsMviViewProxy.Actions
    
    init(item: PlaylistsItemMviContract.ModelItem, actions: PlaylistsMviViewProxy.Actions) {
        self.item = item
        self.actions = actions
    }
    
    var body: some View {
        PlaylistsItemRowView(item: item)
            //.background(Color(.green))
            .overlay(contextMenuOverlay(item: item, actions: actions), alignment: .trailing)
            .swipeActions(edge: .leading) {
                Button {
                    actions.moveAction(item: item)
                } label: {
                    Label("Move", systemImage: "arrow.up.and.down.and.arrow.left.and.right")
                }
                .tint(Color.ui.colorMove)
            }
            .swipeActions(edge: .trailing) {
                Button {
                    actions.deleteAction(item: item)
                } label: {
                    Label("Delete", systemImage: "trash")
                }
                .tint(Color.ui.colorDelete)
            }
    }
}

struct PlaylistsListItemView: View {
    
    let list: PlaylistsItemMviContract.ModelList
    let actions: PlaylistsMviViewProxy.Actions
    
    let layout = [GridItem(.fixed(100))]
    
    var body: some View {
        ScrollView(.horizontal){
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
                        // this doesnt work with List
                        //.overlay(contextMenuOverlay(item: item, actions: actions), alignment: .topTrailing)
                        .onTapGesture {actions.tapAction(item: item)}
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

@ViewBuilder
private func contextMenuOverlay(
    item: PlaylistsItemMviContract.ModelItem,
    actions: PlaylistsMviViewProxy.Actions
) -> some View {
    Image(systemName: "ellipsis")
        .foregroundColor(Color.ui.light_color_on_surface)
        .padding(.horizontal, 4)
        .padding(.vertical, 4)
        .frame(width: 30, height: 30, alignment: .center)
        //.background(Color(.red))
        //.onTapGesture {}
        .contextMenu {
            Button() {actions.playInAppAction(item: item)} label: {
                Label("Play", systemImage: "play.fill")
            }
            Button {actions.shareAction(item: item)} label: {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            Button() {actions.playAction(item: item)} label: {
                Label("Launch", systemImage: "arrow.up.right.video.fill")
            }
            
            Divider()
            
            Button {actions.starAction(item: item)} label: {
                if (item.starred) {
                    Label("Unstar", systemImage: "star")
                } else {
                    Label("Star", systemImage: "star.fill")
                }
            }
            Button() {actions.moveAction(item: item)} label: {
                Label("Move", systemImage: "arrow.up.and.down.and.arrow.left.and.right")
            }
            Button() {actions.editAction(item: item)} label: {
                Label("Edit", systemImage: "pencil")
            }
            Button() {actions.mergeAction(item: item)} label: {
                Label("Merge", systemImage: "arrow.triangle.merge")
            }
            
            Divider()
            
            Button(role: .destructive) {actions.deleteAction(item: item)} label: {
                Label("Delete", systemImage: "trash")
            }
        }
}

struct TitleView_Previews: PreviewProvider {
    static var previews: some View {
        let item = PlaylistsItemMviContract.ModelHeader(
            id: DomainOrchestratorContractIdentifier<DomainGUID>(id:DomainGUID(value:"header-guid"), source: DomainOrchestratorContractSource.local),
            title: "Header title"
        )
        PlaylistsHeaderItemView(item: item)
    }
}
