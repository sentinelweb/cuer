//
//  PlaylistContract.swift
//  CuerIos
//
//  Created by Robert Munro on 15/11/2022.
//

import Foundation
import shared

struct PlaylistModel {
    var title: String = "No title"
    var items: Array<PlaylistItemModel> = []
}

struct PlaylistItemModel {
    var title: String = "item title"
}

class PlaylistModelMapper {
    func map(playlist:DomainPlaylistDomain) -> PlaylistModel {
        return PlaylistModel(
            title: playlist.title,
            items: playlist.items.map{item in PlaylistItemModel(title:item.media.title ?? "No item title")}
        )
    }
    
}
