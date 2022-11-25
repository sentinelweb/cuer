//
//  IosShareWrapper.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import Foundation
import shared

class IosShareWrapper: ShareWrapper {
    let mainCoordnator: MainCoordinator
    
    init(mainCoordinator:MainCoordinator) {
        self.mainCoordnator = mainCoordinator
    }
    
    override func share(media: DomainMediaDomain) {
        mainCoordnator.share(fullMessage(media: media))
        //debugPrint("share: \(fullMessage(media: media))")
    }
    
    override func share(playlist: DomainPlaylistDomain) {
        mainCoordnator.share(playlistMessage(playlist: playlist))
        //debugPrint("share: \(playlistMessage(playlist: playlist))")
    }
}
