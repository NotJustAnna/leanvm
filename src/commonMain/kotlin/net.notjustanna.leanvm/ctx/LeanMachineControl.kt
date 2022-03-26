package net.notjustanna.leanvm.ctx

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny

public interface LeanMachineControl {
    public fun push(layer: LeanContext)

    public fun replace(layer: LeanContext)

    public fun onReturn(value: LAny)

    public fun onThrow(value: LAny)

    public fun stackTrace(): List<StackTrace>
}
