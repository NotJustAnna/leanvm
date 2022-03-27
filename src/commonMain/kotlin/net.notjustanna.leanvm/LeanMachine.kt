package net.notjustanna.leanvm

import net.notjustanna.leanvm.ctx.LeanContext
import net.notjustanna.leanvm.ctx.LeanMachineControl
import net.notjustanna.leanvm.types.LAny

public class LeanMachine(initializer: (LeanMachineControl) -> LeanContext) {
    private val stack = mutableListOf<LeanContext>()
    private val control: LeanMachineControl = Control()
    private var current: LeanContext = initializer(control)
    private var result: LeanResult? = null

    public fun run(): LeanResult {
        while (hasNextStep()) {
            try {
                step()
            } catch (e: Exception) {
                control.onThrow(
                    when (e) {
                        is LAnyException -> e.value
                        else -> current.runtime.handlePlatformException(control, e)
                    }
                )
            }
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

    public inner class Control : LeanMachineControl {
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
