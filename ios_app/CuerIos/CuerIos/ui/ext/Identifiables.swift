//
//  Identifiables.swift
//  CuerIos
//
//  Created by Robert Munro on 18/11/2022.
//

import Foundation
import shared
// Redundant conformance of 'BrowseContractViewCategoryModel' to protocol 'Identifiable'
// for some bizzare reason this file isn't included in the test target or we get the above compile error.
extension BrowseContractViewCategoryModel: Identifiable{}

extension PlaylistsItemMviContract.Model: Identifiable{}

extension URL: Identifiable {
    
    public var id: String {
        absoluteString
    }
    
}
extension String: Identifiable {
    
    public var id: String {
        self
    }
    
}
