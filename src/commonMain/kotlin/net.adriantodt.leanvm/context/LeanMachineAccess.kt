package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.types.LAny

public interface LeanMachineAccess {
    public val runtime: LeanRuntime

    public fun push(layer: LeanContext)

    public fun replace(layer: LeanContext)

    public fun onReturn(value: LAny)

    public fun onThrow(value: LAny)

    public fun stackTrace(): List<StackTrace>
}
