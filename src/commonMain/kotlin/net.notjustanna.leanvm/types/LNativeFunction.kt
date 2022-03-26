package net.notjustanna.leanvm.types

import net.notjustanna.leanvm.context.LeanContext
import net.notjustanna.leanvm.context.LeanMachineControl
import net.notjustanna.leanvm.context.LeanRuntime
import net.notjustanna.leanvm.context.NativeFunctionContext

public class LNativeFunction(
    override val name: String? = null,
    public val block: (thisValue: LAny?, args: List<LAny>) -> LAny,
    public val runtime: LeanRuntime? = null,
) : LFunction() {
    override fun setupContext(
        control: LeanMachineControl,
        thisValue: LAny?,
        args: List<LAny>,
        runtime: LeanRuntime?,
    ): LeanContext {
        return NativeFunctionContext(control, runtime ?: this.runtime ?: LeanRuntime(), this, thisValue, args)
    }
}
