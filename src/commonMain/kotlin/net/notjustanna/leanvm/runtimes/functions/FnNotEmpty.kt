package net.notjustanna.leanvm.runtimes.functions

import net.notjustanna.leanvm.types.*

internal object FnNotEmpty : LNativeFunction("notEmpty") {
    override fun run(thisValue: LAny?, args: List<LAny>): LAny {
        val value = if (thisValue != null) {
            // Called as an extension function.
            if (args.isNotEmpty()) {
                throw IllegalArgumentException("notEmpty() takes no arguments.")
            } else {
                thisValue
            }
        } else {
            // Called as a static function.
            if (args.size != 1) {
                throw IllegalArgumentException("notEmpty() takes exactly one argument.")
            } else {
                args[0]
            }
        }
        return when (value) {
            is LString -> LBoolean.of(value.value.isNotEmpty())
            is LArray -> LBoolean.of(value.value.isNotEmpty())
            is LObject -> LBoolean.of(value.value.isNotEmpty())
            else -> throw IllegalArgumentException("notEmpty() can only be called on strings, arrays and objects.")
        }
    }
}
