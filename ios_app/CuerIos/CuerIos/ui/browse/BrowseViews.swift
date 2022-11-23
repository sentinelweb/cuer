//
//  Images.swift
//  CuerIos
//
//  Created by Robert Munro on 21/11/2022.
//

import SwiftUI
import shared
import Kingfisher

struct BrowseItem: View {
    
    let item:BrowseContractViewCategoryModel
    let seq: Int
    
    init( item:BrowseContractViewCategoryModel, seq: Int) {
        self.item = item
        self.seq = seq
    }
    
    var body: some View {
        VStack {
            let url = (item.thumbNailUrl ??? {$0?.starts(with: "https") ?? false}) ?? "https://cuer-275020.firebaseapp.com/images/categories/greek.jpg"
            KFImage(URL(string: url))
                .fade(duration: 0.3 + 0.1 * Double(seq % 10))
                .forceTransition()
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: UIScreen.main.bounds.width / 2, height: 150)
                .clipped()
                .overlay(titleOverlay(item: item), alignment: .bottom)
        }
    }
    
    @ViewBuilder
    private func titleOverlay(item: BrowseContractViewCategoryModel)-> some View {
        HStack {
            Text(item.title)
            Spacer()
        }
        .foregroundColor(Color(.label))
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
        .background(Color(.systemBackground).opacity(0.75))
        .font(itemTileTypeface)
    }
}

struct Images_Previews: PreviewProvider {
    static var previews: some View {
        BrowseItem(
            item: BrowseContractViewCategoryModel(
                id: 1,
                title: "title",
                description: "description",
                thumbNailUrl: "greek.jpg",
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
