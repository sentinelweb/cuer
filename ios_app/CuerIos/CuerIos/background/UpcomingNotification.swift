//
//  UpcomingView.swift
//  CuerIos
//
//  Created by rob munro on 05/03/2024.
//

import Foundation
import shared
import UserNotifications

class UpcomingNotification: UpcomingContractView {
    
    func showNotification(item: DomainPlaylistItemDomain) {
        //debugPrint("showNotification: \(item.media.title ?? "No title")")
        var trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let content = UNMutableNotificationContent()
        content.title = "Upcoming episode"
        content.body = item.media.title ?? "No title"
        
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
    }
    
    
}
