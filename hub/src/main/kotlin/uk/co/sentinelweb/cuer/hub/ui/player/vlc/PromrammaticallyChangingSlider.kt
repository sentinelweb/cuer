package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import javax.swing.JSlider
import javax.swing.event.ChangeEvent

class PromrammaticallyChangingSlider : JSlider {
    private var programmaticChange = false

    var actualListener: ((e: ChangeEvent) -> Unit)? = null

    constructor() : super() {
        setupChangeListener()
    }

    constructor(orientation: Int) : super(orientation) {
        setupChangeListener()
    }

    constructor(min: Int, max: Int) : super(min, max) {
        setupChangeListener()
    }

    constructor(min: Int, max: Int, value: Int) : super(min, max, value) {
        setupChangeListener()
    }

    constructor(orientation: Int, min: Int, max: Int, value: Int) : super(orientation, min, max, value) {
        setupChangeListener()
    }

    private fun setupChangeListener() {
        addChangeListener {
            if (!programmaticChange) {
                actualListener?.invoke(it)
            }
        }
    }

    override fun setValue(n: Int) {
        programmaticChange = true
        super.setValue(n)
        programmaticChange = false
    }
}