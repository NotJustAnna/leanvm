package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.exceptions.Exceptions
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LType

public open class LeanRuntime {
    public open fun mismatchedArgs(parent: LeanMachineAccess) {
        parent.onThrow(Exceptions.mismatchedArgs(parent.stackTrace()))
    }

    public open fun noElementExists(parent: LeanMachineAccess, type: LType, name: String) {
        // TODO: Add type to the error message
        parent.onThrow(Exceptions.noElementExists(name, parent.stackTrace()))
    }

    public open fun getMember(target: LAny, name: String): LAny? {
        return null
    }
}
