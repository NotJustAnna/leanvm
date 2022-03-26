package net.adriantodt.leanvm.ctx

import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.types.LAny
import net.adriantodt.leanvm.types.LNativeFunction

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
