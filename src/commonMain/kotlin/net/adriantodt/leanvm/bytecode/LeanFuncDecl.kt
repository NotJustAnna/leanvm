package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import net.adriantodt.leanvm.utils.isU8
import net.adriantodt.leanvm.utils.readU8
import okio.Buffer

public class LeanFuncDecl(
    public val nameConst: Int,
    public val bodyId: Int,
    public val varargsParam: Int,
    public val paramArr: Array<LeanParamDecl>,
) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        check(paramArr.size.isU8) { "LeanFuncDecl.paramArr exceeds max size of U8 (0xFF)." }

        buffer.writeInt(nameConst)
            .writeInt(bodyId)
            .writeByte(if (varargsParam == -1) 0 else 1)

        if (varargsParam != -1) {
            buffer.writeByte(varargsParam)
        }

        buffer.writeByte(paramArr.size)
        for (param in paramArr) param.serializeTo(buffer)
    }

    public companion object : Deserializer<LeanFuncDecl> {
        public fun create(nameConst: Int, bodyId: Int, varargsParam: Int, paramArr: List<LeanParamDecl>): LeanFuncDecl {
            return LeanFuncDecl(nameConst, bodyId, varargsParam, paramArr.toTypedArray())
        }

        override fun deserializeFrom(buffer: Buffer): LeanFuncDecl {
            return LeanFuncDecl(
                buffer.readInt(),
                buffer.readInt(),
                if (buffer.readU8() != 0) buffer.readU8() else -1,
                Array(buffer.readU8()) { LeanParamDecl.deserializeFrom(buffer) }
            )
        }
    }
}
