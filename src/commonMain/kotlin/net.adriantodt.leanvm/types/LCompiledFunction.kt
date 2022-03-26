package net.adriantodt.leanvm.types

import net.adriantodt.leanvm.LeanMachine
import net.adriantodt.leanvm.Scope
import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanFuncDecl
import net.adriantodt.leanvm.context.FunctionSetupContext

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
