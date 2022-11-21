//
//  Images.swift
//  CuerIos
//
//  Created by Robert Munro on 21/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct BrowseImage: View {
    
    let item:BrowseContractViewCategoryModel
    let seq: Int
    
    init( item:BrowseContractViewCategoryModel, seq: Int) {
        self.item = item
        self.seq = seq
    }
    var body: some View {
        VStack {
            let url = (item.thumbNailUrl ??? {$0?.starts(with: "https") ?? false})
                        ?? "https://cuer-275020.firebaseapp.com/images/categories/greek.jpg"
            KFImage(URL(string: url))
                .fade(duration: 0.3 + 0.1 * Double(seq))
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: UIScreen.main.bounds.width / 2, height: 150)
                .clipped()
                .overlay(titleOverlay(item: item), alignment: .bottom)
            // todo vary show time not fade time
//                .onAppear {
//                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
//                        self.isButtonHidden = false
//                    }
//                }
        }
        .clipShape(RoundedRectangle(cornerRadius: 25.0))
        
    }
    
    @ViewBuilder
    private func titleOverlay(item: BrowseContractViewCategoryModel)-> some View {
        return HStack {
            Text(item.title)
                Spacer()
            }
            .font(.headline)
            .foregroundColor(Color(.label))
            .padding(.horizontal, 20)
            .padding(.vertical, 8)
            .background(Color(.systemBackground).opacity(0.75))
    }
}

struct Images_Previews: PreviewProvider {
    static var previews: some View {
        BrowseImage(
            item: BrowseContractViewCategoryModel(
                id: 1,
                title: "title",
                description: "description",
                thumbNailUrl: "https://cuer-275020.firebaseapp.com/images/categories/greek.jpg",
                subCategories: [],
                subCount: 0,
                isPlaylist: true,
                forceItem: false,
                existingPlaylist: nil
            ),
            seq:0
        )
    }
}
