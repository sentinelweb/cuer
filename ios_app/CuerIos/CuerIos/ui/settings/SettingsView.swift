//
//  SettingsView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
import SwiftUI

struct SettingsView: View {

    // MARK: Stored Properties

    @StateObject var coordinator: MainCoordinator

    // MARK: Views

    var body: some View {
        VStack(spacing: 8) {
            Text("Sentinel Web Technologies Ltd").bold()
            Text("Chamonix, France")
        }
        .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, maxHeight: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/)
        .contentShape(Rectangle())
        .onTapGesture(perform: openWebsite)
        .navigationTitle("Settings")
    }

    // MARK: Methods

    private func openWebsite() {
        debugPrint("click!!")
        guard let url = URL(string: "https://sentinelweb.co.uk/") else {
            return assertionFailure()
        }
        self.coordinator.open(url)
    }

}

