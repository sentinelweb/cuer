//
//  TabViews.swift
//  CuerIos
//
//  Created by Robert Munro on 21/11/2022.
//

import SwiftUI

struct TabLabelView: View {
    let systemImage: String
    let text: String
    
    init(text: String, systemImage: String){
        self.systemImage = systemImage
        self.text = text
    }
    
    var body: some View {
        Label {
            Text(self.text)
//                .font(tabItemTypeface)
                .foregroundColor(.primary)
        } icon: {
            Image(systemName: self.systemImage)
                .font(.system(size: 12)) // fixme size icon: doesnt work
        }
    }
}

struct TabLabelView_Previews: PreviewProvider {
    static var previews: some View {
        TabLabelView(text:"title", systemImage:"list.bullet.indent")
    }
}
