package net.adriantodt.leanvm.runtimes.functions

import net.adriantodt.leanvm.types.*

internal object FnEmpty : LNativeFunction("empty") {
    override fun run(thisValue: LAny?, args: List<LAny>): LAny {
        val value = if (thisValue != null) {
            // Called as an extension function.
            if (args.isNotEmpty()) {
                throw IllegalArgumentException("empty() takes no arguments.")
            } else {
                thisValue
            }
        } else {
            // Called as a static function.
            if (args.size != 1) {
                throw IllegalArgumentException("empty() takes exactly one argument.")
            } else {
                args[0]
            }
        }
        return when (value) {
            is LString -> LBoolean.of(value.value.isEmpty())
            is LArray -> LBoolean.of(value.value.isEmpty())
            is LObject -> LBoolean.of(value.value.isEmpty())
            else -> throw IllegalArgumentException("empty() can only be called on strings, arrays and objects.")
        }
    }
}
