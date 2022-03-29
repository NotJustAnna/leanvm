package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer

public data class LeanParamDecl(val nameConst: Int, val defaultValueNodeId: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(nameConst).writeInt(defaultValueNodeId)
    }

    public companion object : Deserializer<LeanParamDecl> {
        override fun deserializeFrom(buffer: Buffer): LeanParamDecl {
            return LeanParamDecl(buffer.readInt(), buffer.readInt())
        }
    }
}
