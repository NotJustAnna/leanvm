package net.notjustanna.leanvm.types

import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.bytecode.LeanCode
import net.notjustanna.leanvm.bytecode.LeanFuncDecl
import net.notjustanna.leanvm.context.FunctionSetupContext
import net.notjustanna.leanvm.context.LeanContext
import net.notjustanna.leanvm.context.LeanMachineControl
import net.notjustanna.leanvm.context.LeanRuntime

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
