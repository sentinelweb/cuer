package uk.co.sentinelweb.cuer.test

class MinStack<Type : Comparable<Type>> {

    private val stack = mutableListOf<Type>()
    private var _min: Type? = null
    val min get() = _min

    val top: Type?
        get() = stack
            .takeIf { stack.size > 0 }
            ?.get(stack.size - 1)

    fun push(number: Type) {
        stack += number
        _min = _min?.takeIf { number > it } ?: number
    }

    fun pop(): Type? =
        stack
            .takeIf { stack.size > 0 }
            ?.removeLast()
            ?.also {
                it.takeIf { it == min }?.also { this._min = min() }
            }

    private fun min() = stack.minByOrNull { it }

}