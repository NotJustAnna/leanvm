package net.adriantodt.leanvm.runtimes.functions

import net.adriantodt.leanvm.types.LAny
import net.adriantodt.leanvm.types.LNativeFunction
import net.adriantodt.leanvm.types.LObject

internal object FnCreateMetaObject : LNativeFunction("createMetaObject") {
    override fun run(thisValue: LAny?, args: List<LAny>): LAny {
        val value = if (thisValue != null) {
            // Called as an extension function.
            if (args.isNotEmpty()) {
                throw IllegalArgumentException("createMetaObject() takes no arguments.")
            } else {
                thisValue
            }
        } else {
            // Called as a static function.
            if (args.size != 1) {
                throw IllegalArgumentException("createMetaObject() takes exactly one argument.")
            } else {
                args[0]
            }
        }

        if (value !is LObject) {
            throw IllegalArgumentException("createMetaObject() can only be called on objects.")
        }

        TODO("Implement meta objects")
    }
}
