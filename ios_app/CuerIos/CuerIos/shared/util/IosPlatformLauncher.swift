//
//  IosPlatformLauncher.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import Foundation
import shared

class IosPlatformLauncher: PlatformLaunchWrapper {
    func canLaunchVideo() -> Bool {
        true
    }
    
    func canLaunchVideoWithOptions() -> Bool {
        true
    }
    
    func launch(address: String) -> Bool {
        debugPrint("launch: \(address)")
        return true
    }
    
    func launchChannel(id: String) -> Bool {
        debugPrint("launchChannel.id: \(id)")
        return true
    }
    
    func launchChannel(media: DomainMediaDomain) -> Bool {
        debugPrint("launchChannel: \(media.channelData.title)")
        return true
    }
    
    func launchPlaylist(id: String) -> Bool {
        debugPrint("launchPlaylist: \(id)")
        return true
    }
    
    func launchVideo(media: DomainMediaDomain) -> Bool {
        debugPrint("launchVideo: \(media.title)")
        return true
    }
    
    func launchVideoSystem(platformId: String) -> Bool {
        debugPrint("launchVideoSystem: \(platformId)")
        return true
    }
    
    func launchVideoWithTimeSystem(media: DomainMediaDomain) -> Bool {
        debugPrint("launchVideoWithTimeSystem: \(media.positon)")
        return true
    }
    
    
}
