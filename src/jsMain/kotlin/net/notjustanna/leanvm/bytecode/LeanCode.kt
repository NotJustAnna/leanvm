package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.isU24
import net.notjustanna.leanvm.utils.maxU24
import net.notjustanna.leanvm.utils.readU24
import net.notjustanna.leanvm.utils.writeU24
import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8

public actual class LeanCode(
    private val lConstArr: LongArray,
    private val sConstArr: Array<String>,
    private val funcArr: Array<LeanFuncDecl>,
    private val sectArr: Array<LeanSectDecl>,
    private val nodeArr: Array<LeanNode>,
) : Serializable {

    public actual val lCount: Int get() = lConstArr.size

    public actual fun lConstOrNull(index: Int): Long? {
        return lConstArr.getOrNull(index)
    }

    public actual fun lConst(index: Int): Long {
        return lConstOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access lConst $index on array with length ${lConstArr.size}")
    }

    public actual val sCount: Int get() = sConstArr.size

    public actual fun sConstOrNull(index: Int): String? {
        return sConstArr.getOrNull(index)
    }

    public actual fun sConst(index: Int): String {
        return sConstOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access sConst $index on array with length ${sConstArr.size}")
    }

    public actual val nodeCount: Int get() = nodeArr.size

    public actual fun nodeOrNull(index: Int): LeanNode? {
        return nodeArr.getOrNull(index)
    }

    public actual fun node(index: Int): LeanNode {
        return nodeOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access node $index on array with length ${nodeArr.size}")
    }

    public actual val sectCount: Int get() = sectArr.size

    public actual fun sectOrNull(index: Int): LeanSectDecl? {
        return sectArr.getOrNull(index)
    }

    public actual fun sect(index: Int): LeanSectDecl {
        return sectOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access sect $index on array with length ${sectArr.size}")
    }

    public actual val funcCount: Int get() = funcArr.size

    public actual fun funcOrNull(index: Int): LeanFuncDecl? {
        return funcArr.getOrNull(index)
    }

    public actual fun func(index: Int): LeanFuncDecl {
        return funcOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access func $index on array with length ${funcArr.size}")
    }

    override fun serializeTo(buffer: Buffer) {
        check(lConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the long pool exceeds the maximum size (${lConstArr.size} >= $maxU24)"
        }
        check(sConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the string pool exceeds the maximum size (${sConstArr.size} >= $maxU24)"
        }
        check(funcArr.size.isU24) {
            "Compiled Source cannot be serialized as the function definitions exceeds the maximum size (${funcArr.size} >= $maxU24)"
        }

        buffer.writeU24(lConstArr.size)
        for (l in lConstArr) buffer.writeLong(l)

        buffer.writeU24(sConstArr.size)
        for (s in sConstArr) {
            val encoded = s.encodeUtf8()
            buffer.writeInt(encoded.size).write(encoded)
        }

        buffer.writeU24(funcArr.size)
        for (func in funcArr) func.serializeTo(buffer)

        buffer.writeInt(sectArr.size)
        for (sect in sectArr) sect.serializeTo(buffer)

        buffer.writeInt(nodeArr.size)
        for (node in nodeArr) node.serializeTo(buffer)
    }

    public actual companion object : Deserializer<LeanCode> {

        public actual fun create(
            lConstArr: List<Long>,
            sConstArr: List<String>,
            funcArr: List<LeanFuncDecl>,
            sectArr: List<LeanSectDecl>,
            nodeArr: List<LeanNode>,
        ): LeanCode {
            return LeanCode(
                lConstArr.toLongArray(),
                sConstArr.toTypedArray(),
                funcArr.toTypedArray(),
                sectArr.toTypedArray(),
                nodeArr.toTypedArray(),
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanCode {
            val lConstArr = LongArray(buffer.readU24()) { buffer.readLong() }
            val sConstArr = Array(buffer.readU24()) { buffer.readByteString(buffer.readInt().toLong()).utf8() }
            val funcArr = Array(buffer.readU24()) { LeanFuncDecl.deserializeFrom(buffer) }
            val sectArr = Array(buffer.readInt()) { LeanSectDecl.deserializeFrom(buffer) }
            val nodeArr = Array(buffer.readInt()) { LeanNode.deserializeFrom(buffer) }
            return LeanCode(lConstArr, sConstArr, funcArr, sectArr, nodeArr)
        }

    }
}
