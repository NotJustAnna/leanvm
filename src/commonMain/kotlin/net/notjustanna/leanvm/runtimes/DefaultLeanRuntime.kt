package net.notjustanna.leanvm.runtimes

import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.context.LeanRuntime
import net.notjustanna.leanvm.runtimes.functions.FnCreateMetaObject
import net.notjustanna.leanvm.runtimes.functions.FnEmpty
import net.notjustanna.leanvm.runtimes.functions.FnNotEmpty
import net.notjustanna.leanvm.runtimes.functions.FnSize

public class DefaultLeanRuntime : LeanRuntime() {
    override fun rootScope(): Scope {
        val scope = Scope()
        scope.define("size", false, FnSize)
        scope.define("empty", false, FnEmpty)
        scope.define("notEmpty", false, FnNotEmpty)
        scope.define("createMetaObject", false, FnCreateMetaObject)
        return scope
    }
}
