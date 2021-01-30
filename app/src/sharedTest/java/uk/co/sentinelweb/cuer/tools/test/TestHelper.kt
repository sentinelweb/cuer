package uk.co.sentinelweb.cuer.tools.test

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class TestHelper {

    companion object {

        fun setField(obj: Any, fieldName: String, newValue: Any?) {
            setFinalStatic(obj::class.java.getField(fieldName), newValue)
        }

        @Throws(Exception::class)
        private fun setFinalStatic(field: Field, newValue: Any?) {
            field.setAccessible(true)
            val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
            modifiersField.setAccessible(true)
            modifiersField.setInt(field, field.getModifiers() and Modifier.FINAL.inv())
            field.set(null, newValue)
        }
    }
}