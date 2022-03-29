package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny

open class DummyLeanMachineControl : LeanMachineControl {
    override fun push(layer: LeanContext) {
        throw UnsupportedOperationException("push")
    }

    override fun replace(layer: LeanContext) {
        throw UnsupportedOperationException("replace")
    }

    override fun onReturn(value: LAny) {
        throw UnsupportedOperationException("onReturn")
    }

    override fun onThrow(value: LAny) {
        throw UnsupportedOperationException("onThrow")
    }

    override fun stackTrace(): List<StackTrace> {
        throw UnsupportedOperationException("stackTrace")
    }
}
