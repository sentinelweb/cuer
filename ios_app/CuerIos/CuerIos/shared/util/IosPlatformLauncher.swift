//
//  IosPlatformLauncher.swift
//  CuerIos
//
//  Created by Robert Munro on 23/11/2022.
//

import Foundation
import shared

class IosPlatformLauncher: PlatformLaunchWrapper {
    
    let mainCoordnator: MainCoordinator
    
    init(mainCoordinator:MainCoordinator) {
        self.mainCoordnator = mainCoordinator
    }
    
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
        mainCoordnator.open(URL.init(string: YoutubeUrl.companion.channelUrl(media: media))!)
        debugPrint("launchChannel: \(media.channelData.title)")
        return true
    }
    
    func launchPlaylist(id: String) -> Bool {
        mainCoordnator.open(URL.init(string: YoutubeUrl.companion.playlistUrl(platformId: id))!)
        return true
    }
    
    func launchVideo(media: DomainMediaDomain) -> Bool {
        mainCoordnator.open(URL.init(string: YoutubeUrl.companion.videoUrl(media:  media))!)
        return true
    }
    
    func launchVideoSystem(platformId: String) -> Bool {
        debugPrint("launchVideoSystem: \(platformId)")
        return true
    }
    
    func launchVideoWithTimeSystem(media: DomainMediaDomain) -> Bool {
        mainCoordnator.open(URL.init(string: YoutubeUrl.companion.videoUrlWithTime(media: media))!)
        return true
    }
    
    
}
