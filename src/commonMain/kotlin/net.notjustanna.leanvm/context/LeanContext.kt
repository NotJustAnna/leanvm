package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny

public interface LeanContext {
    public val runtime: LeanRuntime

    public fun step()

    public fun onReturn(value: LAny)

    public fun onThrow(value: LAny)

    public fun trace(): StackTrace?
}
