//
//  SnackbarView.swift
//  CuerIos
//
//  Created by Robert Munro on 07/02/2023.
//

import SwiftUI

struct SnackbarView: View {
    let message: String
    let bgColor: Color = Color.ui.colorDelete
    var body: some View {
        HStack {
            Text(message)
                .font(.body)
                .foregroundColor(.white)
                .padding()
            Spacer()
            PillButtonInvert(text: "Undo", icon: "arrow.uturn.backward") {}
        }
        .background(bgColor)
        .cornerRadius(10)
        .padding()
        .offset(y: -32)
        .transition(.move(edge: .bottom))
        .animation(.default)
    }
}

struct SnackbarView_Previews: PreviewProvider {
    static var previews: some View {
        SnackbarView(message: "This is the message")
    }
}
