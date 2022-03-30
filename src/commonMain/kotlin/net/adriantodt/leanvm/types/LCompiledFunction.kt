package net.adriantodt.leanvm.types

import net.adriantodt.leanvm.Scope
import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanFuncDecl
import net.adriantodt.leanvm.context.FunctionSetupContext
import net.adriantodt.leanvm.context.LeanContext
import net.adriantodt.leanvm.context.LeanMachineControl
import net.adriantodt.leanvm.context.LeanRuntime

public class LCompiledFunction(
    public val source: LeanCode,
    public val data: LeanFuncDecl,
    public val runtime: LeanRuntime,
    public val rootScope: Scope,
) : LFunction() {
    override val name: String?
        get() = source.sConstArr.getOrNull(data.nameConst)

    override fun setupContext(
        control: LeanMachineControl,
        thisValue: LAny?,
        args: List<LAny>,
        runtime: LeanRuntime?,
    ): LeanContext {
        return FunctionSetupContext(control, this, runtime ?: this.runtime, thisValue, args)
    }
}
