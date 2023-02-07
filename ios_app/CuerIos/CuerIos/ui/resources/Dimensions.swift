//
//  Dimensions.swift
//  CuerIos
//
//  Created by Robert Munro on 07/02/2023.
//

import Foundation
import CoreGraphics

extension Dimension {
    static let buttons = Dimension.Buttons()
    static let spacing = Dimension.Spacing()
    
    struct Buttons {
        let pillRadius = CGFloat(24.0)
        let pillHeight = CGFloat(48.0)
        let pillLineWidth = CGFloat(1.0)
    }
    
    struct Spacing {
        let leadingHeader = CGFloat(8.0)
    }
}
