//
//  TitleNavView.swift
//  CuerIos
//
//  Created by Robert Munro on 25/11/2022.
//

import SwiftUI

struct TitleNavView: View {
    let title: String
    let backAction: () ->Void
    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            Image(systemName: "arrow.backward")
                .onTapGesture {backAction()}
                .padding(8)
            
            Text(title)
                .font(headerTypeface)
            Spacer()
        }
    }
}

struct TitleNavView_Previews: PreviewProvider {
    static var previews: some View {
        TitleNavView(title:"The title") {}
    }
}
