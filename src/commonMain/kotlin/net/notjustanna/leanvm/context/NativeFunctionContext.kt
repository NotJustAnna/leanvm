package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LNativeFunction

public class NativeFunctionContext(
    private val control: LeanMachineControl,
    override val runtime: LeanRuntime,
    private val function: LNativeFunction,
    private val thisValue: LAny? = null,
    private val args: List<LAny>,
) : LeanContext {
    override fun step() {
        control.onReturn(function.block(thisValue, args))
    }

    override fun onReturn(value: LAny) {
        control.onReturn(value) // Keep cascading.
    }

    override fun onThrow(value: LAny) {
        control.onThrow(value) // Keep cascading.
    }

    override fun trace(): StackTrace {
        return StackTrace(function.name ?: "<anonymous function>")
    }
}
