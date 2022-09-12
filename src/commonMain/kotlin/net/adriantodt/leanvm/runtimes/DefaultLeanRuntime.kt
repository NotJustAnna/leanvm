package net.adriantodt.leanvm.runtimes

import net.adriantodt.leanvm.Scope
import net.adriantodt.leanvm.context.LeanRuntime
import net.adriantodt.leanvm.runtimes.functions.FnEmpty
import net.adriantodt.leanvm.runtimes.functions.FnSize

public class DefaultLeanRuntime : LeanRuntime() {
    override fun rootScope(): Scope {
        val scope = Scope()
        scope.define("size", false, FnSize)
        scope.define("empty", false, FnEmpty)
        scope.define("notEmpty", false, FnEmpty)
        return scope
    }
}
