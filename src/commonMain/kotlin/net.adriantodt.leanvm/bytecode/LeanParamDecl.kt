package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import okio.Buffer

public data class LeanParamDecl(val nameConst: Int, val defaultValueNodeId: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(nameConst).writeInt(defaultValueNodeId)
    }

    public companion object : Deserializer<LeanParamDecl> {
        public const val SIZE_BYTES: Int = Int.SIZE_BYTES * 2

        override fun deserializeFrom(buffer: Buffer): LeanParamDecl {
            return LeanParamDecl(buffer.readInt(), buffer.readInt())
        }
    }
}
