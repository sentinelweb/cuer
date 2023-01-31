//
//  DateParseTest.swift
//  CuerIosTests
//
//  Created by Robert Munro on 31/01/2023.
//

import XCTest
import Foundation

class DateParseTest: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testDateParse() throws {
        let timestamp = "2021-11-22T20:18:21Z"
        let datestamp = "2021-11-22"
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "GMT")
        formatter.dateFormat = "yyyy-MM-dd"
        let parsed = formatter.date( from: datestamp)
        debugPrint(parsed)
        XCTAssertNotNil(parsed)
    }
    
    func testTimestampParse() throws {
        let timestamp = "2021-11-22T20:18:21.01Z"
        let timestampCleaned = timestamp.replacingOccurrences(of: "\\.\\d+", with: "", options: .regularExpression)
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "GMT")
        formatter.dateFormat = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        let parsed = formatter.date( from: timestampCleaned)
        XCTAssertNotNil(parsed)
    }
    
    func testTimestampParseMs() throws {
        let timestamp = "2021-11-22T20:18:21.01Z"
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "GMT")
        formatter.dateFormat = "uuuu-MM-dd'T'HH:mm:ss.SSSS'Z'"
        let parsed = formatter.date( from: timestamp)
        XCTAssertNotNil(parsed)
    }

//    func testPerformanceExample() throws {
//        // This is an example of a performance test case.
//        self.measure {
//            // Put the code you want to measure the time of here.
//        }
//    }

}
