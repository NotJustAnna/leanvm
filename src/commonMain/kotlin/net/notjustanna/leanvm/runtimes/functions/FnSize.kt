package net.notjustanna.leanvm.runtimes.functions

import net.notjustanna.leanvm.types.*

internal object FnSize : LNativeFunction("size") {
    override fun run(thisValue: LAny?, args: List<LAny>): LAny {
        val value = if (thisValue != null) {
            // Called as an extension function.
            if (args.isNotEmpty()) {
                throw IllegalArgumentException("size() takes no arguments.")
            } else {
                thisValue
            }
        } else {
            // Called as a static function.
            if (args.size != 1) {
                throw IllegalArgumentException("size() takes exactly one argument.")
            } else {
                args[0]
            }
        }
        return when (value) {
            is LString -> LInteger(value.value.length)
            is LArray -> LInteger(value.value.size)
            is LObject -> LInteger(value.value.size)
            else -> throw IllegalArgumentException("size() can only be called on strings, arrays and objects.")
        }
    }
}
