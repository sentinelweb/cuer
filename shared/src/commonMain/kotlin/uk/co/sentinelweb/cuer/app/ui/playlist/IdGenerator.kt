package uk.co.sentinelweb.cuer.app.ui.playlist

class IdGenerator {

    private var _modelIdGenerator = 0L

    var value: Long = 0
        get() {
            _modelIdGenerator--
            return _modelIdGenerator
        }
        set(value) = if (value != 0L) {
            throw IllegalArgumentException("You can only reset the generator")
        } else field = value
}