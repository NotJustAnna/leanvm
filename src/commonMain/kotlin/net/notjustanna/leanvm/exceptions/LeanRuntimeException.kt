package net.notjustanna.leanvm.exceptions

import net.notjustanna.leanvm.StackTrace

public open class LeanRuntimeException(
    override val message: String,
    public val leanStackTrace: List<StackTrace>,
) : RuntimeException(message) {

    override fun toString(): String {
        return "${super.toString()}\n  in Lean:\n${leanStackTrace.joinToString("\n    at ", "    at ")}\n  in Platform:"
    }

    public open val leanExceptionName: String
        get() = "runtimeException"
}
