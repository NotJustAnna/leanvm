package net.adriantodt.leanvm.ctx

import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.types.LAny

public interface LeanContext {
    public val runtime: LeanRuntime

    public fun step()

    public fun onReturn(value: LAny)

    public fun onThrow(value: LAny)

    public fun trace(): StackTrace?
}
