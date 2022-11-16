package uk.co.sentinelweb.cuer.core.ntuple

// adapted from : https://stackoverflow.com/questions/46202147/kotlin-quadruple-quintuple-etc-for-destructuring

data class NTuple4<T1, T2, T3, T4>(val first: T1, val second: T2, val third: T3, val fourth: T4)

data class NTuple5<T1, T2, T3, T4, T5>(val first: T1, val second: T2, val third: T3, val fourth: T4, val fifth: T5)

data class NTuple6<T1, T2, T3, T4, T5, T6>(val first: T1, val second: T2, val third: T3, val fourth: T4, val fifth: T5, val sixth: T6)

infix fun <T1, T2, T3> Pair<T1, T2>.then(third: T3): Triple<T1, T2, T3> {
    return Triple(this.first, this.second, third)
}

infix fun <T1, T2, T3, T4> Triple<T1, T2, T3>.then(fourth: T4): NTuple4<T1, T2, T3, T4> {
    return NTuple4(this.first, this.second, this.third, fourth)
}

infix fun <T1, T2, T3, T4, T5> NTuple4<T1, T2, T3, T4>.then(fifth: T5): NTuple5<T1, T2, T3, T4, T5> {
    return NTuple5(this.first, this.second, this.third, this.fourth, fifth)
}

infix fun <T1, T2, T3, T4, T5, T6> NTuple5<T1, T2, T3, T4, T5>.then(sixth: T6): NTuple6<T1, T2, T3, T4, T5, T6> {
    return NTuple6(this.first, this.second, this.third, this.fourth, this.fifth, sixth)
}