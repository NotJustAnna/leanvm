package net.adriantodt.leanvm

import net.adriantodt.leanvm.context.LeanContext
import net.adriantodt.leanvm.context.LeanMachineAccess
import net.adriantodt.leanvm.context.LeanRuntime
import net.adriantodt.leanvm.types.LAny

public class LeanMachine(runtime: LeanRuntime = LeanRuntime(), initializer: (LeanMachineAccess) -> LeanContext) {
    private val stack = mutableListOf<LeanContext>()
    private var current: LeanContext = initializer(Access(runtime))
    private var result: LeanResult? = null

    public fun run(): LeanResult {
        while (hasNextStep()) {
            step()
        }
        return result()
    }

    public fun hasNextStep(): Boolean {
        return result == null
    }

    public fun step() {
        current.step()
    }

    public fun result(): LeanResult {
        return result ?: throw RuntimeException("Execution not finished")
    }

    private inner class Access(override val runtime: LeanRuntime) : LeanMachineAccess {
        override fun push(layer: LeanContext) {
            stack += current
            current = layer
        }

        override fun replace(layer: LeanContext) {
            current = layer
        }

        override fun onReturn(value: LAny) {
            val layer = stack.removeLastOrNull()
            if (layer == null) {
                result = LeanResult.Returned(value)
                return
            }
            current = layer
            layer.onReturn(value)
        }

        override fun onThrow(value: LAny) {
            val layer = stack.removeLastOrNull()
            if (layer == null) {
                result = LeanResult.Thrown(value)
                return
            }
            current = layer
            layer.onThrow(value)
        }

        override fun stackTrace(): List<StackTrace> {
            return (stack + current).asReversed().mapNotNull(LeanContext::trace)
        }
    }
}
