//
//  IosShareWrapper.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import Foundation
import shared

class IosShareWrapper: ShareWrapper {
    
    override func share(media: DomainMediaDomain) {
        debugPrint("share: \(fullMessage(media: media))")
    }
    
    override func share(playlist: DomainPlaylistDomain) {
        debugPrint("share: \(playlistMessage(playlist: playlist))")
    }
}
