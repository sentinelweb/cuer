//
//  ButtonViews.swift
//  CuerIos
//
//  Created by Robert Munro on 07/02/2023.
//

import SwiftUI

struct PillButton: View {
    let text: String
    let icon: String
    var fgColor: Color = Color.ui.pillForeground
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(fgColor)
                    .padding(.leading, 16)
                    .padding(.vertical, 12)
                    .frame(width: 30, height: 30, alignment: .center)
                
                Text(text)
                    .padding(.leading, 8)
                    .padding(.trailing, 16)
                    .padding(.vertical, 12)
                    .foregroundColor(fgColor)
                    .cornerRadius(Dimension.buttons.pillRadius)
            }
        }
        .overlay(
            RoundedRectangle(cornerRadius: Dimension.buttons.pillRadius)
                .stroke(fgColor, lineWidth: Dimension.buttons.pillLineWidth)
        ).padding(2)
    }
}

struct PillButtonInvert: View {
    let text: String
    let icon: String
    let action: () -> Void

    var body: some View {
        PillButton(text:text, icon: icon, fgColor: Color.ui.pillBackground, action: action)
    }
}

struct ButtonViews_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            PillButton(text: "Play", icon: "play.fill") {}
            HStack{
                PillButtonInvert(text: "Play", icon: "play.fill") {}
                    .padding(4)
                    .background(Color.black)
                    
            }
        }
    }
}
