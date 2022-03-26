package net.notjustanna.leanvm.bytecode

import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class LeanNodeTest {
    @Test
    fun serialization1() {
        val empty = LeanNode.create(listOf(), listOf(), listOf())

        val buffer = Buffer()
        buffer.write(empty.toBytes())
        val serialized = LeanNode.deserializeFrom(buffer)
        assertEquals(buffer.size, 0)

        println(serialized)
    }

    @Test
    fun serialization2() {
        val empty = LeanNode.create(listOf(LeanInsn.parameterless(LeanInsn.ParameterlessCode.DUP)), listOf(), listOf())

        val buffer = Buffer()
        buffer.write(empty.toBytes())
        val serialized = LeanNode.deserializeFrom(buffer)
        assertEquals(buffer.size, 0)

        println(serialized)
    }
}
