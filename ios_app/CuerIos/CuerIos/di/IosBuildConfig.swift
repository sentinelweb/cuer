//
//  IosBuildConfig.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import Foundation
//import shared
protocol IosBuildConfigDependency {
    var buildConfig:IosBuildConfig {get}
}
class IosBuildConfig  {
    var applicationId = "CuerAppID"
#if DEBUG
    var isDebug = true
#else
    var isDebug = false
#endif
    var versionCode: Int32 = 1
    var versionName = "0.77"
}
