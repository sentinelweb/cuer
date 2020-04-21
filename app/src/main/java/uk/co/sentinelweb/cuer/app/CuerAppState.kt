package uk.co.sentinelweb.cuer.app

// todo think about this nice to have a global state injectable as needed
data class CuerAppState constructor(
    var connected:Boolean = false
)