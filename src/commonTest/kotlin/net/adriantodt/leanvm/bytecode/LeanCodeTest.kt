package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.bytecode.builder.LeanCodeBuilder
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class LeanCodeTest {
    @Test
    fun ensureReserialization() {
        val empty = LeanCode.create(listOf(), listOf(), listOf(), listOf(), listOf())

        val buffer = Buffer()
        buffer.write(empty.toBytes())
        LeanCode.deserializeFrom(buffer)
        assertEquals(buffer.size, 0)
    }

    @Test
    fun reserializeWithNode() {
        val builder = LeanCodeBuilder()
        val node = builder.newNodeBuilder()
        node.pushIntegerInsn(1L)
        repeat(200) {
            node.pushIntegerInsn(1L)
            node.addInsn()
            node.pushIntegerInsn(1L)
            node.subtractInsn()
        }

        val code = builder.build()

        val buffer = Buffer()
        buffer.write(code.toBytes())
        LeanCode.deserializeFrom(buffer)
        assertEquals(0, buffer.size)
    }

}
