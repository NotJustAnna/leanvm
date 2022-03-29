package net.adriantodt.leanvm.exceptions

import net.adriantodt.leanvm.StackTrace

public open class LeanMachineException(message: String, private val trace: List<StackTrace>) : Exception(message) {
    override fun toString(): String {
        return "${super.toString()}\n  in Lean:\n${trace.joinToString("\n    at ", "    at ")}\n  in Platform:"
    }
}
