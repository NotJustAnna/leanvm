package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.bytecode.builder.LeanCodeBuilder
import net.adriantodt.leanvm.ctx.NodeExecutionContext
import kotlin.test.Test
import kotlin.test.assertFails

class ExecutionContextTest {
    @Test
    fun malformedPopScope() {
        val code = LeanCodeBuilder()
        val node = code.newNodeBuilder()
        node.popScopeInsn()

        val c = NodeExecutionContext(DummyLeanMachineControl(), code.build())
        assertFails {
            c.step()
        }
    }
}
