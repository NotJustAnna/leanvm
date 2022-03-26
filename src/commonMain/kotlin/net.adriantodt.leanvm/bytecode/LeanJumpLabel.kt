package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import okio.Buffer

public data class LeanJumpLabel(val code: Int, val at: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(code).writeInt(at)
    }

    public companion object : Deserializer<LeanJumpLabel> {
        public const val SIZE_BYTES: Int = Int.SIZE_BYTES * 2

        override fun deserializeFrom(buffer: Buffer): LeanJumpLabel {
            return LeanJumpLabel(buffer.readInt(), buffer.readInt())
        }
    }
}
