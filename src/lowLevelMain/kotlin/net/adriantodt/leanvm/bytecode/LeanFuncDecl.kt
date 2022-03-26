package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.isU8
import net.adriantodt.leanvm.utils.readU8
import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import okio.Buffer
import okio.ByteString

public actual class LeanFuncDecl(
    public actual val nameConst: Int,
    public actual val bodyId: Int,
    public actual val varargsParam: Int,
    public actual val paramCount: Int,
    private val paramData: ByteString,
) : Serializable {

    private val paramArr: Array<LeanParamDecl?> = arrayOfNulls(paramCount)

    public actual fun paramOrNull(index: Int): LeanParamDecl? {
        if (index !in 0 until paramCount) return null
        val fromArray = paramArr[index]
        if (fromArray != null) return fromArray
        val param = LeanParamDecl.fromBytes(
            paramData.substring(index * LeanParamDecl.SIZE_BYTES, (index + 1) * LeanParamDecl.SIZE_BYTES)
        )
        paramArr[index] = param
        return param
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

        buffer.writeByte(paramCount).write(paramData)
    }

    public actual companion object : Deserializer<LeanFuncDecl> {
        public actual fun create(
            nameConst: Int,
            bodyId: Int,
            varargsParam: Int,
            paramArr: List<LeanParamDecl>,
        ): LeanFuncDecl {
            return LeanFuncDecl(
                nameConst,
                bodyId,
                varargsParam,
                paramArr.size,
                Buffer().apply { paramArr.forEach { it.serializeTo(this) } }.readByteString()
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanFuncDecl {
            val nameConst = buffer.readInt()
            val bodyId = buffer.readInt()
            val varargsParam = if (buffer.readU8() != 0) buffer.readU8() else -1
            val paramCount = buffer.readU8()
            val paramData = buffer.readByteString(paramCount.toLong() * LeanParamDecl.SIZE_BYTES)

            return LeanFuncDecl(nameConst, bodyId, varargsParam, paramCount, paramData)
        }

        internal fun determineByteSize(source: Buffer): Long {
            val buffer = source.copy()

            source.skip(Int.SIZE_BYTES * 2L)
            val hasVararg = buffer.readU8() != 0
            if (hasVararg) source.skip(1)

            val paramCount = buffer.readU8()
            source.skip(paramCount.toLong() * LeanParamDecl.SIZE_BYTES)

            return Int.SIZE_BYTES * 2L + if (hasVararg) 1 else 2 + paramCount * LeanParamDecl.SIZE_BYTES
        }

    }
}
