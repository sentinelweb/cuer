//
//  PlaylistsDialogView.swift
//  CuerIos
//
//  Created by Robert Munro on 25/11/2022.
//

import SwiftUI
import shared

struct PlaylistsDialogView: View {
    
    @StateObject
    private var holder: PlaylistsDialogViewModelHolder
    
    init(holder: PlaylistsDialogViewModelHolder) {
        self._holder = StateObject(wrappedValue: holder)
    }
    
    var body: some View {
        //let _ = debugPrint("playlists dialog view")
        //ScrollView{
            //let _ = debugPrint(holder.model?.playistsModel)
            if (holder.model?.playistsModel != nil) {
                VStack(spacing: 0) {
                    TitleNavView(title: holder.model!.playistsModel!.title) {
                        holder.onUpClick()
                    }.padding(.top, 16).padding(.leading, 8)
                    List{
                        ForEach(holder.model!.playistsModel!.items) { item in
                            switch (item) {
                            case let header as PlaylistsItemMviContract.ModelHeader:
                                PlaylistsHeaderItemView(item: header)
                                
                            case let itemRow as PlaylistsItemMviContract.ModelItem:
                                PlaylistsItemRowView(item: item as! PlaylistsItemMviContract.ModelItem)
                                    .onTapGesture {holder.onItemSelected(item: itemRow)}
                                
                            default: Text("Unsupported item type: \(String(describing: item))")
                            }
                        }
                    }.padding(.bottom, 16)
                }
                .frame(width: UIScreen.main.bounds.width * 0.9, height: UIScreen.main.bounds.height * 0.8)
                .background(Color(.systemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .circular))
            }
        //}.frame(width: 200)
    }
}

//struct PlaylistsDialogView_Previews: PreviewProvider {
//    static var previews: some View {
//        PlaylistsDialogView()
//    }
//}
