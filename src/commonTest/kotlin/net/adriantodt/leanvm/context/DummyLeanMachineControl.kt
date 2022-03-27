package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.ctx.LeanContext
import net.adriantodt.leanvm.ctx.LeanMachineControl
import net.adriantodt.leanvm.types.LAny

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
