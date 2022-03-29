package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer

public data class LeanJumpLabel(val code: Int, val at: Int) : Serializable, Comparable<LeanJumpLabel> {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(code).writeInt(at)
    }

    override fun compareTo(other: LeanJumpLabel): Int {
        return code.compareTo(other.code)
    }

    public companion object : Deserializer<LeanJumpLabel> {
        override fun deserializeFrom(buffer: Buffer): LeanJumpLabel {
            return LeanJumpLabel(buffer.readInt(), buffer.readInt())
        }
    }
}
