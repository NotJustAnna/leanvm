package net.adriantodt.leanvm.types

import net.adriantodt.leanvm.ctx.LeanContext
import net.adriantodt.leanvm.ctx.LeanMachineControl
import net.adriantodt.leanvm.ctx.LeanRuntime
import net.adriantodt.leanvm.ctx.NativeFunctionContext

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
