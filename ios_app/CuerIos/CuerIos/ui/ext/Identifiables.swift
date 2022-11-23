//
//  Identifiables.swift
//  CuerIos
//
//  Created by Robert Munro on 18/11/2022.
//

import Foundation
import shared

extension BrowseContractViewCategoryModel: Identifiable{}

extension PlaylistsItemMviContract.Model: Identifiable{}

extension URL: Identifiable {

    public var id: String {
        absoluteString
    }

}
