//
//  PlaylistViews.swift
//  CuerIos
//
//  Created by Robert Munro on 07/02/2023.
//

import SwiftUI
import shared
import Kingfisher

struct PlaylistHeaderView: View {
    
    let header: PlaylistMviContractViewHeader
    let action: (PlaylistMviContractViewEvent) -> Void
    
    var body: some View {
        VStack(alignment: .leading) {
            if let url = header.imageUrl {
            KFImage(URL(string: url))
                .fade(duration: 0.3)
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: UIScreen.main.bounds.width, height: 150)
                .clipped()
                //.onTapGesture {view.dispatch(event: PlaylistMviContractViewEvent.OnRefresh())}
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack{
                    if (header.canPlay) {
                        PillButton(text: "Play", icon: "play.fill") {action(PlaylistMviContractViewEvent.OnPlay())}
                    }
                    // PillButton(text: "Edit", icon: "pencil") {action(PlaylistMviContractViewEvent.OnEdit())}
                    if (header.canUpdate) {
                        PillButton(text: "Update", icon: "arrow.clockwise") {action(PlaylistMviContractViewEvent.OnUpdate())}
                    }
                    if (header.isStarred) {
                        PillButton(text: "Unstar", icon: "star.fill") {action(PlaylistMviContractViewEvent.OnStar())}
                    } else {
                        PillButton(text: "Star", icon: "star") {action(PlaylistMviContractViewEvent.OnStar())}
                    }
                    switch(header.loopModeIndex) {
                        case 0: PillButton(text: "Straight", icon: "arrow.forward") {action(PlaylistMviContractViewEvent.OnPlayModeChange())}
                        case 1: PillButton(text: "Shuffle", icon: "shuffle") {action(PlaylistMviContractViewEvent.OnPlayModeChange())}
                        default: PillButton(text: "Loop", icon: "repeat") {action(PlaylistMviContractViewEvent.OnPlayModeChange())}
                    }
                    PillButton(text: "Launch", icon: "arrow.up.right") {action(PlaylistMviContractViewEvent.OnLaunch())}
                    PillButton(text: "Share", icon: "square.and.arrow.up") {action(PlaylistMviContractViewEvent.OnShare())}
                }
            }.padding(.leading, Dimension.spacing.leadingHeader)
                
            Text(String(header.itemsText))
                .font(headerInfoTypeface)
                .padding(.leading, Dimension.spacing.leadingHeader)
            
            Text(header.title)
                .font(headerTypeface)
                .padding(.leading, Dimension.spacing.leadingHeader)
        }
        .frame(maxWidth: .infinity)
    }
}

struct PlaylistItemRowView: View {
    
    let item: PlaylistItemMviContract.ModelItem
    let actions: PlaylistMviViewProxy.Actions?
    
    var body: some View {
        HStack(alignment: .center) {
            KFImage(URL(string: item.thumbUrl ?? DEFAULT_IMAGE))
                .fade(duration: 0.3)
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 100, height: 60)
                .clipped()
                .onTapGesture { actions?.playInAppAction(item: item) }
            
            Text(item.title)
                .font(itemRowTitleTypeface)
                .onTapGesture { actions?.playInAppAction(item: item) }
                //.onTapGesture { actions?.editAction(item: item) }
            Spacer()
        }
        //
        .frame(maxWidth: .infinity)
        .padding(.bottom, 2)
    }
}

struct PlaylistItemRowViewActions: View {

    let item: PlaylistItemMviContract.ModelItem
    let actions: PlaylistMviViewProxy.Actions

    init(item: PlaylistItemMviContract.ModelItem, actions: PlaylistMviViewProxy.Actions) {
        self.item = item
        self.actions = actions
    }

    var body: some View {
        PlaylistItemRowView(item: item, actions: actions)
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

@ViewBuilder
private func contextMenuOverlay(
    item: PlaylistItemMviContract.ModelItem,
    actions: PlaylistMviViewProxy.Actions
) -> some View {
    Image(systemName: "ellipsis")
        .foregroundColor(Color.ui.light_color_on_surface)
        .padding(.horizontal, 4)
        .padding(.vertical, 4)
        .frame(width: 30, height: 30, alignment: .center)
        .contextMenu {
            Button() {actions.playInAppAction(item: item)} label: {
                Label("Play", systemImage: "play.fill")
            }
            Button {actions.shareAction(item: item)} label: {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            Button() {actions.launchAction(item: item)} label: {
                Label("Launch", systemImage: "arrow.up.right.video.fill")
            }

            Divider()

            Button {actions.starAction(item: item)} label: {
                if (item.isStarred) {
                    Label("Unstar", systemImage: "star")
                } else {
                    Label("Star", systemImage: "star.fill")
                }
            }
            Divider()

            Button(role: .destructive) {actions.deleteAction(item: item)} label: {
                Label("Delete", systemImage: "trash")
            }
        }
}

struct PlaylistViews_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            PlaylistHeaderView(header: PlaylistMviContractViewHeader(
                title: "Playlist title",
                imageUrl: DEFAULT_IMAGE,
                loopModeIndex: 0,
                loopModeIcon: .icPlaymodeShuffle,
                loopModeText: "Shuffle",
                playIcon: .icPlaylistPlay,
                playText: "Play",
                starredIcon: .icStarred,
                starredText: "Unstar",
                isStarred: true,
                isDefault: true,
                isPlayFromStart: true,
                isPinned: true,
                isSaved: true,
                canPlay: true,
                canEdit: true,
                canUpdate: true,
                canDelete: true,
                canEditItems: true,
                canDeleteItems: true,
                hasChildren: 0,
                itemsText: "3/30"
            ), action: {x in })
            
            PlaylistItemRowView(item:  PlaylistItemMviContract.ModelItem(
                id: DomainOrchestratorContractIdentifier(id: DomainGUID(value:"test-guid"), source: .memory),
                index: 1,
                url: "",
                type: .video,
                title: "Quake eyewitness: 'We are aid workers, but now we are the ones who need help' | DW News",
                duration: "00:66",
                positon: "00:33",
                imageUrl: DEFAULT_IMAGE,
                thumbUrl: DEFAULT_IMAGE,
                channelImageUrl: DEFAULT_IMAGE,
                progress: 0.5,
                published: "PubDate",
                watchedSince: "Watched",
                isWatched: true,
                isStarred: true,
                platform: .youtube,
                isLive: false,
                isUpcoming: false,
                infoTextBackgroundColor: .colorOnSurface,
                canEdit: true,
                playlistName: "Playlist",
                canDelete: false,
                canReorder: true,
                showOverflow: true,
                deleteResources: nil
            ), actions: nil)
            Spacer()
        }
    }
}
