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
    var undoAction: (() -> Void)? = nil
    
    var body: some View {
        HStack {
            Text(message)
                .font(snackbarTypeface)
                .foregroundColor(.white)
                .padding()
            Spacer()
            if let action = undoAction {
                PillButtonInvert(text: "Undo", icon: "arrow.uturn.backward", action: action)
            }
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
