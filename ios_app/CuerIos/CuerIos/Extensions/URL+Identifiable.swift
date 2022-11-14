//
//  URL+Identifiable.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation

extension URL: Identifiable {

    public var id: String {
        absoluteString
    }

}
