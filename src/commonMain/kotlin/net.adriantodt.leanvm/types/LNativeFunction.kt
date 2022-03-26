package net.adriantodt.leanvm.types

import net.adriantodt.leanvm.context.LeanContext
import net.adriantodt.leanvm.context.LeanMachineControl
import net.adriantodt.leanvm.context.LeanRuntime
import net.adriantodt.leanvm.context.NativeFunctionContext

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
