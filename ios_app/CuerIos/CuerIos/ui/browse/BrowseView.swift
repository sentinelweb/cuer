//
//  BrowseView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct BrowseView: View {
    @StateObject private var viewModel: BrowseViewModel
    private let apiKey:CuerYoutubeApiKeyProvider = CuerYoutubeApiKeyProvider()
    init(viewModel: BrowseViewModel) {
        self._viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        VStack {
            Text("Browse, World!").onTapGesture {
                viewModel.execPlatformRequest()
            }
            Text("apiKey.key")
            Text(apiKey.key)
        }
    }
}

//struct BrowseView_Previews: PreviewProvider {
//    static var previews: some View {
//        BrowseView()
//    }
//}
