package net.notjustanna.leanvm.ctx

import net.notjustanna.unifiedplatform.currentPlatform
import net.notjustanna.leanvm.LeanMachine
import net.notjustanna.leanvm.bytecode.builder.LeanCodeBuilder
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

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

    @Test
    @OptIn(ExperimentalTime::class)
    fun crudeBenchmark() {
        val builder = LeanCodeBuilder()
        val node = builder.newNodeBuilder()
        node.pushIntegerInsn(1L)
        repeat(100_000) {
            node.pushIntegerInsn(1L)
            node.addInsn()
            node.pushIntegerInsn(1L)
            node.subtractInsn()
        }

        val (code, buildTime) = measureTimedValue { builder.build() }

        val vm = LeanMachine {
            NodeExecutionContext(it, code)
        }
        vm.step()

        val runtime = measureTime { vm.run() }

        println("## $currentPlatform")
        println("Build time: $buildTime")
        println("Time to execute 8,000,000 insns: $runtime")
        println("Insn/s: ${(8_000_000 / runtime.toDouble(DurationUnit.SECONDS)).toLong().pretty()} insns")
        println()
    }

    private fun Long.pretty(): String {
        return toString().reversed().chunked(3).reversed().joinToString(",") { it.reversed() }
    }
}
