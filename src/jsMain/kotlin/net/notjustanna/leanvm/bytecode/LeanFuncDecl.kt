package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.isU8
import net.notjustanna.leanvm.utils.readU8
import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer

public actual class LeanFuncDecl(
    public actual val nameConst: Int,
    public actual val bodyId: Int,
    public actual val varargsParam: Int,
    private val paramArr: Array<LeanParamDecl>
) : Serializable {

    public actual val paramCount: Int = paramArr.size

    public actual fun paramOrNull(index: Int): LeanParamDecl? {
        return paramArr.getOrNull(index)
    }

    public actual fun param(index: Int): LeanParamDecl {
        return paramOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access param $index on array with length ${paramArr.size}")
    }

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

    public actual companion object : Deserializer<LeanFuncDecl> {
        public actual fun create(
            nameConst: Int,
            bodyId: Int,
            varargsParam: Int,
            paramArr: List<LeanParamDecl>,
        ): LeanFuncDecl {
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