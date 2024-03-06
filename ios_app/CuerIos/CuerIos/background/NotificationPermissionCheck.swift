//
//  NotificationPermissionCheck.swift
//  CuerIos
//
//  Created by rob munro on 05/03/2024.
//

import Foundation
import UserNotifications

class NotificationPermissionCheck {
    func askPermissionCheck() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            if settings.authorizationStatus == .authorized {
                // Notifications are allowed
            } else {
                self.askPermission()
            }
        }
    }
    
    private func askPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert,.badge]) { success, error in
            if success {
                print("access granted")
            } else if let error = error {
                debugPrint("askPermission: error: \(error.localizedDescription)")
            }
        }
    }
}
