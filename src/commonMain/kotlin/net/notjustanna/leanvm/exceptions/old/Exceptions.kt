package net.notjustanna.leanvm.exceptions.old

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LArray
import net.notjustanna.leanvm.types.LObject
import net.notjustanna.leanvm.types.LString

public object Exceptions {
    public fun create(type: String, description: String, stackTrace: List<StackTrace>): LObject {
        return LObject(
            LString("errorType") to LString(type),
            LString("description") to LString(description),
            LString("stackTrace") to LArray(stackTrace.mapTo(mutableListOf()) { LString(it.toString()) })
        )
    }

    public fun mismatchedArgs(stackTrace: List<StackTrace>): LObject {
        return create(
            "mismatchedArguments",
            "Invocation failed due to mismatched arguments.",
            stackTrace
        )
    }

    public fun fromNative(e: Exception, stackTrace: List<StackTrace>): LAny {
        return create("nativeException", e.toString(), stackTrace)
    }
}
