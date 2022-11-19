//
//  CompatKotlin.swift
//  CuerIos
//
//  Created by Robert Munro on 19/11/2022.
//

infix operator ???

// https://stackoverflow.com/questions/65842032/kotlin-takeif-statement-equivalent-in-swift
// takeIf operator
func ???<T>(value: T?, predicate: (T?) throws -> Bool) rethrows -> T? {
    try predicate(value) ? value : nil
}