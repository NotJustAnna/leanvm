package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.exceptions.Exceptions
import net.adriantodt.leanvm.types.LAny
import net.adriantodt.leanvm.types.LType

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
