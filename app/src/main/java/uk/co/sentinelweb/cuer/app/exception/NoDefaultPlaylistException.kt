package uk.co.sentinelweb.cuer.app.exception

class NoDefaultPlaylistException(msg: String? = "No default playlist and no playlist selected") :
    IllegalStateException(msg)