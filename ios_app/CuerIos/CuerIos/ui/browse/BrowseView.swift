//
//  BrowseView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI

struct BrowseView: View {
    @StateObject private var viewModel: BrowseViewModel
    
    init(viewModel: BrowseViewModel) {
        self._viewModel = StateObject(wrappedValue: viewModel)
    }
    
    var body: some View {
        Text("Browse, World!").onTapGesture {
            viewModel.execPlatformRequest()
        }
    }
}

//struct BrowseView_Previews: PreviewProvider {
//    static var previews: some View {
//        BrowseView()
//    }
//}
