//
//  CuerYoutubeApiKeyProvider.swift
//  CuerIos
//
//  Created by Robert Munro on 17/11/2022.
//

import Foundation
import shared

class CuerYoutubeApiKeyProvider:NetKmmApiKeyProvider {
    var key: String {
      get {
          return getApiKey(key: "CUER_YOUTUBE_API_KEY")
      }
    }
}

class CuerPixabayApiKeyProvider:NetKmmApiKeyProvider {
    var key: String {
      get {
          return getApiKey(key: "CUER_PIXABAY_API_KEY")
      }
    }
}

func getApiKey(key:String) ->String {
    // 1
    guard let filePath = Bundle.main.path(forResource: "ApiKeys", ofType: "plist") else {
      fatalError("Couldn't find file 'ApiKeys.plist'.")
    }
    // 2
    let plist = NSDictionary(contentsOfFile: filePath)
    guard let value = plist?.object(forKey: key) as? String else {
      fatalError("Couldn't find key '\(key)' in 'ApiKeys.plist'.")
    }
    // 3
    if (value.starts(with: "_")) {
      fatalError("Key not provided: get template ApiKeys.plist from home/build folder")
    }
    return value
}
