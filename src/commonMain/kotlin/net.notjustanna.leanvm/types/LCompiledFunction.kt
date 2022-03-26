package net.notjustanna.leanvm.types

import net.notjustanna.leanvm.LeanMachine
import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.bytecode.LeanCode
import net.notjustanna.leanvm.bytecode.LeanFuncDecl
import net.notjustanna.leanvm.context.FunctionSetupContext

public class LCompiledFunction(
    public val source: LeanCode,
    public val data: LeanFuncDecl,
    public val rootScope: Scope
) : LFunction() {
    override val name: String?
        get() = source.sConstOrNull(data.nameConst)

    override fun call(thisValue: LAny?, args: List<LAny>): LAny {
        return LeanMachine {
            FunctionSetupContext(it, this, thisValue, args.toList())
        }.run().getOrThrow()
    }
}
